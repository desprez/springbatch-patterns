package fr.training.springbatch.tools.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import fr.training.springbatch.tools.notifier.EmailNotificationService;
import fr.training.springbatch.tools.notifier.JobMonitoringListener;

@ExtendWith(MockitoExtension.class)
class JobMonitoringListenerTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> captor;

    @Test
    void afterJob_with_failed_JobExecution_should_send_mail() throws Exception {
        // Given
        final SimpleMailMessage templateMessage = new SimpleMailMessage();
        templateMessage.setFrom("customerservice@mycompany.com");
        templateMessage.setSubject("your job");

        final JobMonitoringListener jobListener = new JobMonitoringListener(new EmailNotificationService(mailSender, templateMessage));

        final JobExecution jobExecution = getFailedJobExecution();

        // when
        jobListener.afterJob(jobExecution);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        assertThat(captor.getAllValues().get(0).getText())
                .startsWith("Job execution #122 of job instance #12 failed with following exceptions:java.lang.RuntimeException: exception example");
    }

    private JobExecution getFailedJobExecution() {
        final JobExecution jobExecution = MetaDataInstanceFactory.createJobExecutionWithStepExecutions(122L, Arrays.asList("step"));
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setExitStatus(ExitStatus.FAILED);
        jobExecution.setStartTime(LocalDateTime.now());
        jobExecution.setEndTime(jobExecution.getStartTime().plusSeconds(100));
        jobExecution.addFailureException(new RuntimeException("exception example"));
        return jobExecution;
    }

}
