package org.entity;


import lombok.Data;

@Data
public class PaymentResponse {
    private String orderID;
    private String status;
    private String customId;
    private String captureId;
}