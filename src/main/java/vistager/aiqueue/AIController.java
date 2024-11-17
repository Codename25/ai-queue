package vistager.aiqueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AIController {

    @Autowired
    private RequestQueueService requestQueueService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/ai/send-request")
    public ResponseEntity<String> sendRequest(@RequestBody RequestData requestData) {
        try {
            // Convert RequestData to JSON String
            String requestBody = objectMapper.writeValueAsString(requestData);

            // Set headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create HttpEntity with body and headers
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // Wrap the requestEntity in a RequestWrapper and enqueue it
            RequestWrapper requestWrapper = new RequestWrapper(requestEntity);
            requestQueueService.enqueueRequest(requestWrapper);

            // Wait for the response from the external service
            return requestWrapper.getResponseFuture().get();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to process request: " + e.getMessage());
        }
    }

}