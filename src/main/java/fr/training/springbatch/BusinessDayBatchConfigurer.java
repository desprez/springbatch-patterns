package fr.training.springbatch;

// import javax.sql.DataSource;
//
// import org.springframework.batch.core.launch.JobLauncher;
// import org.springframework.boot.autoconfigure.batch.BasicBatchConfigurer;
// import org.springframework.boot.autoconfigure.batch.BatchProperties;
// import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
// import org.springframework.context.annotation.Configuration;
//
// import fr.training.springbatch.job.daily.BusinessDayJobLauncher;

// @Configuration
// public class BusinessDayBatchConfigurer extends BasicBatchConfigurer {
//
// protected BusinessDayBatchConfigurer(final BatchProperties properties, final DataSource dataSource,
// final TransactionManagerCustomizers transactionManagerCustomizers) {
// super(properties, dataSource, transactionManagerCustomizers);
// }
//
// @Override
// protected JobLauncher createJobLauncher() throws Exception {
// final BusinessDayJobLauncher jobLauncher = new BusinessDayJobLauncher();
// jobLauncher.setJobRepository(getJobRepository());
// jobLauncher.afterPropertiesSet();
// return jobLauncher;
// }
//
// }
