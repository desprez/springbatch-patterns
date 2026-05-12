package fr.training.springbatch.job.partition.file;

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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, FilePartitionJobConfig.class },
        properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=partition-job" })
class FilePartitionJobTest {

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void launch_PartitionJob_nominal_should_success() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-path", "csv/big/customer*.csv") //
                .addString("output-path", "target/output/files/") //
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution jobExec = testUtils.startJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

}
