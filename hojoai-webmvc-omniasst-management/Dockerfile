# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Copy application file
COPY ./hojoai-webmvc-omniasst-management-server/target/hojoai-webmvc-omniasst-management-server-1.0.0-SNAPSHOT.jar ./app.jar

# Expose application port
EXPOSE 12005

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]