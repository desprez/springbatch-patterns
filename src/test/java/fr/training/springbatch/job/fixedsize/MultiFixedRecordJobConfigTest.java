package fr.training.springbatch.job.fixedsize;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, MultiFixedRecordJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=fixed-job", "application.batch.transmitterCode=AP99325" })
class MultiFixedRecordJobConfigTest {

    private static final String OUTPUT_FILE_PATH = "target/fixedresult.txt";
    private static final String INPUT_FILE_PATH = "src/test/resources/datas/fixed/multirecordfile.txt";

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void fixedJob_should_success() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
                .addString("inputfile", INPUT_FILE_PATH) //
                .addString("outputfile", OUTPUT_FILE_PATH) //
                .addString("receivercode", "AP99530") //
                .addDate("created-date", new SimpleDateFormat("yyyy-MM-dd").parse("2021-05-31")) //
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution jobExec = testUtils.startJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(new File(OUTPUT_FILE_PATH)).hasSameTextualContentAs(new File(INPUT_FILE_PATH));
    }

}
