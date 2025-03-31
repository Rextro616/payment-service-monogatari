package org.entities.paymentresponse;

public class PaymentResponse {
    private String id;
    private String message;
    private double amount;

    public PaymentResponse(String id, String message, double amount) {
        this.id = id;
        this.message = message;
        this.amount = amount;
    }

    // Getters y setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
}

