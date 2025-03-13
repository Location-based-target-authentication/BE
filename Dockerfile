FROM cloudtype/jdk:17 as build

WORKDIR /app
COPY . .
WORKDIR /app/Location-based-target-authentication

# Create a custom build script
COPY <<EOF /app/build.sh
#!/bin/bash
cd /app/Location-based-target-authentication
chmod +x ./gradlew
./gradlew bootJar -x test --build-cache --parallel
EOF

RUN chmod +x /app/build.sh && /app/build.sh

FROM cloudtype/jre:17
WORKDIR /app
COPY --from=build /app/Location-based-target-authentication/build/libs/*.jar app.jar

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
