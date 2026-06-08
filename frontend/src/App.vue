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
}
</style>
