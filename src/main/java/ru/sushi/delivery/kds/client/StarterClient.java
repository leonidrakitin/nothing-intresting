//package ru.sushi.delivery.kds.client;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.util.Assert;
//import org.springframework.web.client.ResponseErrorHandler;
//import org.springframework.web.client.RestClient;
//import ru.sushi.delivery.kds.config.StarterProperties;
//
//@Component
//public class StarterClient {
//
//    private final RestClient rest;
//    private final ResponseErrorHandler responseErrorHandler;
//    private final StarterProperties starterProperties;
//
//    @Autowired
//    public StarterClient(StarterProperties starterProperties, RestClient.Builder restClientBuilder) {
//        this.starterProperties = starterProperties;
//        this.responseErrorHandler = new OpenAiResponseErrorHandler();
//        this.rest = restClientBuilder
//                .baseUrl(this.starterProperties.getBaseUrl())
//                .defaultHeaders(headers -> {
//                    headers.set(OPEN_AI_BETA, ASSISTANTS_V2);
//                    headers.setBearerAuth(openAiToken);
//                    headers.setContentType(MediaType.APPLICATION_JSON);
//                })
//                .build();
//    }
//
//    public void getOrders() {
//
//    }
//
//    public void updateStatus() {
//
//    }
//}
