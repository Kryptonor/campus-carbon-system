/**
 * Mock 数据 — 后端 API 尚未部署时的开发数据
 * 数据结构与后端 Spring Boot 响应对齐
 */

// 创建一份新的模拟用户数据（每次调用返回独立对象）
export function createMockUserData() {
  return {
    id: '2024001',
    studentId: '2024001001',
    name: '张同学',
    avatar: '',
    department: '计算机科学与技术学院',
    className: '软件工程2401班',
    grade: '2024级',
    points: 1280,
    carbonTotal: 156.8,
    carbonReduced: 45.2,
    walletAddress: '0x7a3b8f2c1d9e4a6b5c7d8e9f0a1b2c3d4e5f6a7b',
    joinDate: '2025-03-01',
  }
}

// ====== 碳足迹记录 Mock ======
export function createMockRecords() {
  return [
    {
      id: 'r001',
      category: '餐饮',
      categoryIcon: '🍜',
      carbonAmount: 5.2,
      quantity: '1份',
      photo: '',
      notes: '食堂午餐：米饭+红烧肉+青菜',
      aiResult: { category: '餐饮', carbonPerUnit: 5.2, confidence: 0.92, suggestion: '建议增加蔬菜比例，减少红肉摄入' },
      date: '2026-05-27 12:30',
      createdAt: Date.now() - 14400000,
    },
    {
      id: 'r002',
      category: '出行',
      categoryIcon: '🚗',
      carbonAmount: 3.8,
      quantity: '5公里',
      photo: '',
      notes: '打车去实验室',
      aiResult: { category: '出行', carbonPerUnit: 0.76, confidence: 0.88, suggestion: '建议改用公交或步行，每次可减少约70%碳排放' },
      date: '2026-05-27 08:30',
      createdAt: Date.now() - 28800000,
    },
    {
      id: 'r003',
      category: '用电',
      categoryIcon: '💡',
      carbonAmount: 2.4,
      quantity: '3度',
      photo: '',
      notes: '宿舍空调使用3小时',
      aiResult: { category: '用电', carbonPerUnit: 0.8, confidence: 0.95, suggestion: '将空调温度调高1℃可节约10%用电' },
      date: '2026-05-26 22:00',
      createdAt: Date.now() - 72000000,
    },
    {
      id: 'r004',
      category: '废弃物',
      categoryIcon: '🗑️',
      carbonAmount: 1.5,
      quantity: '2kg',
      photo: '',
      notes: '未分类垃圾',
      aiResult: { category: '废弃物', carbonPerUnit: 0.75, confidence: 0.83, suggestion: '进行垃圾分类可减少填埋产生的甲烷排放' },
      date: '2026-05-26 18:00',
      createdAt: Date.now() - 86400000,
    },
    {
      id: 'r005',
      category: '购物',
      categoryIcon: '🛒',
      carbonAmount: 12.0,
      quantity: '1件',
      photo: '',
      notes: '购买新衣服',
      aiResult: { category: '购物', carbonPerUnit: 12.0, confidence: 0.78, suggestion: '购买二手衣物或选择环保品牌可减少碳足迹' },
      date: '2026-05-25 15:00',
      createdAt: Date.now() - 180000000,
    },
    {
      id: 'r006',
      category: '餐饮',
      categoryIcon: '🍜',
      carbonAmount: 3.5,
      quantity: '1份',
      photo: '',
      notes: '晚餐：蔬菜沙拉+鸡胸肉',
      aiResult: { category: '餐饮', carbonPerUnit: 3.5, confidence: 0.91, suggestion: '当前饮食结构较为健康，继续保持' },
      date: '2026-05-25 19:00',
      createdAt: Date.now() - 198000000,
    },
  ]
}

// ====== 绿色行动 Mock ======
export const mockGreenActions = [
  { id: 'walk', name: '步行出行', icon: '🚶', points: 10, unit: '次', description: '单次步行距离超过1公里即可打卡', totalCheckins: 1280, color: '#4CAF50' },
  { id: 'bike', name: '骑行出行', icon: '🚲', points: 15, unit: '次', description: '使用共享单车或自有单车出行', totalCheckins: 860, color: '#29B6F6' },
  { id: 'bus', name: '公交出行', icon: '🚌', points: 20, unit: '次', description: '乘坐公交/地铁代替私家车出行', totalCheckins: 2450, color: '#FF9800' },
  { id: 'recycle', name: '垃圾分类', icon: '♻️', points: 5, unit: '次', description: '将垃圾正确分类投放', totalCheckins: 3600, color: '#8BC34A' },
  { id: 'oldGoods', name: '旧物回收', icon: '📦', points: 30, unit: '次', description: '将旧衣物、旧书籍等捐赠或回收', totalCheckins: 520, color: '#FFB300' },
  { id: 'savePower', name: '节约用电', icon: '💡', points: 8, unit: '天', description: '离开教室/宿舍随手关灯关空调', totalCheckins: 4320, color: '#00BCD4' },
  { id: 'noPlastic', name: '自带水杯', icon: '🥤', points: 12, unit: '次', description: '使用自带水杯/餐具，拒绝一次性塑料', totalCheckins: 1890, color: '#E91E63' },
  { id: 'plantTree', name: '植树护绿', icon: '🌳', points: 50, unit: '棵', description: '参与校园植树或绿地维护活动', totalCheckins: 310, color: '#2E7D32' },
  { id: 'vegan', name: '素食一餐', icon: '🥬', points: 8, unit: '餐', description: '选择素食餐食，减少畜牧碳排放', totalCheckins: 1560, color: '#66BB6A' },
  { id: 'stairs', name: '爬楼代替电梯', icon: '🏃', points: 5, unit: '次', description: '3层以内走楼梯代替电梯', totalCheckins: 4200, color: '#78909C' },
]

export function createMyCheckinHistory() {
  return [
    { id: 'c001', actionId: 'walk', actionName: '步行出行', icon: '🚶', points: 10, date: '2026-05-27 08:00' },
    { id: 'c002', actionId: 'recycle', actionName: '垃圾分类', icon: '♻️', points: 5, date: '2026-05-26 20:30' },
    { id: 'c003', actionId: 'savePower', actionName: '节约用电', icon: '💡', points: 8, date: '2026-05-26 22:00' },
    { id: 'c004', actionId: 'bike', actionName: '骑行出行', icon: '🚲', points: 15, date: '2026-05-25 07:45' },
    { id: 'c005', actionId: 'vegan', actionName: '素食一餐', icon: '🥬', points: 8, date: '2026-05-24 12:00' },
  ]
}

// ====== 积分中心 Mock ======
export function createMockTransactions() {
  return [
    { id: 't001', type: 'income', desc: '步行出行打卡奖励', points: 10, date: '2026-05-27 08:05', txHash: '0x7a3b...8f2c' },
    { id: 't002', type: 'income', desc: '垃圾分类打卡奖励', points: 5, date: '2026-05-26 20:35', txHash: '0x2d1f...a9b4' },
    { id: 't003', type: 'income', desc: '节约用电打卡奖励', points: 8, date: '2026-05-26 22:05', txHash: '0x9e4c...1d7a' },
    { id: 't004', type: 'expense', desc: '兑换：环保帆布袋', points: -200, date: '2026-05-25 14:20', txHash: '0x5b3e...c6f2' },
    { id: 't005', type: 'income', desc: '骑行出行打卡奖励', points: 15, date: '2026-05-25 07:50', txHash: '0x1a8d...e3b7' },
    { id: 't006', type: 'income', desc: '旧物回收打卡奖励', points: 30, date: '2026-05-24 16:30', txHash: '0x4f7c...9d2a' },
    { id: 't007', type: 'expense', desc: '兑换：校园咖啡券', points: -150, date: '2026-05-23 10:15', txHash: '0x8e2b...7a4c' },
    { id: 't008', type: 'income', desc: '素食一餐打卡奖励', points: 8, date: '2026-05-23 12:10', txHash: '0x3d6a...f1e9' },
  ]
}

export const mockRewards = [
  { id: 'rw001', name: '环保帆布袋', image: '🛍️', pointsCost: 200, stock: 50, description: '可重复使用的棉质帆布袋，减少塑料袋使用', category: '生活用品' },
  { id: 'rw002', name: '校园咖啡券', image: '☕', pointsCost: 150, stock: 100, description: '校内咖啡厅任意饮品兑换券', category: '餐饮' },
  { id: 'rw003', name: '不锈钢吸管套装', image: '🥤', pointsCost: 180, stock: 80, description: '含4支不锈钢吸管+清洁刷，告别一次性吸管', category: '生活用品' },
  { id: 'rw004', name: '校园文创笔记本', image: '📓', pointsCost: 250, stock: 60, description: '再生纸笔记本，校园限定封面', category: '文具' },
  { id: 'rw005', name: '多肉植物盆栽', image: '🪴', pointsCost: 120, stock: 40, description: '桌面小绿植，清新空气减碳排', category: '绿植' },
  { id: 'rw006', name: '图书馆优先选座卡', image: '📚', pointsCost: 500, stock: 30, description: '考试周可优先选择图书馆座位', category: '校园特权' },
  { id: 'rw007', name: '校园食堂代金券5元', image: '🎫', pointsCost: 100, stock: 200, description: '校内各食堂通用', category: '餐饮' },
  { id: 'rw008', name: '运动水壶', image: '🏺', pointsCost: 300, stock: 45, description: '750ml大容量运动水壶，BPA-free', category: '生活用品' },
]

export function createRedeemHistory() {
  return [
    { id: 'rd001', rewardName: '环保帆布袋', image: '🛍️', pointsCost: 200, redeemDate: '2026-05-25 14:20', status: 'completed', code: 'GFT202605251420' },
    { id: 'rd002', rewardName: '校园咖啡券', image: '☕', pointsCost: 150, redeemDate: '2026-05-23 10:15', status: 'completed', code: 'GFT202605231015' },
  ]
}

// ====== 排行榜 Mock ======
export function createMockRankList(type) {
  const baseList = [
    { rank: 1, name: '李环保', avatar: '李', department: '环境科学与工程学院', score: 5200, carbonReduced: 280.5 },
    { rank: 2, name: '王低碳', avatar: '王', department: '计算机科学与技术学院', score: 4850, carbonReduced: 245.3 },
    { rank: 3, name: '陈绿行', avatar: '陈', department: '软件学院', score: 4500, carbonReduced: 210.8 },
  ]

  const rest = []
  for (let i = 4; i <= 20; i++) {
    rest.push({
      rank: i,
      name: `同学${i}`,
      avatar: String(i),
      department: ['计算机科学与技术学院', '软件学院', '经济管理学院', '外国语学院', '环境科学与工程学院'][i % 5],
      score: Math.floor(4500 - i * 180 + Math.random() * 50),
      carbonReduced: +(210 - i * 10 + Math.random() * 8).toFixed(1),
    })
  }

  return [...baseList, ...rest]
}

export function createMockClassRankList() {
  return [
    { rank: 1, className: '环境工程2201班', department: '环境科学与工程学院', studentCount: 35, perCapitaPoints: 1680, perCapitaCarbon: 84.5 },
    { rank: 2, className: '软件工程2401班', department: '软件学院', studentCount: 40, perCapitaPoints: 1520, perCapitaCarbon: 76.2 },
    { rank: 3, className: '计科2302班', department: '计算机科学与技术学院', studentCount: 38, perCapitaPoints: 1450, perCapitaCarbon: 72.8 },
    { rank: 4, className: '通信工程2201班', department: '信息与通信工程学院', studentCount: 36, perCapitaPoints: 1380, perCapitaCarbon: 69.1 },
    { rank: 5, className: '金融2203班', department: '经济管理学院', studentCount: 42, perCapitaPoints: 1320, perCapitaCarbon: 65.4 },
    { rank: 6, className: '英语2202班', department: '外国语学院', studentCount: 30, perCapitaPoints: 1260, perCapitaCarbon: 62.3 },
    { rank: 7, className: '法学2301班', department: '法学院', studentCount: 33, perCapitaPoints: 1180, perCapitaCarbon: 58.7 },
    { rank: 8, className: '化工2201班', department: '化学化工学院', studentCount: 31, perCapitaPoints: 1120, perCapitaCarbon: 55.2 },
    { rank: 9, className: '生科2301班', department: '生命科学学院', studentCount: 28, perCapitaPoints: 1050, perCapitaCarbon: 51.6 },
    { rank: 10, className: '物理2201班', department: '物理学院', studentCount: 32, perCapitaPoints: 980, perCapitaCarbon: 48.9 },
    { rank: 11, className: '数学2202班', department: '数学与统计学院', studentCount: 34, perCapitaPoints: 920, perCapitaCarbon: 45.3 },
    { rank: 12, className: '电子2203班', department: '电子工程学院', studentCount: 37, perCapitaPoints: 860, perCapitaCarbon: 42.1 },
    { rank: 13, className: '马院2201班', department: '马克思主义学院', studentCount: 25, perCapitaPoints: 780, perCapitaCarbon: 38.5 },
    { rank: 14, className: '自动化2301班', department: '电子工程学院', studentCount: 36, perCapitaPoints: 720, perCapitaCarbon: 35.2 },
    { rank: 15, className: '大数据2201班', department: '计算机科学与技术学院', studentCount: 39, perCapitaPoints: 650, perCapitaCarbon: 31.8 },
  ]
}

// ====== 首页仪表盘数据 ======
export const mockDashboardData = {
  dailySummary: {
    todayCarbon: 6.8,
    yesterdayCarbon: 8.2,
    carbonChange: -17.1,
    todayPoints: 35,
    totalPoints: 1280,
    weeklyCarbon: 48.5,
    weeklyChange: -12.3,
    goalCarbon: 150,
    goalProgress: 67.7,
    collegeRank: 12,
    collegeTotal: 68,
  },
  quickActions: [
    { id: 'walk', name: '步行出行', icon: 'walk', points: 10, color: '#4CAF50' },
    { id: 'bike', name: '骑行出行', icon: 'bike', points: 15, color: '#29B6F6' },
    { id: 'bus', name: '公交出行', icon: 'bus', points: 20, color: '#FF9800' },
    { id: 'recycle', name: '垃圾分类', icon: 'recycle', points: 5, color: '#8BC34A' },
    { id: 'oldGoods', name: '旧物回收', icon: 'goods', points: 30, color: '#FFB300' },
    { id: 'savePower', name: '节约用电', icon: 'power', points: 8, color: '#00BCD4' },
    { id: 'noPlastic', name: '拒绝一次性塑料', icon: 'plastic', points: 12, color: '#E91E63' },
    { id: 'plantTree', name: '植树护绿', icon: 'tree', points: 50, color: '#2E7D32' },
  ],
  carbonTrend: [
    { date: '05-21', carbon: 9.2 },
    { date: '05-22', carbon: 8.5 },
    { date: '05-23', carbon: 7.8 },
    { date: '05-24', carbon: 6.3 },
    { date: '05-25', carbon: 8.1 },
    { date: '05-26', carbon: 8.2 },
    { date: '05-27', carbon: 6.8 },
  ],
}

// 生成登录响应
export function createLoginResponse(userInfo) {
  return {
    code: 200,
    message: '登录成功',
    data: {
      token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock-token-for-dev',
      refreshToken: 'mock-refresh-token',
      expiresIn: 86400,
      userInfo: { ...userInfo },
    },
  }
}

// ====== AI 分析模拟结果 ======
export function mockAiAnalyze() {
  const results = [
    { category: '餐饮', categoryIcon: '🍜', carbonPerUnit: +(3 + Math.random() * 6).toFixed(1), unit: '份', confidence: 0.88, suggestion: '减少红肉消费，增加植物性饮食比例可显著降低碳足迹' },
    { category: '出行', categoryIcon: '🚗', carbonPerUnit: +(0.5 + Math.random() * 1.5).toFixed(2), unit: '公里', confidence: 0.91, suggestion: '选择公共交通或非机动车出行，碳排放可降低70%以上' },
    { category: '废弃物', categoryIcon: '🗑️', carbonPerUnit: +(0.5 + Math.random() * 2).toFixed(1), unit: 'kg', confidence: 0.85, suggestion: '正确分类垃圾可将填埋产生的温室气体减少30%' },
    { category: '用电', categoryIcon: '💡', carbonPerUnit: +(0.5 + Math.random() * 1.5).toFixed(1), unit: '度', confidence: 0.93, suggestion: '使用节能模式，离开时关闭电源，每度电减少0.8kg碳排放' },
    { category: '购物', categoryIcon: '🛒', carbonPerUnit: +(5 + Math.random() * 15).toFixed(1), unit: '件', confidence: 0.76, suggestion: '选择二手商品或耐用产品，延长物品使用寿命' },
  ]
  return results[Math.floor(Math.random() * results.length)]
}

// ====== 数据可视化 Mock ======
export const mockCarbonBreakdown = [
  { name: '餐饮', value: 35.2, color: '#FF9800' },
  { name: '出行', value: 28.6, color: '#29B6F6' },
  { name: '用电', value: 18.4, color: '#FFB300' },
  { name: '废弃物', value: 10.1, color: '#8BC34A' },
  { name: '购物', value: 7.7, color: '#E91E63' },
]

export const mockCampusCompare = [
  { label: '餐饮', me: 35.2, avg: 42.5 },
  { label: '出行', me: 28.6, avg: 45.3 },
  { label: '用电', me: 18.4, avg: 25.8 },
  { label: '废弃物', me: 10.1, avg: 15.2 },
  { label: '购物', me: 7.7, avg: 18.6 },
]

// ====== 管理后台 Mock ======
export const mockAdminStats = {
  totalUsers: 2840,
  activeToday: 326,
  totalCheckins: 15280,
  pendingAudits: 18,
  totalCarbonReduced: 6840.5,
  totalPointsIssued: 325800,
  onChainTxCount: 4520,
}

export function createMockPendingAudits() {
  return [
    { id: 'a001', studentName: '陈同学', studentId: '2023005', actionName: '光盘打卡', date: '2026-06-02 12:30', photo: '', aiConfidence: 0.72, status: 'pending' },
    { id: 'a002', studentName: '刘同学', studentId: '2024012', actionName: '垃圾回收', category: 'bottle', date: '2026-06-02 11:15', photo: '', aiConfidence: 0.65, status: 'pending' },
    { id: 'a003', studentName: '赵同学', studentId: '2022018', actionName: '光盘打卡', date: '2026-06-02 10:00', photo: '', aiConfidence: 0.58, status: 'pending' },
    { id: 'a004', studentName: '周同学', studentId: '2023009', actionName: '垃圾回收', category: 'battery', date: '2026-06-01 16:45', photo: '', aiConfidence: 0.70, status: 'pending' },
    { id: 'a005', studentName: '林同学', studentId: '2024015', actionName: '光盘打卡', date: '2026-06-01 13:00', photo: '', aiConfidence: 0.61, status: 'pending' },
    { id: 'a006', studentName: '吴同学', studentId: '2022007', actionName: '垃圾回收', category: 'paper', date: '2026-06-01 09:20', photo: '', aiConfidence: 0.74, status: 'pending' },
  ]
}

export function createMockUserList() {
  return [
    { id: 'u001', studentId: '2024001001', name: '张同学', department: '计算机科学与技术学院', className: '软件工程2401班', points: 1280, carbonReduced: 64.0, status: 'active', joinDate: '2025-03-01' },
    { id: 'u002', studentId: '2023005001', name: '李环保', department: '环境科学与工程学院', className: '环境工程2201班', points: 5200, carbonReduced: 280.5, status: 'active', joinDate: '2025-01-15' },
    { id: 'u003', studentId: '2024002001', name: '王低碳', department: '计算机科学与技术学院', className: '计科2302班', points: 4850, carbonReduced: 245.3, status: 'active', joinDate: '2025-02-20' },
    { id: 'u004', studentId: '2023006001', name: '陈绿行', department: '软件学院', className: '软件工程2202班', points: 4500, carbonReduced: 210.8, status: 'active', joinDate: '2025-03-10' },
    { id: 'u005', studentId: '2024003001', name: '刘同学', department: '经济管理学院', className: '金融2203班', points: 890, carbonReduced: 44.5, status: 'active', joinDate: '2025-04-01' },
    { id: 'u006', studentId: '2022001001', name: '赵同学', department: '法学院', className: '法学2201班', points: 560, carbonReduced: 28.0, status: 'frozen', joinDate: '2025-02-15' },
    { id: 'u007', studentId: '2024004001', name: '周同学', department: '信息与通信工程学院', className: '通信工程2301班', points: 720, carbonReduced: 36.0, status: 'active', joinDate: '2025-05-10' },
    { id: 'u008', studentId: '2023007001', name: '林同学', department: '外国语学院', className: '英语2202班', points: 1100, carbonReduced: 55.0, status: 'active', joinDate: '2025-03-20' },
    { id: 'u009', studentId: '2024005001', name: '吴同学', department: '化学化工学院', className: '化工2301班', points: 340, carbonReduced: 17.0, status: 'active', joinDate: '2025-06-01' },
    { id: 'u010', studentId: '2022002001', name: '郑同学', department: '数学与统计学院', className: '数学2202班', points: 260, carbonReduced: 13.0, status: 'inactive', joinDate: '2025-01-10' },
  ]
}

export const mockRadarData = [
  { name: '低碳出行', value: 75, max: 100 },
  { name: '垃圾分类', value: 60, max: 100 },
  { name: '节约能源', value: 80, max: 100 },
  { name: '绿色饮食', value: 45, max: 100 },
  { name: '减塑行动', value: 55, max: 100 },
  { name: '旧物利用', value: 35, max: 100 },
]
