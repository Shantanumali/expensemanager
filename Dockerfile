FROM maven:3.6.3-openjdk-11
COPY ./webapp.war /usr/local/tomcat/webapps
