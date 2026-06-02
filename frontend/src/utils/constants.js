// API 基础地址 — 与后端 Spring Boot 对接
export const API_BASE_URL = '/api'

// 区块链合约地址（FISCO BCOS）
export const CONTRACT_ADDRESS = {
  pointsToken: '', // 积分代币合约地址，部署后填入
  actionLedger: '', // 行为账本合约地址
}

// 积分规则映射（与后端 /api/home/quick-actions 数据结构一致）
export const GREEN_ACTIONS = {
  walk: { name: '步行出行', points: 10, unit: '次', icon: 'walk' },
  bike: { name: '骑行出行', points: 15, unit: '次', icon: 'bike' },
  bus: { name: '公交出行', points: 20, unit: '次', icon: 'bus' },
  recycle: { name: '垃圾分类', points: 5, unit: '次', icon: 'recycle' },
  oldGoods: { name: '旧物回收', points: 30, unit: '次', icon: 'goods' },
  savePower: { name: '节约用电', points: 8, unit: '天', icon: 'power' },
  noPlastic: { name: '拒绝一次性塑料', points: 12, unit: '次', icon: 'plastic' },
  plantTree: { name: '植树护绿', points: 50, unit: '棵', icon: 'tree' },
}

// 碳排放系数 (kgCO₂/单位)
export const CARBON_FACTORS = {
  car: 0.2, // 汽车出行每公里
  bus: 0.05, // 公交每公里
  subway: 0.03, // 地铁每公里
  meat: 6.9, // 每餐含肉
  veg: 1.5, // 素食每餐
  electricity: 0.8, // 每度电
}

// 本地存储 key
export const STORAGE_KEYS = {
  token: 'ccs_token',
  userInfo: 'ccs_user_info',
  refreshToken: 'ccs_refresh_token',
}
