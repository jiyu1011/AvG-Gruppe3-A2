# Commands zum Starten/ Stoppen des Docker Images/ Containers
- docker run --detach --name apache-artemis -p 61616:61616 -p 8161:8161 --rm apache/activemq-artemis:latest-alpine
- docker start apache-artemis
- docker stop apache-artemis

# Command zum Starten des Servers
- mvn spring-boot:run