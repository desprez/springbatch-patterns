package fr.training.springbatch.tools.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class ReportConsoleItemWriterTest {

    private ReportConsoleItemWriter<String> itemWriter;

    @BeforeEach
    public void setUp() {
        itemWriter = new ReportConsoleItemWriter<>();
    }

    @Test
    public void write_should_output_header_and_lines(final CapturedOutput capturedOutput) throws Exception {
        // Given
        itemWriter.setLineAggregator(new PassThroughLineAggregator<String>());

        // Set a header
        itemWriter.setHeader("Header");

        // Create a sample item
        final String item = "Sample Item";

        // When
        itemWriter.write(new Chunk<>(List.of(item)));

        // Then
        assertThat(capturedOutput.getOut()).contains("Header");
        assertThat(capturedOutput.getOut()).contains("Sample Item");
    }

}
