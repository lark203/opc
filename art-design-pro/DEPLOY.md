# 前端生产部署说明（art-design-pro）

采用「Node 构建 → nginx 托管静态资源 + 反向代理后端」的两阶段镜像，前后端**同源部署**（nginx 同时提供页面与 `/prod-api` ），因此浏览器不会触发跨域，前序 CORS 收敛与 OSS 图片预览代理均依赖此同源前提。

## 构建镜像

```bash
# 在 art-design-pro 目录下
docker build -t art-design-pro:latest .
```

### 注入敏感构建变量（重要）

`VITE_*` 在 `vite build` 时读取，必须于构建期注入，切勿将真实密钥写入镜像或提交到仓库：

```bash
docker build \
  --build-arg VITE_LOCK_ENCRYPT_KEY=<真实锁屏密钥> \
  --build-arg VITE_APP_RSA_PRIVATE_KEY=<真实RSA私钥> \
  -t art-design-pro:latest .
```

未注入时，`.env.production` 中的 `<SET_VIA_ENV_OR_CI>` 占位符会进入产物，导致锁屏解密、响应解密等功能不可用。

## 运行容器

```bash
docker run -d -p 80:80 \
  -e BACKEND_API=http://<后端服务名或地址>:8080 \
  --name art-web art-design-pro:latest
```

- `BACKEND_API`：后端地址，nginx 在容器启动时写入 `/prod-api` 反代。生产通常为 compose 服务名（如 `http://ruoyi-backend:8080`）。
- 页面访问 `http://<宿主机>:80/`，API 走同源 `/prod-api`。

## nginx 关键配置（见 `nginx.conf.template`）

- `/assets/` 带内容哈希，缓存 1 年（`immutable`）；`index.html` 不缓存，保证发版即时生效。
- `/prod-api/` 反代后端，并开启 gzip。
- SSE（`/prod-api/resource/message`）关闭缓冲、保持长连接；若改用 WebSocket 传输，需调整 `proxy_set_header Connection` 为 `upgrade` 并补充 `Upgrade` 头。

## 同源部署拓扑

```
浏览器 ──http://host/──▶ nginx(:80)
                       ├─ 静态资源(/, /assets/)  ← dist
                       └─ /prod-api/*  ──proxy──▶ 后端(:8080) ──▶ MySQL/Redis/MinIO
```

后端 `ruoyi-admin` 同样提供 Dockerfile，二者通过 `BACKEND_API` 串联即可组成完整部署。
