# 前端核心架构文档

## 技术栈

Vue 3（Composition API）+ uni-app + Vite + Pinia

---

## 目录结构

```
frontend/src/
├── api/                   # 接口封装层
│   ├── request.js         # 统一请求（token 注入、401 拦截、mock 开关）
│   ├── user.js            # 登录/注册/资料
│   ├── points.js          # 积分/兑换/商品
│   ├── actions.js         # 绿色行动/打卡
│   ├── home.js            # 首页数据
│   ├── record.js          # 碳足迹记录
│   └── leaderboard.js     # 排行榜
├── stores/                # Pinia 状态管理
│   ├── user.js            # 用户登录态/角色/资料
│   └── app.js             # 首页数据缓存
├── pages/                 # 页面
│   ├── login/             # 登录（含角色选择）
│   ├── register/          # 注册
│   ├── index/             # 首页仪表盘
│   ├── green-actions/     # 绿色行动列表 + AI 拍照打卡
│   ├── carbon-record/     # 碳足迹记录
│   ├── points/            # 积分中心 + 兑换商城
│   ├── leaderboard/       # 排行榜
│   ├── profile/           # 个人中心
│   ├── data-viz/          # 数据可视化
│   ├── blockchain/        # 链上存证
│   └── admin/             # 管理后台
│       ├── dashboard.vue  # 统计面板
│       ├── audit.vue      # 审核管理
│       ├── products.vue   # 商品管理
│       └── users.vue      # 用户管理
├── components/            # 公共组件
│   ├── common/            # 通用（nav-bar）
│   └── dashboard/         # 首页卡片/图表组件
├── utils/
│   ├── constants.js       # API_BASE_URL、STORAGE_KEYS
│   └── web3.js            # 钱包地址格式化
└── pages.json             # uni-app 页面路由 + tabBar 配置
```

---

## 路由 & 页面配置

`pages.json` 中注册了全部 18 个页面。底部 tabBar 共 5 项：

| tab | 路径 | 说明 |
|-----|------|------|
| 🏠 首页 | `pages/index/index` | 仪表盘 |
| 👣 足迹 | `pages/carbon-record/list` | 碳足迹记录 |
| 🌱 行动 | `pages/green-actions/list` | 绿色行动 |
| 🏆 积分 | `pages/points/index` | 积分中心 |
| 📊 排行 | `pages/leaderboard/index` | 排行榜 |

管理后台 4 个页面不在 tabBar 中，从首页⚙️按钮或登录后自动跳转进入。

---

## 请求封装

`api/request.js` 统一处理：

- 自动注入 `Authorization: Bearer <token>`
- 401 时清空登录态并跳转登录页
- `USE_MOCK = false` 走真实接口，`true` 走内存 Mock

```js
// 调用示例
import { request } from '@/api/request'
const res = await request({ url: '/products', method: 'GET' })
```

接口路径均为相对路径，Vite 代理 `/api` → `localhost:8080`。

---

## 角色权限

### JWT 携带 role 字段

登录后 token 含 `role: "USER"` 或 `role: "ADMIN"`，后端 `AdminController` 校验非 ADMIN 返回 403。

### 前端路由分流

```js
// stores/user.js
const role = computed(() => userInfo.value?.role || 'USER')
const isAdmin = computed(() => role.value === 'ADMIN')

// login.vue — 登录后按角色跳转
if (userStore.isAdmin) {
  uni.reLaunch({ url: '/pages/admin/dashboard' })
} else {
  uni.reLaunch({ url: '/pages/index/index' })
}
```

首页右上角 ⚙️ 按钮仅 `v-if="userStore.isAdmin"` 显示。

---

## 后端接口映射

### Auth
| 前端调用 | 后端 |
|---------|------|
| `POST /auth/login` | `AuthController.login()` |
| `POST /auth/register` | `AuthController.register()` |

### 商品
| 前端调用 | 后端 |
|---------|------|
| `GET /products` | `ProductController.list()` |
| `POST /products` | `ProductController.create()` |
| `PUT /products/{id}` | `ProductController.update()` |
| `DELETE /products/{id}` | `ProductController.delete()` |

### 兑换
| 前端调用 | 后端 |
|---------|------|
| `POST /exchanges` | `ExchangeController.create()` |
| `GET /exchanges` | `ExchangeController.list()` |

### 管理后台



| 前端调用 | 后端 |
|---------|------|
| `GET /admin/stats` | `AdminController.getStats()` |
| `GET /admin/audit-list` | `AdminController.getAuditList()` |
| `POST /admin/audit-review` | `AdminController.auditReview()` |
| `GET /admin/users` | `AdminController.getUsers()` |

### 绿色行动/AI
| 前端调用 | 后端 |
|---------|------|
| `GET /actions/list` | `ActionsController.getList()` |
| `POST /ai/verify` (upload) | `AiVerifyController.verify()` |

### 其他
| 前端调用 | 后端 |
|---------|------|
| `GET /home/daily-summary` | `HomeController.getDailySummary()` |
| `GET /leaderboard/list` | `LeaderboardController.getList()` |
| `GET /points/balance` | `PointsController.getBalance()` |
| `GET /user/profile` | `UserController.getProfile()` |

---

## Vite 代理

```js
// vite.config.js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

所有 `/api/*` 请求转发到 Spring Boot 后端，本地开发无跨域问题。

---

## 启动

```bash
cd frontend
npm install        # 首次
npm run dev:h5     # H5 开发模式 → http://localhost:5173
```
