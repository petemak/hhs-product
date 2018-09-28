FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/product-0.0.1-SNAPSHOT-standalone.jar /product/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/product/app.jar"]
