# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Copy application file
COPY ./hojoai-webmvc-central-account-server/target/hojoai-webmvc-central-account-server-1.0.0-SNAPSHOT.jar ./app.jar

# Expose application port
EXPOSE 11002

# Run the application. Aliyun credentials (SMS 等) 请通过环境变量或 Nacos 配置注入，勿在镜像中硬编码 AK/SK。
# 例如：ALIBABA_CLOUD_ACCESS_KEY_ID、ALIBABA_CLOUD_ACCESS_KEY_SECRET（与 credentials-java 约定一致）
ENTRYPOINT ["java", "-jar", "app.jar"]
