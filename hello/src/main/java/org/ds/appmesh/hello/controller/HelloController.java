package org.ds.appmesh.hello.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static Logger logger = LoggerFactory.getLogger(HelloController.class);

    private static String SERVICE_INSTANCE = System.getenv("SERVICE_INSTANCE");

    private static String greeting = String.format("Greeting from service instance %s", SERVICE_INSTANCE);



    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        logger.info("get on /hello for instance {}...", SERVICE_INSTANCE);
        return new ResponseEntity<>(greeting, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("get on /health for instance {}...", SERVICE_INSTANCE);
        return new ResponseEntity<>("hello UP", HttpStatus.OK);
    }
}
