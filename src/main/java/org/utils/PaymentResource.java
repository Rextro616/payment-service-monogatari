package org.utils;

import com.paypal.orders.Order;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.entities.paymentrequest.PaymentRequest;
import org.entities.paymentresponse.PaymentResponse;
import org.service.PayPalService;

@Path("/payments")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Payments", description = "Endpoints para procesar pagos")
public class PaymentResource {

    @Inject
    PayPalService payPalService;

    @POST
    @Operation(summary = "Crear un pago", description = "Crea un nuevo pago mediante PayPal y retorna el resultado")
    @APIResponse(responseCode = "201", description = "Pago creado exitosamente")
    public Response createPayment(PaymentRequest request) {
        // Si el método de pago es PayPal, se utiliza la integración
        if ("paypal".equalsIgnoreCase(request.getPaymentMethod())) {
            try {
                Order order = payPalService.createOrder(request.getAmount(), request.getCurrency());
                PaymentResponse response = new PaymentResponse(order.id(), "Orden creada con PayPal", request.getAmount());
                return Response.status(Response.Status.CREATED).entity(response).build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error al procesar el pago con PayPal: " + e.getMessage())
                        .build();
            }
        }
        // Implementa aquí otros métodos de pago o una respuesta por defecto
        PaymentResponse response = new PaymentResponse("12345", "Pago procesado exitosamente", request.getAmount());
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Consultar pago", description = "Consulta el estado de un pago por su identificador")
    @APIResponse(responseCode = "200", description = "Pago encontrado")
    @APIResponse(responseCode = "404", description = "Pago no encontrado")
    public Response getPayment(@PathParam("id") String id) {
        // Implementación dummy para propósitos de demostración
        if ("12345".equals(id)) {
            PaymentResponse response = new PaymentResponse("12345", "Pago procesado exitosamente", 100.0);
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}