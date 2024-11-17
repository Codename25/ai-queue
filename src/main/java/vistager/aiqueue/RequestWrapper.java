package vistager.aiqueue;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

public class RequestWrapper {
    private final HttpEntity<String> requestEntity;
    private final CompletableFuture<ResponseEntity<String>> responseFuture;

    public RequestWrapper(HttpEntity<String> requestEntity) {
        this.requestEntity = requestEntity;
        this.responseFuture = new CompletableFuture<>();
    }

    public HttpEntity<String> getRequestEntity() {
        return requestEntity;
    }

    public CompletableFuture<ResponseEntity<String>> getResponseFuture() {
        return responseFuture;
    }
}
