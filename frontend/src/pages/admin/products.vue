<template>
  <view class="page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="products-grid">
        <view v-for="p in products" :key="p.id" class="product-card card">
          <text class="prod-image">{{ p.imageUrl || '📦' }}</text>
          <text class="prod-name">{{ p.name }}</text>
          <view class="prod-meta">
            <text class="prod-cost">{{ p.pricePoints }} 积分</text>
            <text class="prod-stock" :class="{ low: p.stock <= 30 }">库存 {{ p.stock }}</text>
          </view>
          <view class="prod-actions">
            <button class="btn-edit" @tap="editProduct(p)">编辑</button>
          </view>
        </view>
      </view>

      <!-- Add Button -->
      <button class="add-btn" @tap="addProduct">+ 添加商品</button>

      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- Edit/Add Modal -->
    <view class="modal-overlay" v-if="showModal" @tap="showModal = false">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">{{ editing.id ? '编辑商品' : '添加商品' }}</text>
        <view class="modal-form">
          <view class="form-item">
            <text class="form-label">商品名称</text>
            <input class="form-input" v-model="editing.name" placeholder="请输入名称" />
          </view>
          <view class="form-item">
            <text class="form-label">图片地址</text>
            <input class="form-input" v-model="editing.imageUrl" placeholder="如 https://example.com/img.jpg" />
          </view>
          <view class="form-row">
            <view class="form-item half">
              <text class="form-label">所需积分</text>
              <input class="form-input" v-model.number="editing.pricePoints" type="number" placeholder="0" />
            </view>
            <view class="form-item half">
              <text class="form-label">库存</text>
              <input class="form-input" v-model.number="editing.stock" type="number" placeholder="0" />
            </view>
          </view>
          <view class="form-item">
            <text class="form-label">描述</text>
            <textarea class="form-textarea" v-model="editing.description" placeholder="商品描述" />
          </view>
        </view>
        <view class="modal-actions">
          <button v-if="editing.id" class="btn-delete" @tap="deleteProduct">删除</button>
          <button class="btn-cancel" @tap="showModal = false">取消</button>
          <button class="btn-confirm" @tap="handleSave">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/api/request'

const products = ref([])
const showModal = ref(false)
const editing = reactive({
  id: '', name: '', imageUrl: '', pricePoints: 0, stock: 0, description: '',
})

onShow(() => loadData())

async function loadData() {
  const res = await request({ url: '/products', method: 'GET' })
  if (res.code === 200) products.value = res.data
}

function addProduct() {
  Object.assign(editing, { id: '', name: '', imageUrl: '', pricePoints: 0, stock: 0, description: '' })
  showModal.value = true
}

function editProduct(p) {
  Object.assign(editing, { ...p })
  showModal.value = true
}

async function handleSave() {
  const isUpdate = !!editing.id
  const url = isUpdate ? `/products/${editing.id}` : '/products'
  const res = await request({
    url,
    method: isUpdate ? 'PUT' : 'POST',
    data: {
      name: editing.name,
      description: editing.description,
      pricePoints: editing.pricePoints,
      stock: editing.stock,
      imageUrl: editing.imageUrl,
    },
  })
  if (res.code === 200) {
    uni.showToast({ title: '保存成功', icon: 'success' })
    showModal.value = false
    loadData()
  } else {
    uni.showToast({ title: res.message || '保存失败', icon: 'none' })
  }
}

async function deleteProduct() {
  uni.showModal({
    title: '确认删除',
    content: `确定删除「${editing.name}」吗？`,
    confirmColor: '#F44336',
    success: async (r) => {
      if (r.confirm) {
        const res = await request({ url: `/products/${editing.id}`, method: 'DELETE' })
        if (res.code === 200) {
          uni.showToast({ title: '已删除', icon: 'success' })
          showModal.value = false
          loadData()
        }
      }
    },
  })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: $bg-color; }
.page-scroll { height: 100vh; padding: $space-md; }

.products-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: $space-sm; }
.product-card { padding: $space-md; display: flex; flex-direction: column; align-items: center; gap: 6rpx; }
.prod-image { font-size: 48rpx; }
.prod-name { font-size: $font-sm; font-weight: 600; color: $text-primary; }
.prod-meta { display: flex; gap: $space-sm; align-items: center; }
.prod-cost { font-size: $font-xs; color: $accent-warm; font-weight: 700; }
.prod-stock { font-size: 20rpx; color: $text-secondary; &.low { color: $danger; } }
.prod-cat { font-size: 20rpx; color: $text-light; }
.btn-edit { width: 100%; height: 56rpx; background: #E8F5E9; color: $primary; border-radius: $radius-sm; border: none; font-size: $font-xs; }

.add-btn {
  width: 100%; height: 88rpx; margin-top: $space-lg; background: #fff; border-radius: $radius-md;
  color: $primary; font-size: $font-md; font-weight: 600; border: 2rpx dashed $primary-light;
  display: flex; align-items: center; justify-content: center;
}

/* Modal */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center;
  justify-content: center; z-index: 999; padding: $space-xl;
}
.modal-card { background: #fff; border-radius: $radius-lg; padding: $space-xl; width: 100%; max-width: 600rpx; }
.modal-title { font-size: $font-lg; font-weight: 700; color: $text-primary; text-align: center; display: block; margin-bottom: $space-lg; }
.modal-form { .form-item { margin-bottom: $space-md; } }
.form-label { font-size: $font-sm; color: $text-secondary; display: block; margin-bottom: $space-xs; }
.form-input { width: 100%; height: 80rpx; background: $bg-color; border-radius: $radius-sm; padding: 0 $space-sm; font-size: $font-md; box-sizing: border-box; }
.form-textarea { width: 100%; height: 120rpx; background: $bg-color; border-radius: $radius-sm; padding: $space-sm; font-size: $font-sm; box-sizing: border-box; }
.form-row { display: flex; gap: $space-sm; }
.half { flex: 1; }

.modal-actions { display: flex; gap: $space-md; margin-top: $space-lg; }
.btn-delete { flex: 1; height: 80rpx; background: #FFEBEE; color: $danger; border-radius: $radius-md; border: none; font-size: $font-md; }
.btn-cancel { flex: 1; height: 80rpx; background: $bg-color; color: $text-secondary; border-radius: $radius-md; border: none; font-size: $font-md; }
.btn-confirm { flex: 1; height: 80rpx; background: $bg-gradient; color: #fff; border-radius: $radius-md; border: none; font-size: $font-md; }

.safe-bottom { height: calc($safe-bottom + 40rpx); }
</style>
