package com.cs6238.project2.s2dr.server.services;

import com.cs6238.project2.s2dr.server.dao.Dao;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class Service {

    private final Dao dao;

    @Inject
    public Service(Dao dao) {
        this.dao = dao;
    }

    public Map<String, String> getHelloMessage(Optional<Integer> userId) throws SQLException {
        String name;
        if (!userId.isPresent()) {
            name = "World";
        } else {
            name = dao.getUserName(userId.get());
        }
        String message = "Hello " + name + "!";
        return ImmutableMap.of("message", message);
    }
}
