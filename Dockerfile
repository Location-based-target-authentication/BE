FROM cloudtype/jdk:17 as build

WORKDIR /app
COPY Location-based-target-authentication Location-based-target-authentication/
WORKDIR /app/Location-based-target-authentication
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test --build-cache --parallel

FROM cloudtype/jre:17
WORKDIR /app
COPY --from=build /app/Location-based-target-authentication/build/libs/*.jar app.jar

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
