下面是**四个 PR 的详细总结**（包含文件与功能）

---

## **PR1 — Database & Entities**
**分支**：`feature/db-entities-v2`  
**核心目标**：建立数据底座与实体模型，保证业务闭环可落库。

**包含文件**
- DATABASE.sql  
- User.java  
- Product.java  
- ExchangeRecord.java  
- BehaviorRecord.java  
- SystemConfig.java

**实现功能**
- 定义用户、商品、兑换记录、行为记录、系统配置核心表结构  
- 行为记录包含图片 hash、链上哈希、状态、积分  
- 兑换记录包含核销码与核销状态字段  
- 用户实体包含钱包地址字段（为链上对接预留）

---

## **PR2 — AI Verification Flow**
**分支**：`feature/ai-verify-v2`  
**核心目标**：实现 AI 核验闭环与行为记录生成。

**包含文件**
- AiVerifyController.java  
- AiVerifyService.java  
- BaiduAiClient.java  
- BehaviorRuleService.java  
- BehaviorRecordService.java  
- BehaviorRecordController.java  
- BehaviorRecordRepository.java  
- AiVerifyResponse.java

**实现功能**
- `/api/ai/verify` 接口：图片上传、AI 调用、置信度裁决  
- AI token 缓存与结果解析  
- 行为白名单校验 + AI 标签匹配  
- 日限流控制（daily_limit）  
- 生成行为记录（含 hash、score、points、status）  
- 行为记录分页查询接口（为后台审核/前端列表准备）

---

## **PR3 — Exchange & Redemption**
**分支**：`feature/exchange-redeem-v2`  
**核心目标**：兑换与核销闭环。

**包含文件**
- ExchangeController.java  
- ExchangeService.java  
- ExchangeRecordRepository.java  
- CreateExchangeRequest.java  
- RedeemRequest.java

**实现功能**
- `/api/exchanges` 兑换创建：扣减积分/库存并落库  
- 核销码生成与核销接口 `/api/exchanges/redeem`  
- 兑换记录分页查询  
- 核销码查询能力（便于校验与追踪）

---

## **PR4 — Infrastructure & Common**
**分支**：`feature/infra-common-v2`  
**核心目标**：通用基础设施与工程性保障。

**包含文件**
- CarbonApplication.java  
- CarbonProperties.java  
- RestTemplateConfig.java  
- AppAuthProperties.java  
- SimpleAuthFilter.java  
- SimpleAuthFilterConfig.java  
- GlobalExceptionHandler.java  
- ApiErrorResponse.java  
- SystemConfigService.java  
- SystemConfigRepository.java  
- application.yml

**实现功能**
- 配置扫描与属性绑定  
- RestTemplate HTTP 客户端  
- 全局异常处理与统一错误响应  
- system_config 缓存服务  
- 轻量 API Key 鉴权（`X-API-KEY`）  
- application.yml 运行默认配置

---

下面是**每个 PR 对应的验证流程总结**（最小可行 demo）

---

## **PR1 — Database & Entities**
**目标**：验证表结构与实体一致性  
**流程**
1. 执行 DATABASE.sql  
2. 启动后端（哪怕先不调接口）  
3. 在数据库中检查：  
   - `users/products/behavior_records/exchange_records/system_config` 是否存在  
   - 字段是否完整（`image_hash/points/redeem_code` 等）

---

## **PR2 — AI Verification Flow**
**目标**：验证 AI 核验链路与行为记录入库  
**流程**
1. 执行 DATABASE.sql  
2. 配置 application.yml 的数据库与百度 AI 密钥  
3. `mvn spring-boot:run`  
4. 调接口：  
   - `POST /api/users`（先建用户）  
   - `POST /api/ai/verify`（上传图片）  
   - `GET /api/behaviors?userId=...`（确认记录入库与分页可用）

---

## **PR3 — Exchange & Redemption**
**目标**：验证兑换扣减与核销闭环  
**流程**
1. 执行 DATABASE.sql  
2. `mvn spring-boot:run`  
3. 调接口：  
   - `POST /api/users`  
   - `POST /api/products`  
   - `POST /api/exchanges`（生成兑换记录与核销码）  
   - `POST /api/exchanges/redeem`（核销）  
   - `GET /api/exchanges?userId=...`（核对记录）

---

## **PR4 — Infrastructure & Common**
**目标**：验证基础设施可用、异常返回一致  
**流程**
1. `mvn spring-boot:run`  
2. 检查配置读取：application.yml 能正常加载  
3. 验证异常返回：  
   - 用错误参数调用任意接口，应返回统一 `ApiErrorResponse`  
4. 验证鉴权（可选）：  
   - 在 application.yml 填 `app.auth.token`  
   - 请求头带 `X-API-KEY`，不带应返回 401

---

