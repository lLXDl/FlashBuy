#!/bin/bash

set -e

echo "======================================"
echo "    FlashBuy Docker 一键部署脚本"
echo "======================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查是否为 root 用户
if [ "$(id -u)" != "0" ]; then
    echo -e "${RED}错误：请使用 root 用户运行此脚本${NC}"
    exit 1
fi

# ======================================
# ① 安装 Docker 和 Docker Compose
# ======================================
echo -e "\n${YELLOW}① 安装 Docker 和 Docker Compose...${NC}"

if ! command -v docker &> /dev/null; then
    echo "正在安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
else
    echo "Docker 已安装"
fi

if ! command -v docker-compose &> /dev/null; then
    echo "正在安装 Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/download/v2.24.6/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
else
    echo "Docker Compose 已安装"
fi

echo -e "${GREEN}✓ Docker 环境安装完成${NC}"

# ======================================
# ② 停止宿主机原有服务
# ======================================
echo -e "\n${YELLOW}② 停止宿主机原有服务...${NC}"

services=("mysqld" "mysql" "redis" "redis-server" "rabbitmq-server" "nginx")
for service in "${services[@]}"; do
    if systemctl is-active --quiet "$service" 2>/dev/null; then
        echo "停止 $service..."
        systemctl stop "$service"
    fi
done

echo -e "${GREEN}✓ 宿主机服务已停止${NC}"

# ======================================
# ③ 创建必要目录
# ======================================
echo -e "\n${YELLOW}③ 创建必要目录...${NC}"

mkdir -p ./data/mysql
mkdir -p ./data/redis
mkdir -p ./data/rabbitmq
mkdir -p ./data/nacos/logs
mkdir -p ./data/nacos/data
mkdir -p ./sql

echo -e "${GREEN}✓ 目录创建完成${NC}"

# ======================================
# ④ 启动中间件服务
# ======================================
echo -e "\n${YELLOW}④ 启动中间件服务（MySQL/Redis/RabbitMQ/Nacos）...${NC}"

docker compose up -d mysql redis rabbitmq nacos

echo "等待中间件启动..."
sleep 30

# 检查 MySQL 状态
echo "检查 MySQL 状态..."
for i in {1..10}; do
    if docker compose exec -T mysql mysqladmin ping -h localhost --password=flashbuy@123 2>/dev/null; then
        echo "MySQL 就绪"
        break
    fi
    echo "等待 MySQL... ($i/10)"
    sleep 5
done

# 检查 Redis 状态
echo "检查 Redis 状态..."
for i in {1..10}; do
    if docker compose exec -T redis redis-cli -a flashbuy@123 ping 2>/dev/null | grep -q PONG; then
        echo "Redis 就绪"
        break
    fi
    echo "等待 Redis... ($i/10)"
    sleep 3
done

# 检查 RabbitMQ 状态
echo "检查 RabbitMQ 状态..."
for i in {1..10}; do
    if docker compose exec -T rabbitmq rabbitmqctl status 2>/dev/null; then
        echo "RabbitMQ 就绪"
        break
    fi
    echo "等待 RabbitMQ... ($i/10)"
    sleep 5
done

# 检查 Nacos 状态
echo "检查 Nacos 状态..."
nacos_ready=false
for i in {1..30}; do
    if curl -s http://localhost:8848/nacos/v1/ns/health/liveness | grep -q "UP"; then
        nacos_ready=true
        echo "Nacos 就绪"
        break
    fi
    echo "等待 Nacos... ($i/30)"
    sleep 5
done

if [ "$nacos_ready" = false ]; then
    echo -e "${RED}Nacos 启动失败，请查看日志${NC}"
    docker compose logs nacos
    exit 1
fi

echo -e "${GREEN}✓ 中间件服务全部启动成功${NC}"

# ======================================
# ⑤ 初始化数据库
# ======================================
echo -e "\n${YELLOW}⑤ 初始化数据库...${NC}"

if ls ./sql/*.sql 1>/dev/null 2>&1; then
    echo "正在导入 SQL 文件..."
    for sql_file in ./sql/*.sql; do
        docker compose exec -T mysql mysql -u root -pflashbuy@123 fastbuy < "$sql_file"
        echo "导入 $sql_file 完成"
    done
else
    echo "提示：未找到 SQL 文件，请手动导入数据库表结构"
    echo "执行方式：docker compose exec mysql mysql -u root -pflashbuy@123 fastbuy"
fi

echo -e "${GREEN}✓ 数据库初始化完成${NC}"

# ======================================
# ⑥ Maven 打包所有服务
# ======================================
echo -e "\n${YELLOW}⑥ Maven 打包所有服务...${NC}"

if ! command -v mvn &> /dev/null; then
    echo "正在安装 Maven..."
    wget -q https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
    tar -xzf apache-maven-3.9.5-bin.tar.gz -C /opt
    ln -s /opt/apache-maven-3.9.5/bin/mvn /usr/local/bin/mvn
fi

echo "开始打包..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}Maven 打包失败${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Maven 打包完成${NC}"

# ======================================
# ⑦ Docker Build 业务镜像
# ======================================
echo -e "\n${YELLOW}⑦ 构建业务服务镜像...${NC}"

docker compose build gateway auth-service seckill-service frontend

echo -e "${GREEN}✓ 业务镜像构建完成${NC}"

# ======================================
# ⑧ 启动业务服务
# ======================================
echo -e "\n${YELLOW}⑧ 启动业务服务...${NC}"

docker compose up -d gateway auth-service seckill-service frontend

echo "等待业务服务启动..."
sleep 20

# 检查 Gateway 状态
echo "检查 Gateway 状态..."
gateway_ready=false
for i in {1..20}; do
    if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
        gateway_ready=true
        echo "Gateway 就绪"
        break
    fi
    echo "等待 Gateway... ($i/20)"
    sleep 3
done

if [ "$gateway_ready" = false ]; then
    echo -e "${RED}Gateway 启动失败，请查看日志${NC}"
    docker compose logs gateway
    exit 1
fi

echo -e "${GREEN}✓ 业务服务全部启动成功${NC}"

# ======================================
# ⑨ 验证部署
# ======================================
echo -e "\n${YELLOW}⑨ 验证部署...${NC}"

echo "--- 容器状态 ---"
docker compose ps

echo -e "\n${GREEN}======================================"
echo -e "    ✅ FlashBuy 部署完成！"
echo -e "======================================${NC}"
echo -e "\n访问地址："
echo -e "  前端页面: ${YELLOW}http://服务器IP${NC}"
echo -e "  Gateway: ${YELLOW}http://服务器IP:8080${NC}"
echo -e "  Nacos 控制台: ${YELLOW}http://服务器IP:8848/nacos (nacos/nacos)${NC}"
echo -e "  RabbitMQ 管理: ${YELLOW}http://服务器IP:15672 (flashbuy/flashbuy@123)${NC}"

echo -e "\n常用命令："
echo -e "  查看日志: ${YELLOW}docker-compose logs -f [服务名]${NC}"
echo -e "  停止服务: ${YELLOW}docker-compose down${NC}"
echo -e "  重启服务: ${YELLOW}docker-compose restart${NC}"
echo -e "  查看容器: ${YELLOW}docker-compose ps${NC}"