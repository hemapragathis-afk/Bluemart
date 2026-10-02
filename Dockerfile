FROM tomcat:9.0-jdk17
COPY target/bluemart.war /usr/local/tomcat/webapps/bluemart.war
EXPOSE 8080