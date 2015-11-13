package com.cs6238.project2.s2dr.server.services;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

public class SystemService {

    public Map<String, String> getHelloMessage(Optional<String> name) {
        String message = "Hello " + name.orElse("World") + "!";
        return ImmutableMap.of("message", message);
    }
}
