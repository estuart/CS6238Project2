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

    // change the database url to "jdbc:h2:mem:s2dr;" and uncomment the return statement
    // in the provider that contains the "INIT=runscript..." to use an in-memory database
    // that is created from the `s2dr.sql` script
    private static final String DATABASE_URL = "jdbc:h2:s2dr;";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String SQL_SCRIPT_NAME = "s2dr.sql";

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    private Connection getDatabaseConnection() throws SQLException, ClassNotFoundException {
        Class.forName(H2_DRIVER);

        // Right now, our H2 database is persisted at `${projectRoot}/s2dr.h2.db`. Changes to that file
        // should not be committed to the repository. If for whatever reason you feel that you need to
        // start over with your machine-local database, you should:
        //      1.) make sure the servlet is not running
        //      2.) delete the `${projectRoot}/s2dr.h2.db` file
        //      3.) un-comment this return statement and comment out the one below
        //      4.) start the servlet
        //      5.) login (at least one interaction with the database is needed for H2 to "create" the database.
        //              Logging in will do it.)
        //      6.) stop the servlet
        //      7.) comment this return statement back out and uncomment the one below
        // This will re-create the database following the schema that is defined in the `s2dr.sql` script
//        return DriverManager.getConnection(DATABASE_URL + "INIT=runscript from'classpath:/" + SQL_SCRIPT_NAME + "'");

        return DriverManager.getConnection(DATABASE_URL);
    }
}
