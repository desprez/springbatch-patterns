package fr.training.springbatch.converter;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.core.convert.converter.Converter;

public class AugmentedJobParametersConverter extends DefaultJobParametersConverter {

    public static class PathToStringConverter implements Converter<Path, String> {
        @Override
        public String convert(final Path source) {
            return source.toString();
        }
    }

    public static class StringToPathConverter implements Converter<String, Path> {
        @Override
        public Path convert(final String source) {
            return Paths.get(source);
        }
    }

    public AugmentedJobParametersConverter() {
        conversionService.addConverter(new StringToPathConverter());
        conversionService.addConverter(new PathToStringConverter());
    }

}
