# Use an official Maven/Java runtime as a parent image
FROM maven:3.6.3-openjdk-11 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file and download dependencies
COPY pom.xml .

# Copy the project source code
COPY src src

# Build the JAR file
RUN mvn package

COPY ./target/*jar /app/ROOT.jar

# Expose the port that Tomcat will run on
EXPOSE 8080

# Start Tomcat and deploy the JAR file
ENTRYPOINT ["java", "-jar", "ROOT.jar"]
