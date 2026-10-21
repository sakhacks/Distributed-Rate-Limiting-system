package com.jollyapp.distributedratelimitingsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jollyapp.distributedratelimitingsystem.service.TokenBucketRateLimiter;

@RestController
public class TestController {

    @Autowired
    private TokenBucketRateLimiter rateLimiter;

    @GetMapping("/test")
    public String test(@RequestParam String apiKey) {
        boolean allowed = rateLimiter.isAllowed(apiKey, 5, 1.0); // capacity=5, refill=1 token/sec
        return allowed ? "ALLOWED" : "REJECTED";
    }
}
