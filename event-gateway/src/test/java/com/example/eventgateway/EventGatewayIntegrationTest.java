package com.example.eventgateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventGatewayIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void endToEndGatewayToAccountServiceFlow() throws Exception {

        // IMPORTANT:
        // Account Service must already be running on localhost:8081

        String json = """
                {
                  "eventId":"evt-int-001",
                  "accountId":"acct-int",
                  "type":"CREDIT",
                  "amount":100.00,
                  "currency":"USD",
                  "eventTimestamp":"2026-06-07T10:00:00Z"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Trace-Id", "integration-trace-001");

        HttpEntity<String> request =
                new HttpEntity<>(json, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "http://localhost:" + port + "/events",
                        request,
                        Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(
                "evt-int-001",
                response.getBody().get("eventId"));

        // Give Account Service a moment to persist
        Thread.sleep(500);

        ResponseEntity<Map> balanceResponse =
                restTemplate.getForEntity(
                        "http://localhost:8081/accounts/acct-int/balance",
                        Map.class);

        assertEquals(HttpStatus.OK, balanceResponse.getStatusCode());

        assertNotNull(balanceResponse.getBody());

        Number balance =
                (Number) balanceResponse.getBody().get("balance");

        assertEquals(100.0, balance.doubleValue());
    }
}