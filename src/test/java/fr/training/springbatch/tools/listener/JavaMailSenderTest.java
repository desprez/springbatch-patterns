package fr.training.springbatch.tools.listener;

import static org.assertj.core.api.Assertions.assertThat;

import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

@ActiveProfiles("test")
@SpringBootTest(classes = { fr.training.springbatch.boot.BatchApplication.class })
class JavaMailSenderTest {

	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
	.withConfiguration(GreenMailConfiguration.aConfig().withUser("duke", "springboot"))
	.withPerMethodLifecycle(false);

	@Autowired
	private JavaMailSender javaMailSender;

	@Test
	void shouldUseGreenMail() throws Exception {

		final SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom("admin@spring.io");
		mail.setSubject("A new message for you");
		mail.setText("Hello GreenMail!");
		mail.setTo("test@greenmail.io");

		javaMailSender.send(mail);

		final MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		assertThat("Hello GreenMail!").isEqualTo(GreenMailUtil.getBody(receivedMessage));
		assertThat(1).isEqualTo(receivedMessage.getAllRecipients().length);
		assertThat("test@greenmail.io").isEqualTo(receivedMessage.getAllRecipients()[0].toString());

	}
}