# 部署方案

本系统打包为**一个可执行 jar**：前端页面已内置在 jar 中，后端同时提供页面与接口。
部署机器只需安装 **Java 11 或以上**，无需 Node.js、Nginx 或 Tomcat。

```
浏览器 ──HTTP:8080──▶ gdpm-system.jar（Spring Boot：前端页面 + /api 接口）
                          ├─ 数据库：内置 H2 文件库（默认，开箱即用）或 MySQL 8
                          └─ 附件：./uploads 目录
```

| 场景 | 推荐方式 |
| --- | --- |
| 演示、答辩、本机试用 | 方式一：下载发布包 + 内置数据库，两条命令启动 |
| 长期使用、多人访问 | 方式一 + 切换 MySQL（第 3 节）+ Linux 后台运行（第 4 节） |
| 需要修改代码 | 方式二：从源码构建 |

---

## 1. 方式一：下载发布包直接运行（推荐）

### 1.1 安装 Java 11+

```bash
java -version          # 显示 11 或更高版本即可，已安装则跳过
```

- Ubuntu / Debian：`sudo apt update && sudo apt install -y openjdk-17-jre-headless`
- CentOS / Rocky / 阿里云 Linux：`sudo yum install -y java-17-openjdk-headless`
- Windows：安装 [Eclipse Temurin 17](https://adoptium.net/zh-CN/temurin/releases/)（勾选“设置 JAVA_HOME”）
- macOS：`brew install openjdk@17`

### 1.2 下载并启动

Linux / macOS：

```bash
mkdir -p ~/gdpm && cd ~/gdpm
curl -L -o gdpm-system.jar https://github.com/Yfhaaa/xhaaaa/releases/latest/download/gdpm-system.jar
java -jar gdpm-system.jar
```

Windows PowerShell：

```powershell
New-Item -ItemType Directory -Force C:\gdpm | Out-Null; Set-Location C:\gdpm
Invoke-WebRequest https://github.com/Yfhaaa/xhaaaa/releases/latest/download/gdpm-system.jar -OutFile gdpm-system.jar
java -jar gdpm-system.jar
```

看到日志 `Started GdpmApplication` 即启动成功（约 10–30 秒）。首次启动会在**当前目录**自动创建
`data/`（数据库）与 `uploads/`（附件），并写入演示数据。

> GitHub 下载慢时，可先在能访问 GitHub 的电脑上下载 jar，再上传到服务器同一目录。

### 1.3 访问与验证

| 检查 | 地址 | 期望结果 |
| --- | --- | --- |
| 健康检查 | `http://服务器IP:8080/api/health` | `{"status":"UP","application":"gdpm"}` |
| 系统首页 | `http://服务器IP:8080` | 登录页 |
| 接口文档 | `http://服务器IP:8080/swagger-ui/index.html` | Swagger 页面 |

演示账号（密码均为 `123456`）：`admin`（院系管理员）、`teacher1` / `teacher2`（指导教师）、
`student1`–`student4`（学生）。**正式使用前请在“系统管理 → 用户管理”中修改密码。**

云服务器需在安全组 / 防火墙放行 8080 端口，例如 Ubuntu：`sudo ufw allow 8080`。

---

## 2. 方式二：从源码构建

需要 JDK 11+ 与 Node.js 18+；Maven 可不装（脚本自动使用项目自带的 `backend/mvnw`）。

```bash
git clone https://github.com/Yfhaaa/xhaaaa.git gdpm && cd gdpm
bash scripts/build-release.sh --skip-tests        # Windows：powershell -ExecutionPolicy Bypass -File scripts\build-release.ps1 -SkipTests
java -jar release/gdpm-system.jar
```

脚本依次执行：前端 `npm ci` + `npm run build` → 产物复制到 `backend/src/main/resources/static/` →
Maven 打包 → 输出 `release/gdpm-system.jar`。去掉 `--skip-tests` 会同时运行 26 项集成测试。

国内网络下载依赖慢时，可先设置镜像：

```bash
npm config set registry https://registry.npmmirror.com
export MVNW_REPOURL=https://maven.aliyun.com/repository/public   # Maven Wrapper 下载 Maven 本体的镜像
```

Maven 依赖镜像可在 `~/.m2/settings.xml` 中配置阿里云镜像，项目内 `scripts/maven-settings.xml` 可作参考。

开发模式（前后端分开启动、前端热更新）见 [README](README.md#运行与构建)。

---

## 3. 切换到 MySQL 8（可选）

默认的 H2 文件库适合演示与单机使用；多人长期使用建议换成 MySQL。

**① 建库并导入表结构与演示数据**（`sql/init.sql` 会创建 `gdpm` 库，utf8mb4）：

```bash
mysql -uroot -p --default-character-set=utf8mb4 < sql/init.sql
```

> 只下载了 jar 的部署方式，可单独下载该文件：
> `curl -L -o init.sql https://raw.githubusercontent.com/Yfhaaa/xhaaaa/main/sql/init.sql`
>
> 建议为系统单独建账号，而不是使用 root：
> ```sql
> CREATE USER 'gdpm'@'%' IDENTIFIED BY '请改成强密码';
> GRANT ALL PRIVILEGES ON gdpm.* TO 'gdpm'@'%';
> FLUSH PRIVILEGES;
> ```

**② 以 mysql 配置启动**（账号密码只通过环境变量传入，不写进任何文件）：

```bash
export SPRING_PROFILES_ACTIVE=mysql
export DB_URL='jdbc:mysql://127.0.0.1:3306/gdpm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
export GDPM_DB_USERNAME=gdpm
export GDPM_DB_PASSWORD='你的密码'
java -jar gdpm-system.jar
```

Windows PowerShell 用 `$env:SPRING_PROFILES_ACTIVE='mysql'` 等方式设置同名变量。

---

## 4. Linux 长期运行（systemd，参考配置）

```bash
sudo useradd -r -s /sbin/nologin gdpm || true
sudo mkdir -p /opt/gdpm && sudo cp gdpm-system.jar /opt/gdpm/ && sudo chown -R gdpm:gdpm /opt/gdpm
sudo tee /etc/systemd/system/gdpm.service >/dev/null <<'EOF'
[Unit]
Description=GDPM 毕业设计全过程管理系统
After=network.target

[Service]
User=gdpm
WorkingDirectory=/opt/gdpm
Environment=SERVER_PORT=8080
# 使用 MySQL 时取消以下注释并填写
#Environment=SPRING_PROFILES_ACTIVE=mysql
#Environment=DB_URL=jdbc:mysql://127.0.0.1:3306/gdpm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
#Environment=GDPM_DB_USERNAME=gdpm
#Environment=GDPM_DB_PASSWORD=你的密码
ExecStart=/usr/bin/java -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -jar /opt/gdpm/gdpm-system.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
sudo systemctl daemon-reload && sudo systemctl enable --now gdpm
sudo systemctl status gdpm        # 查看状态
journalctl -u gdpm -f             # 查看日志
```

如需使用 80 端口或域名，可用 Nginx 反向代理（参考配置）：

```nginx
server {
    listen 80;
    server_name your.domain.com;
    client_max_body_size 25m;          # 附件上限 20MB
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;   # 操作日志据此记录来源 IP
    }
}
```

---

## 5. 配置项

全部通过环境变量设置，未设置时使用默认值。

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | 监听端口 |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` = 内置 H2 文件库；`mysql` = MySQL |
| `DB_URL` | `jdbc:mysql://127.0.0.1:3306/gdpm?...` | 仅 mysql 模式 |
| `GDPM_DB_USERNAME` / `GDPM_DB_PASSWORD` | 无 | 仅 mysql 模式，必填 |
| `UPLOAD_DIR` | `./uploads` | 材料附件存放目录 |

H2 数据库文件位于启动目录下的 `data/`。

## 6. 备份与升级

- **备份**：停止服务后复制 `data/`（H2）或执行 `mysqldump gdpm > gdpm.sql`（MySQL），并复制 `uploads/`。
- **升级**：停止服务 → 替换 `gdpm-system.jar` → 启动。数据与附件目录保持不变。
- **恢复演示数据**（仅 H2）：停止服务，将 `data/` 改名或移走，重新启动即自动生成。

## 7. 常见问题

| 现象 | 处理 |
| --- | --- |
| `UnsupportedClassVersionError` | Java 版本低于 11，升级 JDK |
| `Port 8080 was already in use` | 换端口：`SERVER_PORT=9090 java -jar gdpm-system.jar` |
| 页面能打开但登录报错 | 查看启动日志；mysql 模式检查 `DB_URL`、账号密码，以及是否已导入 `init.sql` |
| 中文乱码 | 启动参数加 `-Dfile.encoding=UTF-8`；MySQL 库须为 utf8mb4 |
| 上传附件失败 | 单个文件不超过 20MB；Nginx 需设置 `client_max_body_size` |
| 外网无法访问 | 检查云服务器安全组与系统防火墙是否放行端口 |
