package emailService.integrationTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import emailService.EmailListener;
import serviceLibrary.dtos.OrderEvent;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceIntegrationTest {
	@Autowired
	private EmailListener emailListener;

	@MockitoBean
	private JavaMailSender mailSender;

	@Test
	void contextLoads_ShouldCreateEmailListenerBean() {
		assertNotNull(emailListener);
	}

	@Test
	void handleOrderEvent_ShouldSendEmail_WhenEventIsReceived() {
		OrderEvent event = new OrderEvent("message-1", "user@gmail.com", 1, 25999.98,
				"Your order has been created successfully.", LocalDateTime.of(2026, 6, 9, 14, 30));

		emailListener.handleOrderEvent(event);

		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		verify(mailSender, times(1)).send(messageCaptor.capture());

		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertEquals("noreply@sportshop.com", sentMessage.getFrom());
		assertNotNull(sentMessage.getTo());
		assertEquals("user@gmail.com", sentMessage.getTo()[0]);
		assertEquals("Order confirmation #1", sentMessage.getSubject());

		assertNotNull(sentMessage.getText());
		assertTrue(sentMessage.getText().contains("Dear user"));
		assertTrue(sentMessage.getText().contains("Your order has been created successfully."));
		assertTrue(sentMessage.getText().contains("Order ID: 1"));
		assertTrue(sentMessage.getText().contains("Total amount: 25999.98 RSD"));
		assertTrue(sentMessage.getText().contains("Thank you for shopping with SportifyShop."));
	}
}
