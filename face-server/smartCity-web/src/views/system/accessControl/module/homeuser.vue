<template>
  <div class="app-container">
    <!--表格渲染-->
    <el-table   size="small" border style="width: 600px;">
      <el-table-column label="住户姓名"/>
      <el-table-column label="性别"/>
      <el-table-column label="身份证号码"/>
      <el-table-column label="住址"/>
      <el-table-column label="人员类型"/>
      <el-table-column label="年龄段"/>
    </el-table>
  </div>
</template>

<script>
// import checkPermission from '@/utils/permission'
// import initData from '../../../mixins/initData'
// import { del } from '@/api/userData'
// import { getRoleTree } from '@/api/role'
// import { parseTime } from '@/utils/index'
// import eHeader from './module/header'
// import edit from './module/edit'
export default {
//   components: { eHeader, edit },
//   mixins: [initData],
//   data() {
//     return {
//       roles: [], delLoading: false, sup_this: this,
//       picbox:false
//     }
//   },
//   created() {
//     this.getRoles()
//     this.$nextTick(() => {
//       this.init()
//     })
//   },
//   methods: {
//     parseTime,
//     checkPermission,
//     beforeInit() {
//       this.url = 'api/userData'
//       const sort = 'id,desc'
//       const query = this.query
//       const type = query.type
//       const value = query.value
//       const enabled = query.enabled
//       this.params = { page: this.page, size: this.size, sort: sort }
//       if (type && value) { this.params[type] = value }
//       if (enabled !== '' && enabled !== null) { this.params['enabled'] = enabled }
//       return true
//     },
//     subDelete(index, row) {
//       this.delLoading = true
//       del(row.id).then(res => {
//         this.delLoading = false
//         row.delPopover = false
//         this.init()
//         this.$notify({
//           title: '删除成功',
//           type: 'success',
//           duration: 2500
//         })
//       }).catch(err => {
//         this.delLoading = false
//         row.delPopover = false
//         console.log(err.response.data.message)
//       })
//     },
//     getRoles() {
//       getRoleTree().then(res => {
//         // console.log(res);
//         this.roles = res
//       })
//     }
//   }
}
</script>

<style scoped>

</style>
