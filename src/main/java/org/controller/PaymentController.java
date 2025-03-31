package org.controller;



import com.fasterxml.jackson.databind.JsonNode;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.entity.CaptureOrderRequest;
import org.entity.CreateOrderRequest;
import org.entity.PaymentResponse;
import org.service.PayPalService;

import java.util.Map;

@Path("/api/payment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentController {

    @Inject
    PayPalService payPalService;

    @POST
    @Path("/create-order")
    public Response createOrder(CreateOrderRequest request) {
        try {
            String orderId = payPalService.createOrder(
                    request.getEpisodeId(),
                    request.getEpisodeNumber(),
                    request.getPrice()
            );

            PaymentResponse response = new PaymentResponse();
            response.setOrderID(orderId);
            response.setStatus("CREATED");

            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/capture-order")
    public Response captureOrder(CaptureOrderRequest request) {
        try {
            JsonNode captureResponse = payPalService.captureOrder(request.getOrderID());

            // Aquí podrías guardar los detalles del pago en tu base de datos

            String status = captureResponse.get("status").asText();
            JsonNode purchaseUnit = captureResponse.get("purchase_units").get(0);
            String customId = purchaseUnit.get("custom_id").asText();
            String captureId = purchaseUnit.get("payments").get("captures").get(0).get("id").asText();

            PaymentResponse response = new PaymentResponse();
            response.setOrderID(request.getOrderID());
            response.setStatus(status);
            response.setCustomId(customId);
            response.setCaptureId(captureId);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }
}