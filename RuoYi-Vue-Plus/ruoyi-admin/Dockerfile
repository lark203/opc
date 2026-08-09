# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-rocky:21.0.12-cds
# FROM bellsoft/liberica-openjdk-rocky:25.0.4-cds
# FROM findepi/graalvm:java21-native

LABEL maintainer="wangss"

RUN mkdir -p /ruoyi/server/logs \
    /ruoyi/server/temp \
    /ruoyi/skywalking/agent

WORKDIR /ruoyi/server

ENV SERVER_PORT=8080 SNAIL_JOB_PORT=28080 SNAIL_AI_PORT=38080 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}
# 暴露 snail 客户端端口 用于定时任务调度中心通信
EXPOSE ${SNAIL_JOB_PORT}
EXPOSE ${SNAIL_AI_PORT}

ADD ./target/ruoyi-admin.jar ./app.jar

SHELL ["/bin/bash", "-c"]

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} \
           -Dsnail-job.port=${SNAIL_JOB_PORT} \
           -Dsnail-ai.port=${SNAIL_AI_PORT} \
           # 应用名称 如果想区分集群节点监控 改成不同的名称即可
           #-Dskywalking.agent.service_name=ruoyi-server \
           #-javaagent:/ruoyi/skywalking/agent/skywalking-agent.jar \
           # 显式堆内存参数：避免容器内依赖 JVM 默认策略导致过小 OOM 或过大争抢宿主机资源
           # 可通过环境变量 JAVA_OPTS 追加/覆盖(如 -Xmx2g)以适配不同规格实例
           -Xms512m -Xmx1024m \
           -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m \
           -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/ruoyi/server/logs \
           -XX:+UseZGC ${JAVA_OPTS} \
           -jar app.jar
