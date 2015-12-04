# Project 2 - S2DR

### System Requirements
* JDK 8 for the server

### Build/Run Server
The S2DR-Server is built as a Java servlet, and is run on a Tomcat 8 servlet container. The servlet container itself
is run _inside_ the [Gretty Plugin](https://akhikhl.github.io/gretty-doc/Feature-overview.html) for Gradle.
Additionally, we take advantage of the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
(a Gradle instance that is contained within the project). What this means is that the server can be run without
installing anything on the host machine except a valid JDK (JDK 8 for our project). To run the server, simply run (from
the project root directory):
* `./gradlew appRun`

The S2DR-Server will then be accessible at `https://localhost:8443`. The server can be stopped with `ctrl+C`.

### Build/Run Client
**TODO:** once we start making test files/scripts, we will need to update this to include instruction on how to run.

