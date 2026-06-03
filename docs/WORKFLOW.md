# Monorepo 工作流与仓库概况

本文档用于说明仓库目录结构、项目介绍、分支策略与团队协作流程，可直接参考。

## 项目简介
校园“碳足迹”绿色行动可信积分系统，面向高校场景，采用 Web2 业务主干 + AI 图像识别 + 轻量级链上审计的混合架构。学生上传环保行为图片，后端调用 AI 识别并进行置信度裁决，达标后生成行为记录与积分发放依据，积分可用于校园商城兑换。

## 仓库目录结构
```
campus-carbon-system/
├── backend/     # Spring Boot 后端（AI 核验、业务 API、数据层）
├── frontend/    # uni-app 客户端
├── contracts/   # Solidity + Hardhat 合约与测试
└── docs/        # 文档与数据库脚本
```

## 分支策略
- main：发布分支，只允许合并稳定版本
- develop：集成分支，日常开发在此合并
- feature/<module>-<short-desc>：功能分支

推荐模块前缀：
- feature/backend-...
- feature/frontend-...
- feature/contracts-...

## 初始化流程（仓库所有者）
1. 创建 GitHub 仓库：campus-carbon-system（Public，MIT 可选）
2. 初始化 README + Node .gitignore
3. 创建 develop 分支并保护 main/develop（要求 PR + 审核）

## 初始化流程（团队成员）
1. Clone 仓库
2. 从 develop 拉取 feature 分支
3. 只在自己的模块目录内开发

## PR 合并流程
1. 保持本地 develop 与 origin/develop 同步
2. 从 develop 切出 feature 分支
3. 提交清晰的 commit 信息
4. 发起 PR 合并到 develop
5. 通过代码审查与基础检查
6. 必要时 Squash 或 Rebase

## Fork + PR 流程（非协作者）
适用于无法直接推送主仓库的成员。

1. 组长创建主仓库 campus-carbon-system
2. 成员 Fork 主仓库到个人账号
3. 本地克隆个人 Fork
4. 添加上游仓库 upstream 指向主仓库
5. 从 upstream/develop 拉分支并开发
6. 推送到个人 Fork
7. 从个人 Fork 向主仓库 develop 发起 PR

## 提交信息规范
- 使用简洁、明确的动词短语
- 示例：
  - feat: add AI verify endpoint
  - fix: correct exchange points calc
  - docs: update database schema notes

## 模块职责划分
- 后端组：
  - 数据库设计与 CRUD
  - AI 识别路由与置信度裁决
  - 业务 API 与服务层
- 前端组：
  - uni-app 页面、拍照上传、UI 对接
  - 业务接口调用与展示
- 合约组：
  - Solidity 合约与 ABI 导出
  - Hardhat 本地网络与部署脚本
  - 合约测试

## 基础协作约束
- 未经沟通不修改其他模块目录
- 避免提交大体积二进制文件
- 机密信息只放本地 .env 文件，不入仓库

## 示例命令
克隆并创建功能分支：
- git clone https://github.com/<ORG_OR_USER>/campus-carbon-system.git
- git checkout develop
- git pull
- git checkout -b feature/backend-ai-verify

提交并推送后发起 PR：
- git add .
- git commit -m "feat: add AI verify endpoint"
- git push -u origin feature/backend-ai-verify

Fork + PR 命令示例：
- git clone https://github.com/<YOUR_USER>/campus-carbon-system.git
- git remote add upstream https://github.com/<LEADER_USER>/campus-carbon-system.git
- git fetch upstream
- git checkout -b develop upstream/develop
- git checkout -b feature/backend-ai-verify
- git add .
- git commit -m "feat: add AI verify endpoint"
- git push -u origin feature/backend-ai-verify

