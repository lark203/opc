# 前端构建与运行镜像
# 阶段一：依赖安装与构建
FROM node:20-alpine AS builder

WORKDIR /app

# 启用 pnpm（lockfileVersion 9.0 需要 pnpm >= 9）
RUN corepack enable && corepack prepare pnpm@9.15.0 --activate

# 先拷贝依赖清单，利用 Docker 层缓存
COPY package.json pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile

# 拷贝源码并构建
# 敏感构建期环境变量(如锁屏密钥、RSA 私钥)通过 --build-arg 注入，切勿将真实密钥写入镜像
COPY . .
RUN pnpm build

# 阶段二：nginx 运行
FROM nginx:1.27-alpine

# 官方 nginx 镜像会在容器启动时将 /etc/nginx/templates/*.template 中的 ${BACKEND_API}
# 用环境变量替换，并生成到 /etc/nginx/conf.d/。后端地址可在运行时通过 -e 注入，
# 例如: docker run -e BACKEND_API=http://ruoyi-backend:8080 ...
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# 默认后端地址（运行时覆盖）
ENV BACKEND_API=http://localhost:8080

# 构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

# 继承官方 nginx 入口（自动完成 envsubst + 启动 nginx）
