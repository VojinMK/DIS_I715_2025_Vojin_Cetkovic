package serviceLibrary.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class OrderEvent {

    private String messageId;
    private String customerEmail;
    private int orderId;
    private double totalAmount;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    public OrderEvent() {
    }

    public OrderEvent(String messageId, String customerEmail, int orderId, double totalAmount, String message, LocalDateTime eventDate) {
        this.messageId = messageId;
        this.customerEmail = customerEmail;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.message = message;
        this.eventDate = eventDate;
    }

    public String getMessageId() {
        return messageId;
    }

    public OrderEvent setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public OrderEvent setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
        return this;
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderEvent setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public OrderEvent setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public OrderEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public OrderEvent setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "messageId='" + messageId + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", orderId=" + orderId +
                ", totalAmount=" + totalAmount +
                ", message='" + message + '\'' +
                ", eventDate=" + eventDate +
                '}';
    }
}