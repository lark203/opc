# RuoYi-Vue-Plus + art-design-pro 极简 Docker 部署手册

> 范围：只部署 **ruoyi-admin（后端）** 与 **art-design-pro（前端）**。
> MySQL / Redis 是**已有的外部服务**，不在容器里；域名直接带端口访问，**不需要 Nginx**。

---

## 0. 先搞清楚两个概念（避免 confusion）

### Dockerfile 是「配方」，docker-compose 是「运行配置」

```
Dockerfile          →  描述怎么把 jar / 前端打成一个镜像
                       里面 ENTRYPOINT java -jar 表示：容器一启动就跑这个服务
docker-compose.yml  →  描述怎么把这个镜像跑成容器（端口映射 / 环境变量 / 重启策略）
```

两者不重复：**Dockerfile 让服务能跑，compose 决定它跑在哪个端口、用什么环境**。
`java -jar` 确实就是启动服务——但只在你 `docker run` / `docker compose up` 之后。

### 源码不需要传到服务器

镜像在**你有源码的地方**（开发机或 CI）构建，再把*镜像*传到服务器：

```bash
# 开发机：导出镜像
docker save ruoyi-backend:1.0 ruoyi-frontend:1.0 -o ruoyi-images.tar
# 传到服务器
scp ruoyi-images.tar root@140.143.250.175:/opt/ruoyi/
# 服务器：导入镜像
docker load -i /opt/ruoyi/ruoyi-images.tar
```

> 服务器上只需要镜像，不需要 Maven、Node、源码。

---

## 1. 目标架构（极简）

```
浏览器
 ├─ adminui.tx.bestuav.cn:18880  →  [前端容器 nginx :80]  →  /prod-api 反代 → 后端
 └─ admin.tx.bestuav.cn:18000    →  [后端容器 :18000]  ─┬─→ 外部 MySQL (mysql.tx.bestuav.cn:13306)
                                                       └─→ 外部 Redis (redis.tx.bestuav.cn:16379)
```

- 只有 **2 个容器**：backend、frontend。
- MySQL / Redis 是你已有的服务（`application-prod.yml` 里配的域名端口），容器通过 Docker 默认 DNS 直接访问。
- 域名直连端口，没有 Nginx 层。

---

## 2. 在开发机构建镜像

### 2.1 后端

```bash
cd RuoYi-Vue-Plus
mvn clean package -P prod -DskipTests        # 产物 ruoyi-admin/target/ruoyi-admin.jar
cd ruoyi-admin
docker build -t ruoyi-backend:1.0 .           # 上下文就是 ruoyi-admin/，Dockerfile 取 ./target/ruoyi-admin.jar
```

### 2.2 前端

> 构建前按需填写 `art-design-pro/.env.production` 里的
> `VITE_LOCK_ENCRYPT_KEY` / `VITE_APP_RSA_PRIVATE_KEY`（否则锁屏/加解密不可用）。
> `VITE_API_URL = /prod-api` 已就绪，后端地址在运行时注入，无需改。

```bash
cd art-design-pro
docker build -t ruoyi-frontend:1.0 .
```

### 2.3 把镜像传到服务器

见 §0 的 `docker save / scp / docker load`。

---

## 3. 在服务器运行（只两个服务）

把 `docker-compose.yml`（见仓库 `RuoYi-Vue-Plus/docker-compose.yml`，已精简）放到服务器 `/opt/ruoyi/`：

```bash
cd /opt/ruoyi
docker compose up -d
docker compose ps
docker compose logs -f backend      # 确认连上外部 MySQL/Redis、应用起来了
```

`docker-compose.yml` 内容只有 backend + frontend：

```yaml
services:
  backend:
    image: ruoyi-backend:1.0
    container_name: ruoyi-backend
    environment:
      TZ: Asia/Shanghai
      SERVER_PORT: 18000
      ART_CORS_ALLOWED_ORIGINS: "https://adminui.tx.bestuav.cn:18880,http://adminui.tx.bestuav.cn:18880"
      ART_WS_ALLOWED_ORIGINS: "https://adminui.tx.bestuav.cn:18880,http://adminui.tx.bestuav.cn:18880"
    ports:
      - "18000:18000"
    restart: unless-stopped

  frontend:
    image: ruoyi-frontend:1.0
    container_name: ruoyi-frontend
    environment:
      BACKEND_API: "http://ruoyi-backend:18000"   # 同机 compose 服务名，/prod-api 内部转发，不走公网
    ports:
      - "18880:80"
    restart: unless-stopped
```

> `BACKEND_API` 用同机 compose 服务名 `ruoyi-backend:18000`：浏览器在 `adminui.tx.bestuav.cn:18880` 发起 `/prod-api` 请求，
> 前端 nginx 按 `nginx.conf.template` 在同机内部转发到后端容器，同源无跨域问题，且不走公网。

---

## 4. 初始化外部数据库（一次性）

MySQL 是外部服务，需手动建库、建用户、导表结构（在能连到 `mysql.tx.bestuav.cn:13306` 的机器上执行）：

```bash
mysql -h mysql.tx.bestuav.cn -P 13306 -u root -p <<'SQL'
CREATE DATABASE IF NOT EXISTS `ry-art`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS 'ry-art'@'%' IDENTIFIED BY 'DnCJDCmnFdrWaaYG';
GRANT ALL PRIVILEGES ON `ry-art`.* TO 'ry-art'@'%';
FLUSH PRIVILEGES;
SQL

mysql -h mysql.tx.bestuav.cn -P 13306 -u root -p ry-art \
  < RuoYi-Vue-Plus/script/sql/ry_vue.sql
mysql -h mysql.tx.bestuav.cn -P 13306 -u root -p ry-art \
  < RuoYi-Vue-Plus/script/sql/ry_system_config.sql   # 系统配置功能依赖
```

> 密码 `DnCJDCmnFdrWaaYG`、库名 `ry-art` 来自 `application-prod.yml`，必须一致。

---

## 5. 验证

### 5.1 基础连通性（宿主机，端口已发布）

```bash
# 后端本身健康（直连 18000）
curl -i http://127.0.0.1:18000/actuator/health

# 前端页面（直连 18880）
curl -i http://127.0.0.1:18880/
```

### 5.2 验证 nginx 内部转发（核心链路）

前端 nginx 把 `/prod-api` 反代到同机后端服务名 `ruoyi-backend:18000`。
下面这条命令走完整链路 `18880 → /prod-api → ruoyi-backend:18000`，能通即说明转发 + 服务名 DNS 全部 OK：

```bash
# 完整链路：经前端 nginx 反代到后端
curl -i http://127.0.0.1:18880/prod-api/actuator/health
```

期望：返回 `200`，body 含 `"status":"UP"`。

### 5.3 进容器看生成的 nginx 配置

确认 `${BACKEND_API}` 已被 envsubst 正确替换（不应再看到 `${BACKEND_API}` 字面量）：

```bash
docker compose exec frontend cat /etc/nginx/conf.d/default.conf | grep -A2 "prod-api"
# 期望看到：proxy_pass http://ruoyi-backend:18000/;

# 确认前端容器能解析并直连后端服务名
docker compose exec frontend wget -qO- http://ruoyi-backend:18000/actuator/health
```

> 说明：官方 nginx 镜像只替换「环境变量名」列表中的变量，`$host`/`$scheme` 等 nginx 变量会原样保留，不会被清空。

### 5.4 浏览器验证

浏览器访问 `http://adminui.tx.bestuav.cn:18880` → 登录页 → 默认账号 `admin / admin123`。
能打开登录页且验证码正常加载，即代表 `/prod-api` 转发到后端成功。

> 记得在 DNS 控制台把 `admin` / `adminui` 两条 A 记录指向 `140.143.250.175`，
> 并在腾讯云安全组放行 `18000`、`18880`（以及 `22` SSH）。

### 5.5 验证失败排查

按 §5.1~5.3 哪一步失败，对照处理：

**A. 5.1 直连 `18000/actuator/health` 失败（后端没起来）**
```bash
docker compose ps                 # 看 backend 状态是否 Up
docker compose logs backend       # 看启动报错
```
- 报连不上 MySQL/Redis：`mysql.tx.bestuav.cn` / `redis.tx.bestuav.cn` 在容器内解析不到。
  在服务器上 `nslookup mysql.tx.bestuav.cn` 确认能解析；不能则在 compose 的 backend 服务加：
  ```yaml
  extra_hosts:
    - "mysql.tx.bestuav.cn:<MySQL真实IP>"
    - "redis.tx.bestuav.cn:<Redis真实IP>"
  ```
  或加 `dns: ["<内网DNS>"]`。
- 报 `ry-art` 库不存在 / 表不存在：回到 §4 执行建库 + 导入 SQL。
- 报端口占用：宿主机 18000 被占，`lsof -i:18000` 查杀。

**B. 5.1 直连 `18880/` 失败（前端没起来）**
```bash
docker compose logs frontend       # 看 nginx 是否启动、有无配置语法错
docker compose exec frontend nginx -t   # 校验 nginx 配置语法
```
- `nginx -t` 报 `${BACKEND_API}` 未替换或语法错：确认镜像是官方 nginx 且模板在 `/etc/nginx/templates/`；重建前端镜像。
- 18880 被占用：`lsof -i:18880` 查杀或改映射端口。

**C. 5.2 `18880/prod-api/actuator/health` 失败（nginx 转发断链）**
- 返回 `502 Bad Gateway`：前端容器连不上 `ruoyi-backend:18000`。
  ```bash
  docker compose exec frontend wget -qO- http://ruoyi-backend:18000/actuator/health
  ```
  - 上一条也失败 → 服务名没解析或服务没起：确认两个服务在同一 compose 网络（默认会）、`container_name: ruoyi-backend` 拼写一致；`docker compose logs backend` 看是否真起来了。
  - 上一条成功但 5.2 失败 → nginx 反代配置错：执行 5.3 看 `proxy_pass` 是否真的是 `http://ruoyi-backend:18000/`；若仍显示 `${BACKEND_API}`，说明 envsubst 没跑，重建前端镜像。
- 返回 `404`：后端起来了但路径不对。检查后端 `context-path`（应为 `/`），以及请求是否多了/少了 `/prod-api` 前缀。
- 返回跨域 `CORS` 报错：浏览器里才会出现（curl 不触发）。确认 backend 的 `ART_CORS_ALLOWED_ORIGINS` 含 `http://adminui.tx.bestuav.cn:18880`（注意带端口）。

**D. 5.4 浏览器打不开 / 验证码不显示**
- 页面都打不开：检查 DNS A 记录是否生效（`ping adminui.tx.bestuav.cn` 应解析到 `140.143.250.175`）、安全组是否放行 `18880`、服务器防火墙（iptables/firewalld）是否挡住。
- 页面能开但验证码不显示 / 登录转圈：基本是 C 类的 `/prod-api` 转发问题，按 5.2/5.3 排查。

---

## 6. 常见问题

| 现象 | 原因 | 处理 |
|------|------|------|
| 后端连不上 MySQL/Redis | `mysql.tx.bestuav.cn` 在容器内解析不到 | 在服务器上 `nslookup mysql.tx.bestuav.cn` 确认能解析；不能则在 compose 加 `extra_hosts` 或 `dns:` |
| 登录报错 / 验证码不出来 | 前端没连到后端 | 确认 `BACKEND_API` 指向可访问的后端地址；看 `docker compose logs frontend` |
| 跨域被拒 | CORS 白名单没带端口 | `ART_CORS_ALLOWED_ORIGINS` 必须含 `:18880` |
| 后端启动失败 | 外部 MySQL 没建 `ry-art` 库/表 | 执行 §4 初始化 |

---

## 7. 部署验证检查清单

> 部署时逐项打勾；任一项不过，跳到对应章节排查。

### 阶段一：构建与传镜像（开发机）
- [ ] 后端打包成功：`mvn clean package -P prod -DskipTests`，产物 `ruoyi-admin/target/ruoyi-admin.jar` 存在
- [ ] 后端镜像构建：`docker build -t ruoyi-backend:1.0 ./ruoyi-admin` 成功
- [ ] 前端镜像构建：`docker build -t ruoyi-frontend:1.0 ./art-design-pro` 成功
- [ ] 前端敏感变量已填：`.env.production` 中 `VITE_LOCK_ENCRYPT_KEY` / `VITE_APP_RSA_PRIVATE_KEY` 非占位符
- [ ] 镜像已传到服务器：`docker save` → `scp` → `docker load`，`docker images` 能看到两个镜像

### 阶段二：服务器准备
- [ ] Docker 已安装：`docker -v` / `docker compose version` 正常
- [ ] `docker-compose.yml` 已放到服务器（只含 backend + frontend）
- [ ] 外部 MySQL 已初始化：`ry-art` 库 + `ry-art` 用户 + 导入 `ry_vue.sql`（+ `ry_system_config.sql`）（§4）
- [ ] DNS：控制台 `admin` / `adminui` A 记录指向 `140.143.250.175`
- [ ] 安全组放行：`18000`、`18880`、`22`（§5.4）

### 阶段三：启动
- [ ] `docker compose up -d` 成功
- [ ] `docker compose ps` 两个容器状态 `Up`
- [ ] `docker compose logs backend` 无连接 MySQL/Redis 报错、应用启动完成
- [ ] `docker compose logs frontend` 无 nginx 配置语法错

### 阶段四：验证（§5）
- [ ] 5.1 直连后端：`curl 127.0.0.1:18000/actuator/health` 返回 `200` + `UP`
- [ ] 5.1 直连前端：`curl 127.0.0.1:18880/` 返回 HTML
- [ ] 5.2 转发链路：`curl 127.0.0.1:18880/prod-api/actuator/health` 返回 `200` + `UP`
- [ ] 5.3 配置确认：`docker compose exec frontend cat .../default.conf` 中 `proxy_pass http://ruoyi-backend:18000/`，无 `${BACKEND_API}` 字面量
- [ ] 5.3 服务名可达：`docker compose exec frontend wget ... http://ruoyi-backend:18000/actuator/health` 成功
- [ ] 5.4 浏览器：`http://adminui.tx.bestuav.cn:18880` 打开登录页，验证码正常，可用 `admin/admin123` 登录

### 阶段五：失败回溯入口
- [ ] 后端起不来 → §5.5 A / §6
- [ ] 前端起不来 → §5.5 B
- [ ] `/prod-api` 转发失败 → §5.5 C
- [ ] 浏览器异常 → §5.5 D

---

---

## 8. 自动化验证脚本

把 §5 的验证步骤做成一键脚本 `verify-deploy.sh`（位于仓库 `RuoYi-Vue-Plus/verify-deploy.sh`，与 `docker-compose.yml` 同目录）。

**使用：**
```bash
cd /opt/ruoyi                      # docker-compose.yml 所在目录
chmod +x verify-deploy.sh
./verify-deploy.sh
```

**脚本会依次检查并输出 `[ PASS ]` / `[ FAIL ]`：**
1. 后端 / 前端容器是否运行中
2. 直连后端 `18000/actuator/health` → 200
3. 直连前端 `18880/` → 200
4. 转发链路 `18880/prod-api/actuator/health` → 200（验证 nginx 内部转发）
5. 前端容器内 `default.conf` 的 `proxy_pass` 已替换成 `http://ruoyi-backend:18000/`（无 `${BACKEND_API}` 字面量）
6. 前端容器内能否解析并直连 `ruoyi-backend:18000`

**末尾打印汇总**（如 `3 通过 / 0 失败`），全部通过退出码 0，有失败退出码非 0，可接入 CI / 定时巡检。

> 脚本依赖：服务器已装 `docker`、`docker compose`、`curl`；前端容器内 `wget` 由 nginx 镜像自带。
> 若某项 `[ FAIL ]`，按 §5.5 对应分类排查。

---

## 附录：License 说明

`application-prod.yml` 已设 `license.enabled: false`，**本次部署不会因缺授权/指纹而启动失败**。
若日后要开启授权（Docker 下容器 MAC 会变导致指纹失效），建议设 `license.bind-fingerprint: false`，
或给容器固定 MAC。签发/续期流程见上一版手册附录 A（取指纹 → `LicenseGenerator sign` → 上传）。

---

*docker 文件实际位置：*
- *`RuoYi-Vue-Plus/ruoyi-admin/Dockerfile`（镜像配方，无需改）*
- *`RuoYi-Vue-Plus/docker-compose.yml`（本次极简编排，只 backend+frontend）*
