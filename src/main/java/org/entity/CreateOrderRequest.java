package org.entity;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String episodeId;
    private Integer episodeNumber;
    private String price;
}