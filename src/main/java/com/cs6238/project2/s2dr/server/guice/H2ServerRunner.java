package com.cs6238.project2.s2dr.server.guice;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;

public class H2ServerRunner extends AbstractExecutionThreadService {

    private static final Logger LOG = LoggerFactory.getLogger(H2ServerRunner.class);

    private final Connection connection;

    boolean isRunning = false;

    @Inject
    public H2ServerRunner(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected void startUp() {
        try {
            Server.startWebServer(connection);
            isRunning = true;
            this.run();
        } catch (Exception e) {
            isRunning = false;
            LOG.error("Unable to start the H2 web server. {}", e);
        }
    }

    @Override
    protected void run() throws Exception {
        while (isRunning && !connection.isClosed()) {
            isRunning = true;
        }
    }

    @Override
    protected void triggerShutdown() {
        isRunning = false;
    }
}

