FROM openjdk:8-jre-alpine
RUN mkdir /app
WORKDIR /app
ADD sharycar-recommendations-api /app
EXPOSE 8082
CMD ["java", "-jar", "sharycar-recommendations-api-1.0-SNAPSHOT.jar"]
