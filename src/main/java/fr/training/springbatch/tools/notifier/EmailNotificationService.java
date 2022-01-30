package fr.training.springbatch.tools.notifier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Send Email implementation of the NotificationService.
 */
public class EmailNotificationService implements NotificationService {

	private final JavaMailSender mailSender;

	private final SimpleMailMessage templateMessage;

	public EmailNotificationService(final JavaMailSender mailSender, final SimpleMailMessage templateMessage) {
		this.mailSender = mailSender;
		this.templateMessage = templateMessage;
	}

	@Override
	public void notify(final JobExecution jobExecution) {
		final SimpleMailMessage msg = new SimpleMailMessage(templateMessage);
		msg.setTo("batch-admin@acme.com");
		final String content = createMessageContent(jobExecution);
		msg.setText(content);

		mailSender.send(msg);
	}

	private String createMessageContent(final JobExecution jobExecution) {
		final List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
		final StringBuilder content = new StringBuilder();
		content.append("Job execution #").append(jobExecution.getId());
		content.append(" of job instance #").append(jobExecution.getJobInstance().getId());
		content.append(" failed with following exceptions:");
		exceptions.forEach(e -> {
			content.append("");
			content.append(formatExceptionMsg(e));
		});
		return content.toString();
	}

	private String formatExceptionMsg(final Throwable e) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(baos));
		return baos.toString();
	}

}
