FROM cloudtype/jdk:17 as build

WORKDIR /app
COPY . .
WORKDIR /app/Location-based-target-authentication

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test -x clean --build-cache --parallel
RUN mkdir -p /build/libs
RUN cp build/libs/*.jar /build/libs/

FROM cloudtype/jre:17
WORKDIR /app
COPY --from=build /build/libs/*.jar app.jar

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
