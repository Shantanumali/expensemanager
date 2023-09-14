From tomcat:8-jre8 
COPY ./expensemanager-1.0.0.jar /usr/local/tomcat/webapps
COPY ./target/expensemanager-1.0.0.jar /usr/local/tomcat/webapps
