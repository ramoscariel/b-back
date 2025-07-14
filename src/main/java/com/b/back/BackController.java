package com.b.back;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/proxy/messages")
@RequiredArgsConstructor
public class BackController {

    private final RestTemplate restTemplate;

    @Value("${message.service.url:http://localhost:8081}")
    private String baseUrl;

    @GetMapping("/list")
    public ResponseEntity<List<Long>> getMessageIds(@AuthenticationPrincipal Jwt jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt.getTokenValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/api/messages/public/list",
                HttpMethod.GET,
                entity,
                List.class
        );

        List<?> rawList = response.getBody();
        List<Long> longList = rawList == null
                ? Collections.emptyList()
                : rawList.stream()
                .map(num -> ((Number) num).longValue())
                .toList();

        return ResponseEntity.ok(longList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getMessageById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt.getTokenValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/messages/public/" + id,
                HttpMethod.POST,
                entity,
                String.class
        );

        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping("/decrypt")
    public ResponseEntity<String> decryptMessage(@RequestBody Map<String, String> body, @AuthenticationPrincipal Jwt jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        String lambdaDecryptUrl = "https://j43ga1x7sa.execute-api.us-east-2.amazonaws.com/default/decrypt";

        ResponseEntity<String> response = restTemplate.exchange(
                lambdaDecryptUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        return ResponseEntity.ok(response.getBody());
    }

}
