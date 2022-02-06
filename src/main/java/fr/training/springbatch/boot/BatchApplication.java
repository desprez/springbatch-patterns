package fr.training.springbatch.boot;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import fr.training.springbatch.job.controlbreak.ControlBreakJobConfig;
import fr.training.springbatch.job.daily.DailyJobConfig;
import fr.training.springbatch.job.explore.ExploreJobConfig;
import fr.training.springbatch.job.extract.SimpleExtractJobConfig;
import fr.training.springbatch.job.extract.processindicator.ExtractProcessIndicatorJobConfig;
import fr.training.springbatch.job.fixedsize.MultiFixedRecordJobConfig;
import fr.training.springbatch.job.load.SimpleLoadJobConfig;
import fr.training.springbatch.job.partition.jdbc.JDBCPartitionJobConfig;
import fr.training.springbatch.job.purge.PurgeHistoryJobConfig;
import fr.training.springbatch.job.staging.StagingJobConfig;
import fr.training.springbatch.job.synchro.File2FileSynchroJobConfig;
import fr.training.springbatch.job.synchro.File2TableSynchroJobConfig;
import fr.training.springbatch.job.synchro.GroupingRecordsJobConfig;
import fr.training.springbatch.job.synchro.SQLJoinSynchroJobConfig;
import fr.training.springbatch.job.synchro.Table2FileSynchroJobConfig;

@SpringBootApplication
@EnableBatchProcessing(modular = true)
public class BatchApplication {

	@Value("${spring.batch.job.names:#{null}}")
	private String jobName;

	public static void main(final String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(BatchApplication.class, args)));
	}

	@Bean
	public ApplicationContextFactory simpleExtractJobContextFactory() {
		return new GenericApplicationContextFactory(SimpleExtractJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory simpleLoadJobContextFactory() {
		return new GenericApplicationContextFactory(SimpleLoadJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory stagingJobContextFactory() {
		return new GenericApplicationContextFactory(StagingJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory table2FileSynchroJobContextFactory() {
		return new GenericApplicationContextFactory(Table2FileSynchroJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory file2TableSynchroJobContextFactory() {
		return new GenericApplicationContextFactory(File2TableSynchroJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory file2FileSynchroJobContextFactory() {
		return new GenericApplicationContextFactory(File2FileSynchroJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory groupingRecordsJobContextFactory() {
		return new GenericApplicationContextFactory(GroupingRecordsJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory controlBreakJobConfigJobContextFactory() {
		return new GenericApplicationContextFactory(ControlBreakJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory sqlJoinSynchroJobContextFactory() {
		return new GenericApplicationContextFactory(SQLJoinSynchroJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory purgeHistoryJobContextFactory() {
		return new GenericApplicationContextFactory(PurgeHistoryJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory multiFixedRecordJobConfigContextFactory() {
		return new GenericApplicationContextFactory(MultiFixedRecordJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory dailyJobConfigContextFactory() {
		return new GenericApplicationContextFactory(DailyJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory partitionJobConfigContextFactory() {
		return new GenericApplicationContextFactory(JDBCPartitionJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory extractProcessIndicatorJobConfigContextFactory() {
		return new GenericApplicationContextFactory(ExtractProcessIndicatorJobConfig.class);
	}

	@Bean
	public ApplicationContextFactory exploreJobConfigContextFactory() {
		return new GenericApplicationContextFactory(ExploreJobConfig.class);
	}
}
