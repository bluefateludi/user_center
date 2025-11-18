# 使用多阶段构建优化镜像大小
# 阶段1: 构建阶段
FROM maven:3.8.6-openjdk-8 AS builder

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml .

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 打包应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 阶段2: 运行阶段
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的 jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 设置时区为中国标准时间
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 暴露应用端口
EXPOSE 8081

# 设置 JVM 参数和启动命令
# -Dspring.profiles.active=prod 指定使用生产环境配置
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "app.jar"]
