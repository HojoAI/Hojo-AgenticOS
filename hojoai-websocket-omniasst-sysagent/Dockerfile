# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Install system dependencies for DingRTC SDK
RUN yum install -y libXcomposite libXdamage libXfixes mesa-libGL libatomic mesa-libGLU libX11 libXext && yum clean all

# Copy application file
COPY ./hojoai-websocket-omniasst-sysagent-server/target/hojoai-websocket-omniasst-sysagent-server-1.0.0-SNAPSHOT.jar ./app.jar

# Copy shared libraries
COPY ./hojoai-websocket-omniasst-sysagent-server/lib/x86_64/*.so ./lib/x86_64/

# Set library path for DingRTC SDK
ENV LD_LIBRARY_PATH=/workspace/lib/x86_64:$LD_LIBRARY_PATH

# Expose application port
EXPOSE 12003

# Run the application
ENTRYPOINT ["java", "-Djava.library.path=/workspace/lib/x86_64", "-jar", "app.jar"]