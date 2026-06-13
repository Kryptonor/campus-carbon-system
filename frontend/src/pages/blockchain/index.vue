<template>
  <view class="page">
    <scroll-view class="bc-scroll" scroll-y :show-scrollbar="false">
      <!-- Network Status Banner -->
      <view class="network-banner">
        <view class="banner-bg"></view>
        <view class="banner-content">
          <view class="banner-top">
            <view class="chain-badge">
              <text class="chain-icon">⛓️</text>
              <text class="chain-name">FISCO BCOS</text>
            </view>
            <view class="status-dot-wrap">
              <view class="status-dot"></view>
              <text class="status-text">{{ contractInfo.networkStatus || '运行中' }}</text>
            </view>
          </view>
          <text class="banner-title">链上积分存证系统</text>
          <text class="banner-desc">基于 FISCO BCOS 联盟链，保障绿色积分安全、透明、不可篡改</text>
        </view>
      </view>

      <!-- On-chain Balance Card -->
      <view class="balance-section card">
        <view class="section-head">
          <text class="section-title">链上资产</text>
          <text class="addr-text">{{ shortAddr }}</text>
        </view>
        <view class="balance-grid">
          <view class="bal-item">
            <text class="bal-label">链上积分</text>
            <text class="bal-value primary">{{ balance.onChainBalance }}</text>
          </view>
          <view class="bal-item">
            <text class="bal-label">待上链积分</text>
            <text class="bal-value warn">{{ balance.offChainBalance }}</text>
          </view>
          <view class="bal-item full">
            <text class="bal-label">积分总额</text>
            <text class="bal-value total">{{ balance.totalBalance }}</text>
          </view>
        </view>
      </view>

      <!-- Contract Info Card -->
      <view class="contract-section card">
        <view class="section-head">
          <text class="section-title">合约信息</text>
        </view>
        <view class="info-rows">
          <view class="info-row">
            <text class="info-label">链网络</text>
            <text class="info-value">{{ contractInfo.chainName || 'FISCO BCOS 校园联盟链' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">积分合约</text>
            <text class="info-value mono" @tap="copyAddr(contractInfo.pointsToken)">{{ formatAddr(contractInfo.pointsToken) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">行为合约</text>
            <text class="info-value mono" @tap="copyAddr(contractInfo.actionLedger)">{{ formatAddr(contractInfo.actionLedger) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">当前区块</text>
            <text class="info-value">{{ contractInfo.blockHeight || '—' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">链上交易数</text>
            <text class="info-value">{{ contractInfo.txCount || '—' }}</text>
          </view>
        </view>
      </view>

      <!-- Recent Blockchain Transactions -->
      <view class="tx-section card">
        <view class="section-head">
          <text class="section-title">链上交易记录</text>
          <text class="section-sub" @tap="refreshTx">刷新</text>
        </view>
        <view v-if="transactions.length === 0" class="empty-state">
          <text class="empty-text">暂无链上交易记录</text>
        </view>
        <view v-for="tx in transactions" :key="tx.txHash" class="tx-row">
          <view class="tx-left">
            <view class="tx-icon-wrap">
              <text class="tx-icon">{{ tx.type === '积分发放' || tx.type === '奖励发放' ? '📥' : '📤' }}</text>
            </view>
            <view class="tx-info">
              <text class="tx-type-text">{{ tx.type }}</text>
              <text class="tx-hash" @tap="copyAddr(tx.txHash)">{{ tx.txHash }}</text>
            </view>
          </view>
          <view class="tx-right">
            <text class="tx-amount" :class="tx.amount > 0 ? 'income' : 'expense'">
              {{ tx.amount > 0 ? '+' : '' }}{{ tx.amount }}
            </text>
            <text class="tx-time">{{ formatTime(tx.timestamp) }}</text>
          </view>
        </view>
      </view>

      <!-- Carbon Action On-chain Records -->
      <view class="action-section card">
        <view class="section-head">
          <text class="section-title">碳减排行为上链</text>
          <text class="section-sub" @tap="refreshActions">刷新</text>
        </view>
        <view v-if="actionRecords.length === 0" class="empty-state">
          <text class="empty-text">暂无上链记录</text>
        </view>
        <view v-for="record in actionRecords" :key="record.id" class="action-row">
          <view class="action-left">
            <text class="action-icon">{{ record.icon || '🌿' }}</text>
            <view class="action-info">
              <text class="action-name">{{ record.actionName }}</text>
              <text class="action-date">{{ record.date }}</text>
            </view>
          </view>
          <view class="action-right">
            <view class="chain-badge-sm">
              <text class="chain-badge-icon">✅</text>
              <text class="chain-badge-text">已上链</text>
            </view>
            <text class="action-hash" @tap="copyAddr(record.txHash)">{{ record.txHash }}</text>
          </view>
        </view>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { blockchainService, formatAddress } from '@/utils/web3'

const userStore = useUserStore()

const balance = ref({ onChainBalance: 0, offChainBalance: 0, totalBalance: 0 })
const contractInfo = ref({})
const transactions = ref([])
const actionRecords = ref([])

const shortAddr = computed(() => formatAddress(userStore.walletAddress) || '未开通钱包')

function formatAddr(addr) {
  return formatAddress(addr) || '—'
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const M = String(d.getMonth() + 1).padStart(2, '0')
  const D = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${M}-${D} ${h}:${m}`
}

function copyAddr(addr) {
  if (!addr) return
  uni.setClipboardData({
    data: addr,
    success: () => uni.showToast({ title: '已复制', icon: 'success' }),
  })
}

async function loadData() {
  const addr = userStore.walletAddress
  const [balRes, contractRes, txRes, actionRes] = await Promise.all([
    blockchainService.getOnChainBalance(addr),
    blockchainService.getContractInfo(),
    blockchainService.getRecentTransactions(addr),
    blockchainService.getActionRecords(addr),
  ])
  if (balRes.code === 200) balance.value = balRes.data
  if (contractRes.code === 200) contractInfo.value = contractRes.data
  if (txRes.code === 200) transactions.value = txRes.data || []
  if (actionRes.code === 200) actionRecords.value = actionRes.data || []
}

function refreshTx() { loadData() }
function refreshActions() { loadData() }

onShow(() => loadData())
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; }
.bc-scroll { height: 100vh; padding: $space-md; }

/* ---- Network Banner ---- */
.network-banner {
  background: $bg-gradient;
  border-radius: $radius-lg;
  padding: $space-xl $space-lg;
  position: relative;
  overflow: hidden;
  margin-bottom: $space-md;
}
.banner-bg {
  position: absolute; right: -60rpx; top: -60rpx;
  width: 260rpx; height: 260rpx; border-radius: 50%;
  background: rgba(255,255,255,0.08);
}
.banner-content { position: relative; z-index: 1; }
.banner-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: $space-sm; }
.chain-badge {
  display: flex; align-items: center; gap: 6rpx;
  background: rgba(255,255,255,0.18); padding: 4rpx 16rpx; border-radius: $radius-round;
}
.chain-icon { font-size: 24rpx; }
.chain-name { font-size: $font-xs; color: #fff; font-weight: 500; }
.status-dot-wrap { display: flex; align-items: center; gap: 6rpx; }
.status-dot {
  width: 12rpx; height: 12rpx; border-radius: 50%;
  background: #69F0AE;
  box-shadow: 0 0 8rpx rgba(105, 240, 174, 0.6);
}
.status-text { font-size: $font-xs; color: rgba(255,255,255,0.85); }
.banner-title { color: #fff; font-size: $font-xl; font-weight: 700; display: block; margin-bottom: $space-xs; }
.banner-desc { color: rgba(255,255,255,0.75); font-size: $font-xs; line-height: 1.5; }

/* ---- Common Section ---- */
.section-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: $space-md;
}
.section-title { font-size: $font-lg; font-weight: 600; color: $text-primary; }
.section-sub { font-size: $font-xs; color: $primary-light; }
.addr-text { font-size: $font-xs; color: $text-light; font-family: monospace; }

/* ---- Balance ---- */
.balance-section { padding: $space-lg; }
.balance-grid { display: flex; flex-wrap: wrap; gap: $space-sm; }
.bal-item {
  flex: 1; min-width: 45%;
  background: $bg-color; border-radius: $radius-md; padding: $space-md;
  display: flex; flex-direction: column; gap: 4rpx;
  &.full { min-width: 100%; }
}
.bal-label { font-size: $font-xs; color: $text-light; }
.bal-value { font-size: $font-xl; font-weight: 700;
  &.primary { color: $primary; }
  &.warn { color: $accent-warm; }
  &.total { color: $text-primary; }
}

/* ---- Contract Info ---- */
.contract-section { padding: $space-lg; margin-bottom: $space-md; }
.info-rows { display: flex; flex-direction: column; }
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: $space-sm 0;
  border-bottom: 1rpx solid $border-color;
  &:last-child { border-bottom: none; }
}
.info-label { font-size: $font-sm; color: $text-light; }
.info-value { font-size: $font-sm; color: $text-primary; font-weight: 500;
  &.mono { font-family: monospace; color: $primary-light; font-size: $font-xs; }
}

/* ---- Transactions ---- */
.tx-section { padding: $space-lg; margin-bottom: $space-md; }
.tx-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: $space-sm 0;
  border-bottom: 1rpx solid $border-color;
  &:last-child { border-bottom: none; }
}
.tx-left { display: flex; align-items: center; gap: $space-sm; flex: 1; min-width: 0; }
.tx-icon-wrap {
  width: 56rpx; height: 56rpx; border-radius: 50%;
  background: $bg-color; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.tx-icon { font-size: 28rpx; }
.tx-info { display: flex; flex-direction: column; min-width: 0; }
.tx-type-text { font-size: $font-sm; color: $text-primary; font-weight: 500; }
.tx-hash { font-size: 20rpx; color: $text-light; font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tx-right { display: flex; flex-direction: column; align-items: flex-end; flex-shrink: 0; margin-left: $space-sm; }
.tx-amount { font-size: $font-md; font-weight: 700;
  &.income { color: $success; }
  &.expense { color: $danger; }
}
.tx-time { font-size: $font-xs; color: $text-light; }

/* ---- Action Records ---- */
.action-section { padding: $space-lg; margin-bottom: $space-md; }
.action-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: $space-sm 0;
  border-bottom: 1rpx solid $border-color;
  &:last-child { border-bottom: none; }
}
.action-left { display: flex; align-items: center; gap: $space-sm; }
.action-icon { font-size: 36rpx; }
.action-info { display: flex; flex-direction: column; }
.action-name { font-size: $font-sm; color: $text-primary; font-weight: 500; }
.action-date { font-size: $font-xs; color: $text-light; }
.action-right { display: flex; flex-direction: column; align-items: flex-end; }
.chain-badge-sm {
  display: flex; align-items: center; gap: 4rpx;
  background: #E8F5E9; padding: 2rpx 12rpx; border-radius: $radius-round;
  margin-bottom: 4rpx;
}
.chain-badge-icon { font-size: 20rpx; }
.chain-badge-text { font-size: 20rpx; color: $primary; font-weight: 500; }
.action-hash { font-size: 20rpx; color: $text-light; font-family: monospace; }

/* ---- Empty ---- */
.empty-state { padding: $space-xl 0; text-align: center; }
.empty-text { font-size: $font-sm; color: $text-light; }

.safe-bottom { height: calc(env(safe-area-inset-bottom, 30rpx) + 30rpx); }
</style>
