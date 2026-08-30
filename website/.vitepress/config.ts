import { defineConfig } from 'vitepress'

export default defineConfig({
    base: '/spring-boot-nebula/',
    lastUpdated: true,
    cleanUrls: true,
    locales: {
        root: {
            lang: 'zh-CN',
            label: '简体中文',
            title: 'Nebula',
            description: 'Spring Boot 3 企业级组件库 —— 统一响应、异常告警、分布式锁、读写分离、Excel、区域路由 JOIN',
            themeConfig: {
                langMenuLabel: '切换语言',
                sidebarMenuLabel: '菜单',
                returnToTopLabel: '回到顶部',
                outline: { label: '本页目录' },
                docFooter: { prev: '上一篇', next: '下一篇' },
                lastUpdated: { text: '上次更新' },
                notFound: {
                    title: '页面不存在',
                    quote: '看起来你访问的页面不存在，请检查链接是否正确。',
                    linkLabel: '返回首页',
                    linkText: '返回首页'
                },
                nav: [
                    { text: '首页', link: '/' },
                    { text: '快速开始', link: '/guide/quick-start' },
                    { text: '模块文档', link: '/modules/' },
                    { text: '最佳实践', link: '/guide/best-practice' },
                    { text: 'GitHub', link: 'https://github.com/weihubeats/spring-boot-nebula' }
                ],
                sidebar: {
                    '/guide/': [
                        {
                            text: '指南', items: [
                                { text: '快速开始', link: '/guide/quick-start' },
                                { text: '最佳实践', link: '/guide/best-practice' }
                            ]
                        }
                    ],
                    '/modules/': [
                        { text: '模块一览', link: '/modules/' },
                        {
                            text: '脚手架', items: [
                                { text: '项目脚手架', link: '/modules/archetype' }
                            ]
                        },
                        {
                            text: '依赖管理', items: [
                                { text: 'BOM 依赖', link: '/modules/dependencies' },
                                { text: '一键聚合', link: '/modules/all' }
                            ]
                        },
                        {
                            text: 'Web 能力', items: [
                                { text: 'Web 封装', link: '/modules/web' },
                                { text: '国际化', link: '/modules/i18n' },
                                { text: '通用工具', link: '/modules/tools' },
                                { text: '日志与告警', link: '/modules/log' }
                            ]
                        },
                        {
                            text: '数据访问', items: [
                                { text: 'MyBatis-Plus', link: '/modules/mybatis' },
                                { text: '动态数据源', link: '/modules/dynamic-datasource' },
                                { text: '区域路由 JOIN', link: '/modules/join' }
                            ]
                        },
                        {
                            text: '分布式能力', items: [
                                { text: '分布式锁', link: '/modules/distribute-lock' }
                            ]
                        },
                        {
                            text: '其他', items: [
                                { text: 'Excel', link: '/modules/excel' },
                                { text: 'Feign 自动解包', link: '/modules/feign' },
                                { text: '聚合根 DDD', link: '/modules/aggregate' }
                            ]
                        }
                    ]
                }
            }
        },
        en: {
            lang: 'en-US',
            label: 'English',
            link: '/en/',
            title: 'Nebula',
            description: 'Enterprise-grade component library for Spring Boot 3 — unified response, exception alerting, distributed lock, read/write splitting, Excel, region-routing JOIN',
            themeConfig: {
                nav: [
                    { text: 'Home', link: '/en/' },
                    { text: 'Quick Start', link: '/en/guide/quick-start' },
                    { text: 'Modules', link: '/en/modules/' },
                    { text: 'Best Practices', link: '/en/guide/best-practice' },
                    { text: 'GitHub', link: 'https://github.com/weihubeats/spring-boot-nebula' }
                ],
                sidebar: {
                    '/en/guide/': [
                        {
                            text: 'Guide', items: [
                                { text: 'Quick Start', link: '/en/guide/quick-start' },
                                { text: 'Best Practices', link: '/en/guide/best-practice' }
                            ]
                        }
                    ],
                    '/en/modules/': [
                        { text: 'Overview', link: '/en/modules/' },
                        {
                            text: 'Scaffolding', items: [
                                { text: 'Project Archetype', link: '/en/modules/archetype' }
                            ]
                        },
                        {
                            text: 'Dependency Management', items: [
                                { text: 'BOM Dependencies', link: '/en/modules/dependencies' },
                                { text: 'All-in-One', link: '/en/modules/all' }
                            ]
                        },
                        {
                            text: 'Web Capabilities', items: [
                                { text: 'Web Wrapper', link: '/en/modules/web' },
                                { text: 'Internationalization', link: '/en/modules/i18n' },
                                { text: 'Common Utilities', link: '/en/modules/tools' },
                                { text: 'Logging & Alerting', link: '/en/modules/log' }
                            ]
                        },
                        {
                            text: 'Data Access', items: [
                                { text: 'MyBatis-Plus', link: '/en/modules/mybatis' },
                                { text: 'Dynamic Datasource', link: '/en/modules/dynamic-datasource' },
                                { text: 'Region-Routing JOIN', link: '/en/modules/join' }
                            ]
                        },
                        {
                            text: 'Distributed', items: [
                                { text: 'Distributed Lock', link: '/en/modules/distribute-lock' }
                            ]
                        },
                        {
                            text: 'Others', items: [
                                { text: 'Excel', link: '/en/modules/excel' },
                                { text: 'Feign Unwrapping', link: '/en/modules/feign' },
                                { text: 'Aggregate Root DDD', link: '/en/modules/aggregate' }
                            ]
                        }
                    ]
                }
            }
        }
    },
    themeConfig: {
        socialLinks: [
            { icon: 'github', link: 'https://github.com/weihubeats/spring-boot-nebula' }
        ],
        footer: {
            message: 'Released under the Apache 2.0 License.',
            copyright: 'Copyright © 2026 weihubeats'
        }
    },
    markdown: {
        lineNumbers: true
    }
})
