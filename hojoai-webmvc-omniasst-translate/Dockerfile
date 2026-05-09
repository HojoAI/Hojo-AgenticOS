# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Copy application file
COPY ./hojoai-webmvc-omniasst-translate-server/target/hojoai-webmvc-omniasst-translate-server-1.0.0-SNAPSHOT.jar ./app.jar

# Expose application port
EXPOSE 12006

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]