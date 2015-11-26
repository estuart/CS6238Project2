package com.cs6238.project2.s2dr.server.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseModule extends AbstractModule {

    private static final String DATABASE_URL = "jdbc:h2:mem:s2dr;";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String SQL_SCRIPT_NAME = "s2dr.sql";
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseModule.class);

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    private Connection getDatabaseConnection() throws SQLException, ClassNotFoundException {
        Class.forName(H2_DRIVER);

        // create the database to the in-memory H2 database, and run the sql script to
        // create the schema.
        return DriverManager.getConnection(
                DATABASE_URL + "INIT=runscript from'classpath:/" + SQL_SCRIPT_NAME + "'");
    }
}