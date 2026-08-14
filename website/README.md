# Spring Boot Nebula 官网

基于 [VitePress](https://vitepress.dev) 的静态站点。

## 目录结构

```
website/
├── .vitepress/
│   ├── config.ts       # 站点配置（导航、侧边栏、base 路径）
│   ├── dist/           # 构建产物
│   └── cache/
├── guide/              # 指南文档（快速开始、最佳实践）
├── modules/            # 各模块详细文档
├── public/             # 静态资源（logo 等）
├── index.md            # 首页
├── package.json
└── package-lock.json
```

## 本地预览

```bash
cd website && npm install

# 开发模式（热更新），默认 http://localhost:5173/spring-boot-nebula/
npm run docs:dev

# 生产构建
npm run docs:build

# 预览构建产物，地址 http://localhost:4173/spring-boot-nebula/
npm run docs:preview
```

## 文档修改

- `guide/` 下的 `.md` 为指南页面
- `modules/` 下的 `.md` 为各模块文档
- 新增页面需同步更新 `.vitepress/config.ts` 的 `nav` 和 `sidebar`

## 发布

推送 main 分支后，CI（`.github/workflows/deploy-website.yml`）自动构建并发布到 GitHub Pages：

`https://weihubeats.github.io/spring-boot-nebula/`（base 路径 `/spring-boot-nebula/`）