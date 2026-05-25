# 校园碳足迹绿色行动激励与积分系统 (Campus-Footprint-Architecture)

## 一、协作流程

本仓库不直接开放写权限。请通过以下步骤提交你的代码：

### 初始化准备

1.  点击页面右上角的 **Fork** 按钮，将本项目克隆到的个人 GitHub 账号下并创建仓库。
2.  克隆**你自己账号下**的那个仓库到本地：
    ```bash
    git clone https://github.com/你的用户名/campus-carbon-system.git
    cd campus-carbon-system
    ```
3.  **关联上游仓库**（为了同步团队最新代码）：
    ```bash
    git remote add upstream https://github.com/Kryptonor/campus-carbon-system.git
    ```

### 开发与同步
1.  每次开发前，先拉取我们团队主仓库的最新代码。
    ```bash
    git checkout develop
    git pull upstream develop
    ```
2.  **创建功能分支：**
    ```bash
    git checkout -b feature/你的名字-功能描述
    ```
3.  **提交改动：**
    ```bash
    git add .
    git commit -m "feat: 你的提交信息"
    ```
4.  **推送至你的远程仓库：**
    ```bash
    git push origin feature/你的名字-功能描述
    ```

### 发起 Pull Request (PR)
1.  回到**自己的 GitHub 仓库页面**，点击 **Contribute** → **Open pull request**。
2.  **确认合并方向：**
    * base repository: `Kryptonor/campus-carbon-system`  |  base: **`develop`**
    * head repository: `你的用户名/campus-carbon-system` |  compare: `你的分支名`
3.  确认无误后点击 **Create pull request**，等待审核。

---

## 二、目录结构

请根据你负责的模块，将代码放入对应目录：

*   `/backend`：Spring Boot 后端工程（含 AI 调用路由、业务逻辑）。
*   `/frontend`：移动端工程（H5/小程序界面）。
*   `/contracts`：Solidity 智能合约（积分代币、行为账本）。
*   `/ai`：AI 推理服务模块（Python/FastAPI 接口）。
*   `/docs`：项目需求文档、数据库 SQL 脚本、API 接口定义。

---

## 三、提交规范

1.  严禁上传 AI 模型权重文件 (`.pth`, `.h5`, `.model`)。大文件请存放至网盘，并在代码中配置下载地址。
2.  严禁上传包含数据库密码、链上私钥、API Key 的 `.env` 或配置文件。
3.  不要上传包括但不限于以下列举的各种 **运行依赖：** 
    * 前端：`node_modules/`、`unpackage/`
    * 后端：`target/`、`.mvn/`
    * 合约：`artifacts/`、`cache/`
    * AI：`venv/`、`__pycache__/`
4.  注意提交时标注 **前缀标识** （部分参考）：
    * `feat:` 新增功能 (Feature)
    * `fix:` 修复 Bug
    * `docs:` 文档更新 (README, SQL, API说明)
    * `refactor:` 代码重构
    * `chore:` 辅助工具或构建过程的变动

**示例：** `git commit -m "feat: 实现旧物回收积分链上扣减逻辑"`

---

## 四、技术栈概览
1. **后端：** Spring Boot 3, Web3j, MySQL
2. **前端：** Vue 3, uni-app, ECharts
3. **区块链：** FISCO BCOS, Solidity
4. **AI：** Python, FastAPI, PyTorch