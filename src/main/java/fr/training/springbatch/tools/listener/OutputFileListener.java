package fr.training.springbatch.tools.listener;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;

/**
 * @author Dave Syer
 *
 */
public class OutputFileListener {

    private static final Logger log = LoggerFactory.getLogger(OutputFileListener.class);

    private String outputKeyName = "outputFile";

    private String inputKeyName = "fileName";

    private String path = "file:./target/output/";

    public void setPath(final String path) {
        this.path = path;
    }

    public void setOutputKeyName(final String outputKeyName) {
        this.outputKeyName = outputKeyName;
    }

    public void setInputKeyName(final String inputKeyName) {
        this.inputKeyName = inputKeyName;
    }

    @BeforeStep
    public void createOutputNameFromInput(final StepExecution stepExecution) {
        final ExecutionContext executionContext = stepExecution.getExecutionContext();
        String inputName = stepExecution.getStepName().replace(":", "-");
        log.debug("inputName {} stepName {}", inputName, stepExecution.getStepName());
        if (executionContext.containsKey(inputKeyName)) {
            inputName = executionContext.getString(inputKeyName);
        }
        if (!executionContext.containsKey(outputKeyName)) {
            executionContext.putString(outputKeyName, path + FilenameUtils.getBaseName(inputName) + ".csv");
        }
    }

}