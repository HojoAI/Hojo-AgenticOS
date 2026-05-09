# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Copy application file
COPY ./hojoai-grpc-central-provider-server/target/hojoai-grpc-central-provider-server-1.0.0-SNAPSHOT.jar ./app.jar

# Expose application port
EXPOSE 11003
EXPOSE 11103

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]