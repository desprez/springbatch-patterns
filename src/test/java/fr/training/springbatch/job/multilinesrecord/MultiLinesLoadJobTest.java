package fr.training.springbatch.job.multilinesrecord;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, MultiLinesLoadJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=multilines-load-job" })
class MultiLinesLoadJobTest {

    private static final String INPUT_FILE = "src/main/resources/csv/multilines.csv";

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void launch_MultilinesLoadJob_nominal_should_success() throws Exception {
        // Given
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "CUSTOMER", "TRANSACTION");

        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()).addString("input-file", INPUT_FILE) //
                .toJobParameters();
        // When
        final JobExecution jobExec = testUtils.launchJob(jobParameters);
        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

}
