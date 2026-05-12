package fr.training.springbatch.job.multilinesrecord;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, MultiLinesLoadJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=multilines-load-job" })
class MultiLinesLoadJobTest {

    private static final String INPUT_FILE = "src/main/resources/csv/multilines.csv";

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Job job;

    @Test
    void launch_MultilinesLoadJob_nominal_should_success() throws Exception {
        // Given
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "CUSTOMER", "TRANSACTION");

        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-file", INPUT_FILE) //
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution jobExec = testUtils.startJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

}
