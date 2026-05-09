# 构建前请在仓库根目录执行: mvn clean package -DskipTests
# 可通过构建参数替换基础镜像，例如: docker build --build-arg BASE_IMAGE=eclipse-temurin:21-jre .

ARG BASE_IMAGE=dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21
FROM ${BASE_IMAGE}

WORKDIR /workspace

ENV TZ=Asia/Shanghai

COPY ./hojoai-webmvc-omniasst-focus-server/target/hojoai-webmvc-omniasst-focus-server-1.0.0-SNAPSHOT.jar ./app.jar

EXPOSE 12008

ENTRYPOINT ["java", "-Djava.library.path=/workspace/lib/x86_64", "-jar", "app.jar"]
