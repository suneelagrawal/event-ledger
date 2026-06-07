package com.example.accountservice.util;

import java.time.Instant;

public class JsonLogger {

    private static final String SERVICE = "account-service";

    public static void info(String traceId, String message) {

        System.out.println(
                "{"
                        + "\"timestamp\":\"" + Instant.now() + "\","
                        + "\"level\":\"INFO\","
                        + "\"service\":\"" + SERVICE + "\","
                        + "\"traceId\":\"" + traceId + "\","
                        + "\"message\":\"" + message + "\""
                        + "}"
        );
    }
}