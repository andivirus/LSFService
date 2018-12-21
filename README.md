# LSF Service

Software for providing an API for timetables

Samples parse websites created by LSF software (HIS eG)

## Installation

- ```./gradlew jar```
- ```./gradlew installDist```
- Create a 'plugins' folder in ./build/install/lsfserver/bin
- Copy plugin .jar files from their build directories into the plugins folder
- Run start script in ./build/install/lsfserver/bin depending on your OS
- Follow instructions for first time setup

## Structure

| Directory | Use |
|-----------|-----|
|./api | Library for writing plugins |
|./src/main | Main program |
|./HSWormsPlugin | Sample plugin for parsing LSF timetables of Hochschule Worms |
|./TUDortmundPlugin | Sample plugin for parsing LSF timetables of Technische Universität Dortmund |


## APIDOC

After starting the webserver the [Swagger](https://swagger.io)-generated API documentation is available at the ```/docs``` endpoint (e.g. ```http://localhost:8080/docs```)