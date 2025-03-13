FROM cloudtype/jdk:17 as build

WORKDIR /app

# Debug: List contents before copy
RUN ls -la

COPY . .

# Debug: List contents after copy
RUN ls -la

# Debug: Check if we're in the right directory
RUN pwd

# Debug: List contents of Location-based-target-authentication
RUN ls -la Location-based-target-authentication

WORKDIR /app/Location-based-target-authentication

# Debug: Verify current directory
RUN pwd

# Debug: List contents in current directory
RUN ls -la

# Debug: Check gradle wrapper
RUN ls -la gradlew || echo "gradlew not found"

# Debug: Check build.gradle
RUN ls -la build.gradle || echo "build.gradle not found"

# Debug: List available gradle tasks
RUN chmod +x ./gradlew
RUN ./gradlew tasks --all

# Actual build command
RUN ./gradlew bootJar -x test --build-cache --parallel

FROM cloudtype/jre:17
WORKDIR /app
COPY --from=build /app/Location-based-target-authentication/build/libs/*.jar app.jar

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
