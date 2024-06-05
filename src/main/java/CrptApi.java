import com.google.gson.Gson;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.Data;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;


public class CrptApi {

    private Duration timeRefreshPeriod;
    private int requestLimit;
    private Supplier<String> mySupplier;
    private String uriString;

    private JsonDocument jsonDocument;

    public CrptApi(Duration timeRefreshPeriod, int requestLimit, String uriString) {

        this.timeRefreshPeriod = timeRefreshPeriod;
        this.requestLimit = requestLimit;
        this.uriString = uriString;
    }

    private String httpRequest(JsonDocument jsonDocument) {
        System.out.println("Start request at " + LocalDateTime.now());
        URI uri = null;
        try {
            uri = new URI(this.uriString);
//            uri = new URI("http://localhost:8080/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonDocument)))
                .header("Content-type", "application/json").build();
        HttpResponse response = null;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return "Got response " + response.statusCode();
    }

    public CrptApi requestLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(this.requestLimit)
                .limitRefreshPeriod(this.timeRefreshPeriod)
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter limiter = registry.rateLimiter("myLimiter");

        mySupplier = RateLimiter.decorateSupplier(limiter,
                () -> httpRequest(this.jsonDocument));
        return this;
    }

    public void createDocument(JsonDocument jsonDocument) {
        this.jsonDocument = jsonDocument;
        System.out.println(mySupplier.get());
        System.out.println();
    }

    //-------------------JSON Handling-------------------------------------

    public static class JsonHandler {
        public static JsonDocument convertJsonToDocument(String path) {
            try (FileReader reader = new FileReader(path)) {
                Gson gson = new Gson();
                JsonDocument jsonDocument = gson.fromJson(reader, JsonDocument.class);
                System.out.println(jsonDocument);
                return jsonDocument;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static String convertToJson(JsonDocument jsonDocument) {
            Gson gson = new Gson();
            return gson.toJson(jsonDocument);
        }
    }

    //------------------JSON Document--------------------------------------

    @Data
    public class Description {
        String participantInn;
    }

    @Data
    public class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    @Data
    public class JsonDocument {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private Boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;

        private String reg_date;
        private String reg_number;

    }

}




