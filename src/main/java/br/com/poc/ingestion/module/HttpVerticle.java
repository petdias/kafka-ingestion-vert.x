package br.com.poc.ingestion.module;

import br.com.poc.ingestion.domain.MessageType;
import br.com.poc.ingestion.util.StringUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.Objects.nonNull;

/**
 * Http verticle
 */
public class HttpVerticle extends AbstractVerticle {

    private final Logger log = Logger.getLogger(HttpVerticle.class.getName());

    private static final int PORT = 8080;

    private HttpServer server;

    private EventBus bus;

    private static JsonObject info = new JsonObject();

    final String hostname = getHostname();

    @Override
    public void start() {
        this.bus = vertx.eventBus();

        final Router router = Router.router(vertx);
        router.route().failureHandler(this::errorHandler);
        router.mountSubRouter("/ingestion", createRoutes());

        this.server = vertx.createHttpServer();
        server.requestHandler(router::accept)
            .rxListen(PORT, hostname)
            .doOnError(Throwable::printStackTrace)
            .doFinally(() -> log.info("Http module deployed successfully at http://" + hostname + ":" + PORT))
            .subscribe();
    }

    @Override
    public void stop() {
        server.rxClose()
            .doFinally(() -> log.info("Http module stopped."))
            .subscribe();
    }

    /**
     * Default error handler, just returns error code 500
     *
     * @param ctx routing context instance
     */
    private void errorHandler(RoutingContext ctx) {
        log.warning(ctx.failure().getMessage());
        ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        ctx.response().end("Ops! Something went wrong. Try again in a couple minutes.", "UTF-8");
    }


    private Router createRoutes() {
        final Router apiRouter = Router.router(vertx);
        apiRouter.route().handler(this.createCorsHandler());
        apiRouter.post("/config").blockingHandler(this.hit()).produces("application/json");
        apiRouter.get("/check").handler(test()).produces("application/json");

        return apiRouter;
    }

    public static Handler<RoutingContext> test() {
        return (ctx) -> {
            ok(ctx.response(), (new JsonObject()).put("status", "UP").encode());
        };
    }

    private Handler<RoutingContext> hit() {
        return (ctx) -> vertx.eventBus()
                .<JsonObject>rxSend(MessageType.HIT_CREATE, ctx.getBodyAsJson())
                .map(result -> result.body())
                .subscribe(res -> ok(ctx.response(), res.encode()));
    }

    public static void ok(final HttpServerResponse response, String content) {
        if (nonNull(response) && response.closed()) return;
        set(response);
        response.end(content);
    }

    private static void set(final HttpServerResponse response) {
        if (nonNull(response) && response.closed()) return;
        response.setStatusCode(HttpResponseStatus.OK.code());
        response.putHeader("Content-Type", "application/json");
        String version = nonNull(info.getString("version")) ? info.getString("version") : "";
        response.putHeader("Server", "Ingestion Server/".concat(version));
    }

    public static String getHostname() {
        String host = System.getenv().get("HOST");
        String hostname = System.getenv().get("HOSTNAME");
        if (StringUtil.isEmpty(hostname) && StringUtil.isNotEmpty(host)) {
            return host;
        } else if (StringUtil.isNotEmpty(hostname) && StringUtil.isEmpty(host)) {
            return hostname;
        } else {
            return StringUtil.isNotEmpty(hostname) && StringUtil.isNotEmpty(host) ? hostname : "127.0.0.1";
        }
    }

    /**
     * CORS settings
     *
     * @return CorsHandler
     */
    private CorsHandler createCorsHandler() {
        final Set<String> headers = new HashSet<>();
        headers.add("Accept");
        headers.add("Accept-Encoding");
        headers.add("Content-Type");
        headers.add("X-CRED");
        headers.add("User-Agent");

        return CorsHandler.create("*").allowedHeaders(headers)
                .allowedMethod(HttpMethod.HEAD)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS);
    }
}
