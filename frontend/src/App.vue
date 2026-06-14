<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'

onLaunch(() => {
  const userStore = useUserStore()
  userStore.checkLogin()
})
</script>

<style lang="scss">
@import '@/uni.scss';

page {
  background-color: $bg-color;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: $text-primary;
  font-size: 28rpx;
  line-height: 1.6;
  overflow-x: hidden;
  max-width: 100vw;
  box-sizing: border-box;
}

/* 底部导航栏字号加大 */
.uni-tabbar,
uni-tabbar,
.uni-tabbar__item,
.uni-tabbar .uni-tabbar__label {
  font-size: 24rpx !important;
}

/* 防止横向溢出 */
scroll-view,
.uni-scroll-view {
  box-sizing: border-box;
  max-width: 100vw;
}
</style>
