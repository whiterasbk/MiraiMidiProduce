# 构建 service 服务镜像

FROM whiterasbk/musescore:x86_64-3.6.2

ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

RUN apt update && \
    apt install -y \
        openjdk-17-jre \
        ffmpeg \
        timidity \
        fluid-soundfont-gm \
        --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY service.jar /app/service.jar

RUN mkdir -p /app/config /app/config.default

COPY application.conf /app/config/application.conf

COPY application.conf /app/config.default/application.conf

RUN mkdir -p /app/tmp /app/generated

EXPOSE 5827

# 创建启动脚本
RUN cat > /app/entrypoint.sh << 'EOF'
#!/bin/bash
set -e

echo "=== Starting miderproduce-service ==="

# 检查 config 目录是否为空
if [ -z "$(ls -A /app/config 2>/dev/null)" ]; then
    echo "Config directory is empty, copying default configuration..."
    
    # 复制默认配置
    if [ -d "/app/config.default" ] && [ "$(ls -A /app/config.default)" ]; then
        cp -rv /app/config.default/* /app/config/
        echo "✅ Default configuration copied successfully"
    else
        echo "⚠️ Warning: No default configuration found"
    fi
else
    echo "✅ Using existing configuration"
fi

# 显示配置文件信息
echo ""
echo "Configuration files:"
ls -lh /app/config/

# 检查配置文件是否存在
if [ ! -f "/app/config/application.conf" ]; then
    echo "⚠️ Warning: application.conf not found! use default"
fi

echo ""
echo "=== Starting Java application ==="
echo "Java version:"
java -version

echo ""
echo "Starting service with config: /app/config/application.conf"

# 启动应用
exec java -jar /app/service.jar /app/config/application.conf
EOF

RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]