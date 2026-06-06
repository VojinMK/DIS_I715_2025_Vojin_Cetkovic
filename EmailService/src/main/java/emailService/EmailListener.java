package emailService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import serviceLibrary.dtos.OrderEvent;

@Component
public class EmailListener {

    @RabbitListener(queues = MQConfig.QUEUE)
    public void handleOrderEvent(OrderEvent event) {
        System.out.println("======================================");
        System.out.println("EMAIL SERVICE - ORDER CONFIRMATION");
        System.out.println("Sending email to: " + event.getCustomerEmail());
        System.out.println("Order ID: " + event.getOrderId());
        System.out.println("Total amount: " + event.getTotalAmount() + " RSD");
        System.out.println("Message: " + event.getMessage());
        System.out.println("Event date: " + event.getEventDate());
        System.out.println("Message ID: " + event.getMessageId());
        System.out.println("======================================");
    }
}