package br.com.poc.ingestion;

import br.com.poc.ingestion.module.EventVerticle;
import br.com.poc.ingestion.module.HttpVerticle;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

import java.util.Objects;

import static io.vertx.reactivex.core.RxHelper.deployVerticle;
import static java.lang.System.setProperty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Bootstrap application class
 */
public class Application {

    private final Vertx vertx;

    private final long MAX_EVENT_LOOP_EXECUTION = 3L * 1000 * 1000000;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public Application() {
        final VertxOptions vOptions = new VertxOptions();
        final int totalProcessors = Runtime.getRuntime().availableProcessors();

        vOptions.setEventLoopPoolSize(totalProcessors * 4)
            .setMaxEventLoopExecuteTime(MAX_EVENT_LOOP_EXECUTION)
            .setWorkerPoolSize(32)
            .setPreferNativeTransport(true);
        vertx = Vertx.vertx(vOptions);
    }

    public void run() {
        final Disposable deployments = deployVerticle(vertx, new EventVerticle())
            .delay(500, MILLISECONDS)
            .flatMap(id -> deployVerticle(vertx, new HttpVerticle()))
            .subscribe(id -> System.out.println("Ingestion API has been started."), err -> System.exit(-1));

        disposables.add(deployments);
    }

    public void dispose() {
        Objects.requireNonNull(vertx);
        final String[] verticleIds = vertx.deploymentIDs().toArray(new String[]{});
        final Disposable undeployments = Observable.fromArray(verticleIds).subscribe(vertx::undeploy);
        disposables.add(undeployments);

        disposables.clear();
    }

    public static void main(String... args) {
        setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, "io.vertx.core.logging.SLF4JLogDelegateFactory");

        final Application app = new Application();
        Runtime.getRuntime().addShutdownHook(new Thread(app::dispose));

        app.run();
    }
}
