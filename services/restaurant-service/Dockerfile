FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY src src

RUN ./gradlew build -x test
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.doordash.restaurantservice.RestaurantServiceApplication"]

EXPOSE 8082

LABEL maintainer="DoorDash Backend Team"
LABEL version="1.0"
LABEL description="Restaurant Service for DoorDash Backend"