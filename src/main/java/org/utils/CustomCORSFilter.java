package org.utils;


import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class CustomCORSFilter {

    public void init(@Observes Router router) {
        router.route().order(-2).handler(ctx -> {
            String origin = ctx.request().getHeader("Origin");
            if (origin != null) {
                // Permitir cualquier origen durante el desarrollo
                ctx.response().putHeader("Access-Control-Allow-Origin", origin);
                ctx.response().putHeader("Access-Control-Allow-Credentials", "true");
                ctx.response().putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                ctx.response().putHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
                ctx.response().putHeader("Access-Control-Max-Age", "86400");
            }

            if (ctx.request().method() == HttpMethod.OPTIONS) {
                // Responder inmediatamente a las solicitudes OPTIONS preflight
                ctx.response().setStatusCode(204).end();
                return;
            }

            ctx.next();
        });
    }
}