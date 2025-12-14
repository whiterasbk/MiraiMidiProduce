#!/bin/bash
set -eo pipefail

# --- 环境变量设置 ---
# $1: IMAGE_TAG (传入的版本标签)
IMAGE_TAG="$1"
DOCKER_USER="whiterasbk" # 替换为您的 Docker Hub 用户名
IMAGE_NAME="miderproduce-service"
REPOSITORY="${DOCKER_USER}/${IMAGE_NAME}"

# --- 1. 执行 Gradle 构建 ---
echo "--- 1. 执行 Gradle 构建 (跳过测试) ---"
# 确保 gradlew 有执行权限
chmod +x gradlew

# 执行 service 模块的 buildFatJar 任务
./gradlew :service:buildFatJar -x test

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "❌ Gradle 构建失败!"
    exit 1
fi

# 检查 Jar 文件是否存在
FAT_JAR_PATH="service/build/libs/service-all.jar"
if [ ! -f "$FAT_JAR_PATH" ]; then
    echo "❌ 找不到构建的 Fat Jar 文件: $FAT_JAR_PATH"
    exit 1
fi
echo "✅ Fat Jar 构建成功: $FAT_JAR_PATH"

# --- 2. 准备 Docker 构建上下文 ---
echo "--- 2. 准备 Docker 构建上下文 ---"

# 复制 Fat Jar 到 Dockerfile 所在目录 (根目录)
cp "$FAT_JAR_PATH" ./service.jar
echo "✅ 已复制 service.jar"

# 复制 application.conf.default 文件
DEFAULT_CONF_PATH="service/src/main/resources/application.conf.default"
if [ -f "$DEFAULT_CONF_PATH" ]; then
    cp "$DEFAULT_CONF_PATH" ./application.conf.default
    echo "✅ 已复制 application.conf.default"
else
    echo "⚠️ 找不到 application.conf.default，继续构建..."
fi

# --- 3. 构建和标记 Docker 镜像 ---
echo "--- 3. 构建 Docker 镜像 ---"

# 使用传入的标签 $IMAGE_TAG 构建镜像
docker build -t "${REPOSITORY}:${IMAGE_TAG}" .

# 同时添加 latest 标签 (仅在 dev 分支推送时添加)
if [ "$GITHUB_REF_NAME" == "dev" ]; then
    docker tag "${REPOSITORY}:${IMAGE_TAG}" "${REPOSITORY}:latest"
    echo "✅ 已添加 latest 标签"
fi

# --- 4. 推送 Docker 镜像 ---
echo "--- 4. 推送 Docker 镜像 ---"

# 推送带版本的标签
docker push "${REPOSITORY}:${IMAGE_TAG}"
echo "✅ 镜像 ${REPOSITORY}:${IMAGE_TAG} 推送成功!"

# 如果是 dev 分支，推送 latest 标签
if [ "$GITHUB_REF_NAME" == "dev" ]; then
    docker push "${REPOSITORY}:latest"
    echo "✅ 镜像 ${REPOSITORY}:latest 推送成功!"
fi

echo "--- 🥳 Docker 镜像推送流程全部完成! ---"