# Project 2 - S2DR

### System Requirements
* JDK 8 for the server
* Python 2.7 for the client test cases
* pip for installing a python library

### Build/Run Server
The S2DR-Server is built as a Java servlet, and is run on a Tomcat 8 servlet container. The servlet container itself
is run _inside_ the [Gretty Plugin](https://akhikhl.github.io/gretty-doc/Feature-overview.html) for Gradle.
Additionally, we take advantage of the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
(a Gradle instance that is contained within the project). What this means is that the server can be run without
installing anything on the host machine except a valid JDK (JDK 8 for our project). To run the server, simply run (from
the project root directory):
```
./gradlew appRun
```

The S2DR-Server will then be accessible at `https://localhost:8443`. The server can be stopped with `ctrl+c`.

If you would like to view sql queries in the server logs, in `${projectRoor}/src/main/resources/logback.xml`, change
```
<root level="info">
    <appender-ref ref="STDOUT" />
</root>
```
to
```
<root level="debug">
    <appender-ref ref="STDOUT" />
</root>
```
\* note this will require a servlet restart

### Build/Run Client
First, ensure that you have pip installed on your machine. Once you have this installed, install the `requests` library
used in the client tests.
```
pip install requests
```
You _may_ need sudo to install that. Once has successfully installed (ensure that the server is running), run the
python test scripts (from the project root directory):
```
cd src/test
python main.py
```
The python test script will then begin to interact with the server instance running in the Gretty plugin. You can watch
the server and client logs to see the interaction.

