# Project 2 - S2DR

### System Requirements
* [Gradle](http://gradle.org/) must be installed on the system that will run the S2DR servlet

### Build/Run
To build and run the Java servlet, from the project root run `gradle appRun`. That's it! The servlet is now accessible
from `http://localhost:8080/`

### Run Test Case Files
**TODO:** once we start making test files/scripts, we will need to update this to include instruction on how to run.

### Libraries Used
* [Jersey](https://jersey.java.net/) for building RESTful services
* [Jackson](http://wiki.fasterxml.com/JacksonHome) for serializing and deserializing JSON
* [Guava](https://github.com/google/guava/wiki) for general Java programming
* [slf4j](http://www.slf4j.org/) for logging. TODO enable loggin
* [Guice](https://github.com/google/guice/wiki/Motivation) for dependency injection and servlet configuration. This
*should* be fairly invisible for 99% of the development
* [JDBC](https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html) for database interaction