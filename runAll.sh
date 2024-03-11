./gradlew clean build
java -jar microservices/product/build/libs/product-1.0.0-SNAPSHOT.jar &
java -jar microservices/composite/build/libs/composite-1.0.0-SNAPSHOT.jar
