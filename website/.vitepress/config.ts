import { defineConfig } from 'vitepress'

export default defineConfig({
    lang: 'zh-CN',
    title: 'Nebula',
    description: 'Spring Boot 3 企业级组件库 —— 统一响应、异常告警、分布式锁、读写分离、Excel、区域路由 JOIN',
    base: '/spring-boot-nebula/',
    lastUpdated: true,
    cleanUrls: true,
    themeConfig: {
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
        },
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