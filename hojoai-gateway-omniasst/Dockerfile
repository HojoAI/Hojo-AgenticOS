# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Copy application file
COPY ./hojoai-gateway-omniasst-server/target/hojoai-gateway-omniasst-server-1.0.0-SNAPSHOT.jar ./app.jar

# Expose application port
EXPOSE 12001

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]