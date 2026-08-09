#!/usr/bin/env bash
# ============================================================
# RuoYi-Vue-Plus + art-design-pro 部署自动验证脚本
# 用法：在 docker-compose.yml 所在目录执行  ./verify-deploy.sh
# 依赖：docker、docker compose、curl（容器内 wget 由 nginx 镜像自带）
# ============================================================
set -u

BACKEND_PORT=18000
FRONTEND_PORT=18880
BACKEND_SVC=ruoyi-backend
FRONTEND_SVC=ruoyi-frontend

PASS=0
FAIL=0

green() { printf '\033[32m%s\033[0m' "$1"; }
red()   { printf '\033[31m%s\033[0m' "$1"; }
check() {
  # $1=名称 $2=0通过/非0失败
  if [ "$2" -eq 0 ]; then
    printf '  [ %s ] %s\n' "$(green PASS)" "$1"; PASS=$((PASS+1))
  else
    printf '  [ %s ] %s\n' "$(red FAIL)" "$1"; FAIL=$((FAIL+1))
  fi
}

http_ok() {
  # $1=url  返回 0 表示 HTTP 200
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 8 "$1" 2>/dev/null)
  [ "$code" = "200" ]
}

echo "===== 1. 容器状态 ====="
docker compose ps --format '{{.Name}}\t{{.State}}' | grep -E "$BACKEND_SVC|$FRONTEND_SVC"
backend_up=$(docker compose ps -q "$BACKEND_SVC" 2>/dev/null | xargs -r docker inspect -f '{{.State.Running}}' 2>/dev/null | head -1)
frontend_up=$(docker compose ps -q "$FRONTEND_SVC" 2>/dev/null | xargs -r docker inspect -f '{{.State.Running}}' 2>/dev/null | head -1)
[ "$backend_up" = "true" ];  check "后端容器运行中" $?
[ "$frontend_up" = "true" ]; check "前端容器运行中" $?

echo "===== 2. 直连后端健康检查 ====="
http_ok "http://127.0.0.1:$BACKEND_PORT/actuator/health"
check "GET 127.0.0.1:$BACKEND_PORT/actuator/health -> 200" $?

echo "===== 3. 直连前端页面 ====="
http_ok "http://127.0.0.1:$FRONTEND_PORT/"
check "GET 127.0.0.1:$FRONTEND_PORT/ -> 200" $?

echo "===== 4. nginx 内部转发链路 (18880/prod-api -> backend) ====="
http_ok "http://127.0.0.1:$FRONTEND_PORT/prod-api/actuator/health"
check "GET 127.0.0.1:$FRONTEND_PORT/prod-api/actuator/health -> 200" $?

echo "===== 5. 前端容器内 nginx 配置 ====="
docker compose exec -T "$FRONTEND_SVC" grep -q "proxy_pass http://$BACKEND_SVC:${BACKEND_PORT}/;" /etc/nginx/conf.d/default.conf 2>/dev/null
check "default.conf 中 proxy_pass 已替换为 http://$BACKEND_SVC:$BACKEND_PORT/ (无 \${BACKEND_API} 字面量)" $?

echo "===== 6. 前端容器解析并直连后端服务名 ====="
docker compose exec -T "$FRONTEND_SVC" wget -qO- --timeout=8 "http://$BACKEND_SVC:$BACKEND_PORT/actuator/health" >/dev/null 2>&1
check "前端容器内 wget http://$BACKEND_SVC:$BACKEND_PORT/actuator/health 成功" $?

echo
echo "========================================="
printf '结果：%s 通过 / %s 失败\n' "$(green "$PASS")" "$(red "$FAIL")"
echo "========================================="
[ "$FAIL" -eq 0 ]
