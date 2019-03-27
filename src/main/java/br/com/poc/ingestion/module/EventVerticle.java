package br.com.poc.ingestion.module;


import br.com.poc.ingestion.domain.Config;
import br.com.poc.ingestion.domain.MessageType;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Verticle responsible for send events to Kafka broker.
 */
public class EventVerticle extends AbstractVerticle {
    private final Logger log = Logger.getLogger(EventVerticle.class.getName());

    private final KafkaProducer<String, String> kafkaProducer;

    private EventBus bus;

    private List<String> users;

    private List<String> channels = Arrays.asList("TST8000", "SWS8001", "ESF8002");

    private List<String> urls = Arrays.asList("cartoes", "portal", "portal_pj", "amigo_de_valor", "ib", "carrinho", "ofertas", "emprestimo", "profile", "consorcio", "reneg", "home");

    private List<String> hitTypes = Arrays.asList("page-view", "click");

    private List<String> devices = Arrays.asList("MOBILE", "DESKTOP");

    public EventVerticle() {
        this.kafkaProducer = initialize();
    }

    @Override
    public void start() {
        bus = vertx.eventBus();
        bus.consumer(MessageType.HIT_CREATE, createEvent);
    }

    @Override
    public void stop() {
        log.info("Kafka module stopped!");
    }

    private KafkaProducer<String, String> initialize() {
        this.users = getUsers();
        final Properties props = new Properties();
        final String kafkaServers = System.getenv().getOrDefault("KAFKA_SERVERS", "localhost:9092");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "8192");
        props.put(ProducerConfig.RETRIES_CONFIG, "2");
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "200");
        props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, "5000");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "timeline-data");
        return new KafkaProducer(props);
    }

    private Handler<Message<JsonObject>> createEvent = (msg) -> {
        final Config config = Json.decodeValue(msg.body().encode(), Config.class);
        produceHit(config)
                .subscribe(msg::reply,
                        err -> { log.warning("Falha ao criar as mensagens -> " + err); msg.fail(-1, err.getMessage()); }
                );
    };

    private Single<JsonObject> produceHit(final Config config) {
        Integer count = 1;

        while (count <= config.getAmountMessages()) {

            final JsonObject h = JsonObject.mapFrom(getHit());
            final ProducerRecord<String, String> record = new ProducerRecord<>("timeline", h.encode());

            final RecordMetadata meta = await(this.kafkaProducer.send(record));
            if (Objects.nonNull(meta) && !meta.hasTimestamp())
                log.warning("No timestamp generated to hit sent.");

            count++;
        }

        return Single.just(new JsonObject().put("0", "mensagens criadas com sucesso!"));
    }

    private <T> T await(Future<T> future) {
        try {
            while (!future.isDone()) Thread.sleep(5);
            return future.get();
        } catch (Exception ex) {
            log.warning(ex.getMessage());
            return null;
        }
    }

    private JsonObject getHit() {
        shuffleCollections();

        JsonObject hit = new JsonObject();
        hit.put("hit_type", hitTypes.get(0));
        hit.put("channel",  channels.get(0));
        hit.put("dvc_type", devices.get(0));
        hit.put("hit_screen",  "https://www.santander.com.br/".concat(urls.get(0)));
        hit.put("uid",  users.get(0));
        hit.put("sid",  getRandom());
        hit.put("did",  getRandom());
        hit.put("hit_pagetitle",  "Santander");

        return hit;
    }

    private void shuffleCollections() {
        Collections.shuffle(hitTypes);
        Collections.shuffle(users);
        Collections.shuffle(channels);
        Collections.shuffle(urls);
        Collections.shuffle(devices);
    }

    private List<String> getUsers() {
        return Arrays.asList("494f9630-1988-11e9-b56e-0800200c9a66",
                "494f9631-1988-11e9-b56e-0800200c9a66",
                "494f9633-1988-11e9-b56e-0800200c9a66",
                "494f9632-1988-11e9-b56e-0800200c9a66",
                "494f9634-1988-11e9-b56e-0800200c9a66",
                "494fbd40-1988-11e9-b56e-0800200c9a66",
                "494fbd41-1988-11e9-b56e-0800200c9a66",
                "494fbd42-1988-11e9-b56e-0800200c9a66",
                "494fbd43-1988-11e9-b56e-0800200c9a66",
                "494fbd44-1988-11e9-b56e-0800200c9a66",
                "494fbd45-1988-11e9-b56e-0800200c9a66",
                "494fbd46-1988-11e9-b56e-0800200c9a66",
                "494fbd47-1988-11e9-b56e-0800200c9a66",
                "494fbd48-1988-11e9-b56e-0800200c9a66",
                "494fbd49-1988-11e9-b56e-0800200c9a66",
                "494fbd4a-1988-11e9-b56e-0800200c9a66",
                "494fbd4b-1988-11e9-b56e-0800200c9a66",
                "494fbd4c-1988-11e9-b56e-0800200c9a66",
                "494fbd4d-1988-11e9-b56e-0800200c9a66",
                "494fbd4e-1988-11e9-b56e-0800200c9a66",
                "494fbd4e--11e9-b56e-" + getRandom(),
                "494fbd4e--11e9-b56e-" + getRandom(),
                "494fbd4e--11e9-b56e-" + getRandom(),
                "494fbd4e--11e9-b56e-" + getRandom());
    }

    private Integer getRandom() {
        Random r = new Random();
        return r.nextInt(1000000000);
    }
 }
