FROM maven:3.6.3-openjdk-11
COPY ./target/expensemanager-1.0.0.jar /usr/local/tomcat/webapps
