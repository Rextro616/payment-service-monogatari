package org.service;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PayPalService {

    private PayPalHttpClient client;

    @ConfigProperty(name = "paypal.client.id")
    String clientId;

    @ConfigProperty(name = "paypal.client.secret")
    String clientSecret;

    @PostConstruct
    void init() {
        // Configura el entorno Sandbox para pruebas.
        PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        this.client = new PayPalHttpClient(environment);
    }

    /**
     * Crea una orden en PayPal para el monto y moneda indicados.
     * Se basa en el endpoint /v2/checkout/orders de la API Payments v2.
     */
    public Order createOrder(double amount, String currency) throws IOException {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode(currency)
                        .value(String.format("%.2f", amount)));
        purchaseUnits.add(purchaseUnit);
        orderRequest.purchaseUnits(purchaseUnits);

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(orderRequest);

        try {
            HttpResponse<Order> response = client.execute(request);
            return response.result();
        } catch (HttpException e) {
            System.err.println("Error al crear la orden: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Captura una orden previamente creada.
     * Se basa en el endpoint /v2/checkout/orders/{order_id}/capture.
     */
    public Order captureOrder(String orderId) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        // El body se mantiene vacío para la captura.
        request.requestBody(new OrderRequest());
        try {
            HttpResponse<Order> response = client.execute(request);
            return response.result();
        } catch (HttpException e) {
            System.err.println("Error al capturar la orden: " + e.getMessage());
            throw e;
        }
    }
}
