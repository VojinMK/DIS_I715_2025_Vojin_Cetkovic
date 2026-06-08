package emailService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import serviceLibrary.dtos.OrderEvent;

@Component
public class EmailListener {

    private final JavaMailSender mailSender;

    public EmailListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = MQConfig.QUEUE)
    public void handleOrderEvent(OrderEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("noreply@sportshop.com");
        message.setTo(event.getCustomerEmail());
        message.setSubject("Order confirmation #" + event.getOrderId());
        message.setText(
                "Dear user,\n\n" +
                event.getMessage() + "\n\n" +
                "Order ID: " + event.getOrderId() + "\n" +
                "Total amount: " + event.getTotalAmount() + " RSD\n" +
                "Event date: " + event.getEventDate() + "\n\n" +
                "Thank you for shopping with SportifyShop."
        );

        mailSender.send(message);

        System.out.println("Email sent to MailDev inbox: " + event.getCustomerEmail());
    }
}