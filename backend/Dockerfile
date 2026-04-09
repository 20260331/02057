# 多阶段构建 - 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段（使用跨平台更稳定的非 Alpine 镜像）
FROM eclipse-temurin:17-jre

WORKDIR /app

# 设置时区
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata && \
    ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*

# 创建非 root 用户（兼容基础镜像已占用 uid/gid=1000 的情况）
RUN getent group spring >/dev/null || groupadd spring && \
    id -u spring >/dev/null 2>&1 || useradd -m -g spring -s /bin/bash spring

# 创建上传目录和备份目录
RUN mkdir -p /app/uploads /app/backups

# 从构建阶段复制 jar 文件
COPY --from=builder /build/target/*.jar app.jar

# 设置目录权限（在COPY之后）
RUN chown -R spring:spring /app

# 切换到非 root 用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# JVM 优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
