package vistager.aiqueue;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class RequestQueueService {
    @Value("${ai.endpoint}")
    private String AI_ENDPOINT;
    private final BlockingQueue<RequestWrapper> requestQueue = new LinkedBlockingQueue<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private boolean isProcessing = false;

    @PostConstruct
    public void init() {
        new Thread(this::processQueue).start();
    }

    public synchronized void enqueueRequest(RequestWrapper requestWrapper) {
        requestQueue.add(requestWrapper);
        notifyAll();
    }

    private synchronized void processQueue() {
        while (true) {
            try {
                while (isProcessing || requestQueue.isEmpty()) {
                    wait();
                }
                if (!requestQueue.isEmpty()) {
                    RequestWrapper requestWrapper = requestQueue.poll();
                    if (requestWrapper != null) {
                        isProcessing = true;
                        forwardRequest(requestWrapper);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void forwardRequest(RequestWrapper requestWrapper) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(AI_ENDPOINT, HttpMethod.POST, requestWrapper.getRequestEntity(), String.class);
            requestWrapper.getResponseFuture().complete(response);
        } catch (Exception e) {
            requestWrapper.getResponseFuture().completeExceptionally(new RuntimeException("Failed to forward request: " + e.getMessage()));
        } finally {
            synchronized (this) {
                isProcessing = false;
                notifyAll();
            }
        }
    }
}
