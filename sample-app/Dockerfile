FROM amazoncorretto:21

COPY sample-app/target/*.jar /home/app/app.jar

EXPOSE 8080 8080
ENTRYPOINT ["java", "-jar", "/home/app/app.jar"]
