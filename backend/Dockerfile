# 构建阶段
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 创建上传目录
RUN mkdir -p uploads

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
