package emailService.unitTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import emailService.EmailListener;
import serviceLibrary.dtos.OrderEvent;

@ExtendWith(MockitoExtension.class)

public class EmailServiceUnitTest {
	@Mock
	private JavaMailSender mailSender;

	private EmailListener emailListener;

	private OrderEvent orderEvent;

	@BeforeEach
	void setUp() {
		emailListener = new EmailListener(mailSender);

		orderEvent = new OrderEvent("message-1", "user@gmail.com", 1, 25999.98,
				"Your order has been created successfully.", LocalDateTime.of(2026, 6, 8, 14, 30, 0));
	}

	@Test
	void handleOrderEvent_ShouldSendEmail_WhenOrderEventIsReceived() {
		emailListener.handleOrderEvent(orderEvent);

		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		verify(mailSender, times(1)).send(messageCaptor.capture());

		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertEquals("noreply@sportshop.com", sentMessage.getFrom());
		assertNotNull(sentMessage.getTo());
		assertEquals(1, sentMessage.getTo().length);
		assertEquals("user@gmail.com", sentMessage.getTo()[0]);
		assertEquals("Order confirmation #1", sentMessage.getSubject());

		assertNotNull(sentMessage.getText());
		assertTrue(sentMessage.getText().contains("Dear user"));
		assertTrue(sentMessage.getText().contains("Your order has been created successfully."));
		assertTrue(sentMessage.getText().contains("Order ID: 1"));
		assertTrue(sentMessage.getText().contains("Total amount: 25999.98 RSD"));
		assertTrue(sentMessage.getText().contains("Thank you for shopping with SportifyShop."));
	}

	@Test
	void handleOrderEvent_ShouldUseEventDataInEmailContent() {
		OrderEvent customEvent = new OrderEvent("message-2", "anotheruser@gmail.com", 5, 9999.99,
				"Custom order message.", LocalDateTime.of(2026, 6, 8, 15, 0, 0));

		emailListener.handleOrderEvent(customEvent);

		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		verify(mailSender, times(1)).send(messageCaptor.capture());

		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertEquals("anotheruser@gmail.com", sentMessage.getTo()[0]);
		assertEquals("Order confirmation #5", sentMessage.getSubject());

		assertNotNull(sentMessage.getText());
		assertTrue(sentMessage.getText().contains("Custom order message."));
		assertTrue(sentMessage.getText().contains("Order ID: 5"));
		assertTrue(sentMessage.getText().contains("Total amount: 9999.99 RSD"));
	}
}
