# 校园联盟链低碳足迹系统集成测试与前后端联调技术整改报告

## 一、 前端闪退与白屏重定向问题 (Crash & Launch Bypass)

### 1. 现象描述
用户点击主页上的“低碳行为”绿色活动列表、打卡历史、或进入碳足迹记录列表时，页面短暂一闪而过随即白屏闪退，并自动重新调回登录页面。

### 2. 问题根因
JPA 分页实体返回格式不兼容导致的前端渲染异常抛错：
* 后端 `/api/actions/my-history` 和 `/api/record/list` 接口在单机/联调模式下，使用的是 Spring Data JPA 的 `Page` 分页结构，真正的数据存储在 `res.data.content` 数组内。
* 前端（如 `green-actions/list.vue`、`carbon-record/list.vue`）源码中，在拿到接口返回值后，无保护强行读取 `res.data.records`（该字段对于真后端为 `undefined`）。
* 在接下来的生命周期和计算属性中，程序立即对 `records` 进行了 `.filter()` 等操作，这在 JavaScript 引擎中抛出 `TypeError: Cannot read properties of undefined (reading 'filter')` 的崩溃级异常。
* uni-app 框架底层全局未捕获异常导致渲染树损坏崩溃，从而触发了应用重置、路由回退，判定为会话失效而强制重新 launched 至登录页面。

### 3. 整改方案 (已提交 Commit: 810d667)
* 在前端的核心数据绑定方法 `loadData` 和 `loadRecords` 中，将强读取方式重构为向上兼容、抗空的防护格式：
  `records.value = histRes.data.records || histRes.data.content || []`
  `records.value = res.data.records || res.data.content || []`
* 即使在后端为空或格式发生不可预期变化的情况下，页面依然能够安全优雅降级并展示空提示，绝不再引发 `TypeError` 白屏崩溃。

---

## 二、 假 AI 验证机制与硬编码置信度问题 (Fake AI Verify & Static Mock)

### 1. 现象描述
打卡时，无论上传什么样的无关、甚至纯色报错图片，系统均会提示打卡成功并自动派发奖励积分，且弹窗中显示出来的 AI 识别置信度每一次都是完全一模一样的 85%。

### 2. 问题根因
* 前台上传逻辑越权绕过：打卡提交页面（`checkin.vue`）中的 `handleSubmit` 逻辑中，直接向后端的 `/api/actions/checkin`（常规打卡接口）发送了带本地临时图片路径（`photo: photoPath.value`）的 JSON 请求。
* 该接口只处理普通表单，没有任何 Multipart 文件解析能力与百度 AI 核验对接，单机模式下后端默认全盘放行（`decision` 设为 `PASS`，`status` 直接改为 `COMPLETED`）并直接完成了加分。
* 前台接口返回结果中没有 `aiResult` 实体，源码使用了硬编码的后备降级默认值：`res.data.aiResult || { confidence: 0.85 }`。
* 真正的、已接入百度 AI 大模型图像分类的后台接口 `/api/ai/verify`（Multipart 上传接口）在打卡流程中处于完全被闲置和断开的状态。
* 单机测试环境下，`AiVerifyService.java` 中原本在不启用区块链时的结算逻辑不健全，没有对通过 AI 判定的用户记录进行本地加分处理。

### 3. 整改方案 (已提交 Commit: f7c8ca4 与 810d667)
* **前台文件上传核验**：打卡提交页面放弃调用 JSON 格式的打卡接口，全面改用 uni-app 底层的 `uni.uploadFile` 真正以 `Multipart`（文件表单）形式将拍摄照片传输到真实的 `/api/ai/verify`。
* **前台数据对齐**：在前台 `Result Modal` 页面中，绑定并渲染真正由百度 AI 返回的匹配标签（`label`）、以及动态置信度精度值（`score`），彻底去除了 `0.85` 的 Mock 现象。
* **失败拦截阻退**：对 AI 核验不通过（返回 `decision: REJECT`）的记录，前台弹出红叉判定，提示用户因识别标签不匹配拒绝本次资格发放，且限制页面不回退，阻止用户套现积分。
* **单机加分闭环**：在后端的 `AiVerifyService.java` 中，注入并解析 `BlockchainProperties`。在不开启区块链（单机验证环境）下，若 AI 核验为真，则本地将打卡状态由 PENDING 自动结算为 `COMPLETED` 并立即累加用户的本地积分账户。

---

## 三、 用户注册与数据库配置零配置自动初始化

### 1. 现象描述
首次运行数据库为空或新用户注册时，低碳打卡配置缺失无法核验打卡。

### 2. 问题根因
原有 `SystemConfigService` 虽然包含了 `@PostConstruct` 初始化，但在第一次运行、数据库无表或空表状态下缺少配置值的自动插入填充。

### 3. 整改方案 (已提交 Commit: f7c8ca4)
* 在 `SystemConfigService` 的 `@PostConstruct` 方法中增加了强大的空表前置检测：一旦检测到 `system_config` 为空，系统自动以底层批量 SQL 自动写入全部校园打卡的基础碳行为配置与减碳比重因子（包括 `points.clean_plate` / `points.walk` / `ai_threshold` 等），实现零门槛直接开箱运行。
