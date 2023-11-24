package fr.training.springbatch.tools.validator;

import static org.springframework.util.StringUtils.quote;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.batch.core.JobParameter;
import org.springframework.util.Assert;

@FunctionalInterface
public interface ParameterRequirement<T> {

    /**
     * Single method of the @FunctionalInterface, defines a method to check a specific requirement for a job parameter.
     *
     * @param jobParameter
     * @return
     */
    String validate(JobParameter<?> jobParameter);

    static ParameterRequirement<?> required() {
        return jobParameter -> {
            if (jobParameter == null) {
                return " is required";
            }
            return null;
        };
    }

    // optional means No check
    static ParameterRequirement<?> optional() {
        return jobParameter -> null;
    }

    // Check null Value
    static ParameterRequirement<?> nullValue() {
        return jobParameter -> {
            if (Objects.isNull(jobParameter.getValue())) {
                return null;
            }
            return " must have null value";
        };
    }

    // Check Not null Value
    static ParameterRequirement<?> notNullValue() {
        return jobParameter -> {
            if (Objects.nonNull(jobParameter.getValue())) {
                return null;
            }
            return " must have not null value";
        };
    }

    // Check
    static ParameterRequirement<?> expectedValue(final Object expectedValue) {
        return jobParameter -> {
            if (Objects.equals(expectedValue, jobParameter.getValue())) {
                return null;
            }
            return " must have expected '%s' value".formatted(expectedValue.toString());
        };
    }

    static ParameterRequirement<?> valueIn(final Collection<?> values) {
        return jobParameter -> {
            if (values.contains(jobParameter.getValue())) {
                return null;
            }
            return " must have value in "
                    + quote(values.stream().map(Objects::toString).collect(Collectors.joining(", ")))
                    + " (current value is : " + quote(jobParameter.getValue().toString()) + ")";
        };
    }

    static ParameterRequirement<?> valueIn(final Object... values) {
        return valueIn(Arrays.asList(values));
    }

    default ParameterRequirement<T> and(final ParameterRequirement<?> parameterRequirement) {
        Assert.notNull(parameterRequirement, "parameterRequirement must be not null");
        return jobParameter -> {
            final String errorThis = this.validate(jobParameter);
            if (errorThis != null) {
                return errorThis;
            }
            return parameterRequirement.validate(jobParameter);
        };
    }

    default ParameterRequirement<T> or(final ParameterRequirement<T> parameterRequirement) {
        Assert.notNull(parameterRequirement, "parameterRequirement must be not null");
        return jobParameter -> {
            final String errorThis = this.validate(jobParameter);
            if (errorThis == null) {
                return errorThis;
            }
            return parameterRequirement.validate(jobParameter);
        };
    }

    static ParameterRequirement<?> identifying() {
        return jobParameter -> {
            if (!jobParameter.isIdentifying()) {
                return " must be identifying";
            }
            return null;
        };
    }

    static ParameterRequirement<?> nonIdentifying() {
        return jobParameter -> {
            if (jobParameter.isIdentifying()) {
                return " must be non-identifying";
            }
            return null;
        };
    }

    static ParameterRequirement<?> type(final Class<?> type) {
        Assert.notNull(type, "type must be not null");
        return jobParameter -> {
            if (!jobParameter.getType().equals(type)) {
                final var jobParameterTypeName = Optional.ofNullable(jobParameter)
                        .map(JobParameter::getType)
                        .map(Class::getName)
                        .orElse("unknown");
                return " must be of type '%s' (current type is : '%s')".formatted(type.getName(), jobParameterTypeName);
            }
            return null;
        };
    }

    static ParameterRequirement<?> stringType() {
        return type(String.class);
    }

    static ParameterRequirement<?> dateType() {
        return type(Date.class);
    }

    static ParameterRequirement<?> longType() {
        return type(Long.class);
    }

    static ParameterRequirement<?> doubleType() {
        return type(Double.class);
    }

    static ParameterRequirement<? extends Number> gt(final Number requiredValue) {
        return jobParameter -> {
            if ((Double) jobParameter.getValue() <= requiredValue.doubleValue()) {
                return " has value '%s' but required value is greater than '%s'".formatted(jobParameter.getValue(), requiredValue);
            }
            return null;
        };
    }

    static ParameterRequirement<? extends Number> lt(final Number requiredValue) {
        return jobParameter -> {
            if ((Double) jobParameter.getValue() >= requiredValue.doubleValue()) {
                return " has value '%s' but required value is less than '%s'".formatted(jobParameter.getValue(), requiredValue);
            }
            return null;
        };
    }

    static ParameterRequirement<?> before(final Date date) {
        return jobParameter -> {
            if (jobParameter.getValue() instanceof final Date value) {
                if (value.before(date)) {
                    return null;
                }
            }
            if (jobParameter.getValue() instanceof final Date value) {

            }
            return " must be before '%s' (current type is : '%s')".formatted(date, jobParameter.getValue());
        };
    }

    // Check if a numeric value is positive
    static ParameterRequirement<? extends Number> positiveNumber() {
        return jobParameter -> {
            if (jobParameter.getValue() instanceof final Number value) {
                if (value.doubleValue() > 0) {
                    return null;
                }
            }
            return " must be a positive number";
        };
    }

    // Check if a file with value's path exist
    static ParameterRequirement<?> fileExist() {
        return notNullValue().and(jobParameter -> {
            final String fileName = (String) jobParameter.getValue();
            final Path filePath = Paths.get(fileName);
            if (Files.notExists(filePath) || Files.isDirectory(filePath)) {
                return String.format(" with path [%s] does not exist", fileName);
            }
            return null;
        });
    }

    // Check if a file with value's path is readable
    static ParameterRequirement<?> fileReadable() {
        return notNullValue().and(jobParameter -> {
            final String fileName = (String) jobParameter.getValue();
            final Path filePath = Paths.get(fileName);
            if (Files.isReadable(filePath)) {
                return null;
            }
            return String.format(" with path [%s] is not readable", fileName);
        });
    }

    // Check if a file with value's path is writable
    static ParameterRequirement<?> fileWritable() {
        return notNullValue().and(jobParameter -> {
            final String fileName = (String) jobParameter.getValue();
            final Path filePath = Paths.get(fileName);
            if (Files.isWritable(filePath)) {
                return null;
            }
            return String.format(" with path [%s] is not Writable", fileName);
        });
    }

    // Check if a directory with value's path exist
    static ParameterRequirement<?> directoryExist() {
        return notNullValue().and(jobParameter -> {
            final String fileName = (String) jobParameter.getValue();
            final Path path = Paths.get(fileName);
            if (Files.isDirectory(path)) {
                return null;
            }
            return String.format(" with path [%s] is not a valid directory", fileName);
        });
    }

}
