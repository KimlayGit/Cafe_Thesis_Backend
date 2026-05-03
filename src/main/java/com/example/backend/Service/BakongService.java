package com.example.backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class BakongService {

    @Value("${bakong.api-url}")
    private String apiUrl;

    @Value("${bakong.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> checkPayment(String md5) {
        try {
            String url = apiUrl + "/v1/check_transaction_by_md5";

            Map<String, String> body = new HashMap<>();
            body.put("md5", md5);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            System.out.println("Bakong check failed: " + e.getMessage());
            return null;
        }
    }
}