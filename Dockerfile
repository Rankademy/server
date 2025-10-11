FROM amazoncorretto:21

ARG versionCode
COPY ./build/libs/rankademy-${versionCode}.jar app.jar

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "./app.jar"]
