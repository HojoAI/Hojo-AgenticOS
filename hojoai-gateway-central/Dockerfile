# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Copy application file
COPY ./hojoai-gateway-central-server/target/hojoai-gateway-central-server-1.0.0-SNAPSHOT.jar ./app.jar

# Expose application port
EXPOSE 11001

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]