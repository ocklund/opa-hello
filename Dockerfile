FROM adoptopenjdk/openjdk11:jdk-11.0.9_11-alpine
ADD target/opa-hello-0.0.1-SNAPSHOT.jar /opt/app.jar
ENTRYPOINT java -jar /opt/app.jar --opa.host=opa