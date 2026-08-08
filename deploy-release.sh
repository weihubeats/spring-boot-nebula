#!/bin/bash
# spring-boot-nebula 发布到 Maven 中央仓库
# 用法:
#   ./deploy-release.sh        # 发布正式版本（会检查是否在主分支）
#   ./deploy-release.sh -s     # 发布 SNAPSHOT
#   ./deploy-release.sh -f     # 强制发布，跳过分支检查
# 设置自定义 settings 路径:
#   export MAVEN_SETTINGS=/your/path/settings.xml

set -e

VERSION=$(grep -o '<revision>[^<]*</revision>' pom.xml | sed 's/<revision>//;s/<\/revision>//')
echo "=============================================="
echo "  Spring Boot Nebula Deploy"
echo "  Version: ${VERSION}"
echo "=============================================="

FORCE=false
SNAPSHOT=false

if [ "$1" = "-s" ]; then
    SNAPSHOT=true
    echo ">>> 模式: SNAPSHOT 发布"
elif [ "$1" = "-f" ]; then
    FORCE=true
    echo ">>> 模式: 正式版本（跳过分支检查）"
else
    echo ">>> 模式: 正式版本发布"
fi

# 1. 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装"
    exit 1
fi

MVN_VERSION=$(mvn --version 2>&1 | head -1)
echo ">>> Maven: ${MVN_VERSION}"

# 2. Maven Settings 路径（支持自定义）
# 优先级: MAVEN_SETTINGS 环境变量 > Maven 自身 conf > ~/.m2/settings.xml
if [ -n "$MAVEN_SETTINGS" ]; then
    SETTINGS="$MAVEN_SETTINGS"
elif [ -f "$(dirname $(which mvn))/../conf/settings.xml" ]; then
    SETTINGS="$(dirname $(which mvn))/../conf/settings.xml"
elif [ -f "$HOME/.m2/settings.xml" ]; then
    SETTINGS="$HOME/.m2/settings.xml"
else
    SETTINGS="$HOME/.m2/settings.xml"
fi

if [ ! -f "$SETTINGS" ]; then
    echo ""
    echo "❌ 未找到 settings.xml"
    echo "   当前查找: ${SETTINGS}"
    echo "   请设置: export MAVEN_SETTINGS=/your/path/to/settings.xml"
    exit 1
fi

if ! grep -q "<id>central</id>" "$SETTINGS" 2>/dev/null; then
    echo "⚠️  ${SETTINGS}"
    echo "   中未找到 id=central 的配置"
    echo ""
    echo "需要添加："
    echo "  <server>"
    echo "    <id>central</id>"
    echo "    <username>USR_XXX</username>"
    echo "    <password>PWD_XXX</password>"
    echo "  </server>"
    echo "（token 在 https://central.sonatype.com/usertoken 生成）"
    echo ""
    exit 1
fi

echo ">>> Settings: ${SETTINGS}"

# 3. 校验 Central token（发布前检查，避免到最后一步才失败）
if ! command -v curl &> /dev/null; then
    echo "❌ curl 未安装"
    exit 1
fi

CENTRAL_USER=$(sed '/<!--/,/-->/d' "$SETTINGS" | sed -n '/<id>central<\/id>/,/<\/server>/p' | sed -n 's:.*<username>\([^<]*\)</username>.*:\1:p' | head -1)
CENTRAL_PASS=$(sed '/<!--/,/-->/d' "$SETTINGS" | sed -n '/<id>central<\/id>/,/<\/server>/p' | sed -n 's:.*<password>\([^<]*\)</password>.*:\1:p' | head -1)

if [ -z "$CENTRAL_USER" ] || [ -z "$CENTRAL_PASS" ]; then
    echo "❌ ${SETTINGS} 中 id=central 的 username/password 配置不完整"
    exit 1
fi

echo ">>> 校验 Central token..."
TOKEN_HTTP=$(curl -s --max-time 30 -o /tmp/nebula-token-check.json -w "%{http_code}" -u "${CENTRAL_USER}:${CENTRAL_PASS}" "https://central.sonatype.com/api/v1/publisher/status")
if [ "$TOKEN_HTTP" = "401" ] || grep -q "Invalid token" /tmp/nebula-token-check.json 2>/dev/null; then
    echo "❌ Central token 无效（HTTP ${TOKEN_HTTP}）"
    echo ""
    echo "   请重新生成 token："
    echo "     1. 打开 https://central.sonatype.com/usertoken"
    echo "     2. 点击 Generate User Token"
    echo "     3. 将新生成的 username/password 更新到:"
    echo "        ${SETTINGS}"
    echo "        的 <id>central</id> 中"
    echo ""
    exit 1
fi
echo ">>> Central token 校验通过 ✓"

# 4. 检查 GPG
if ! command -v gpg &> /dev/null; then
    echo "❌ GPG 未安装，正式版需要签名"
    exit 1
fi

GPG_KEY=$(gpg --list-secret-keys --keyid-format=long 2>/dev/null | grep sec | head -1)
if [ -z "$GPG_KEY" ]; then
    echo "⚠️  未找到 GPG 密钥，正式版需要签名"
    echo "   生成: gpg --gen-key"
    echo "   上传公钥: gpg --keyserver hkp://keyserver.ubuntu.com --send-keys <KEY_ID>"
    if [ "$SNAPSHOT" = false ]; then
        exit 1
    fi
fi
echo ">>> GPG 密钥: ${GPG_KEY}"

# 5. 检查分支（正式版）
if [ "$SNAPSHOT" = false ] && [ "$FORCE" = false ]; then
    CURRENT_BRANCH=$(git branch --show-current)
    if [ "$CURRENT_BRANCH" != "main" ] && [ "$CURRENT_BRANCH" != "master" ]; then
        echo ""
        echo "❌ 当前分支: ${CURRENT_BRANCH}，不是 main/master"
        echo "   正式版只能在 main 或 master 分支发布"
        echo "   如需强制发布，运行: $0 -f"
        exit 1
    fi
    echo ">>> 当前分支: ${CURRENT_BRANCH} ✓"
fi

# 6. 编译、测试、安装到本地
echo ""
echo "=============================================="
echo "  Step 1/3: Compile, Test, Install"
echo "=============================================="
if [ "$SNAPSHOT" = true ]; then
    mvn -s "$SETTINGS" clean install -DskipTests=false -Dmaven.test.skip=false -Dgpg.skip=true
else
    mvn -s "$SETTINGS" clean install -DskipTests=false -Dmaven.test.skip=false
fi

# 7. 部署到 Central Portal
echo ""
echo "=============================================="
echo "  Step 2/3: Deploy to Central Portal"
echo "=============================================="
if [ "$SNAPSHOT" = true ]; then
    mvn -s "$SETTINGS" deploy -DskipTests=false -Dgpg.skip=true -Dmaven.test.skip=false
else
    mvn -s "$SETTINGS" deploy -DskipTests=false -Dmaven.test.skip=false
fi

echo ""
echo "=============================================="
echo "  Step 3/3: 等待发布完成"
echo "=============================================="
echo ""
echo "✅ 发布命令已执行，已配置 autoPublish=true，等待发布完成"
echo ""
echo "说明："
echo "  1. central-publishing-maven-plugin 会自动上传并发布（无需手动操作）"
echo "  2. 如果 mvn deploy 正常结束即表示已发布成功"
echo "  3. 可在 https://central.sonatype.com/publishing 查看发布记录"
echo "  4. 在 Maven Central 搜索确认: https://central.sonatype.com/artifact-overview"
echo ""
