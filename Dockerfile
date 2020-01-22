FROM openjdk:8-alpine

COPY target/uberjar/todo-app-two.jar /todo-app-two/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/todo-app-two/app.jar"]
