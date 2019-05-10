<template>
  <div class="app-container">
    <eHeader :query="query"/>
    <!--表格渲染-->
    <el-table v-loading="loading" :data="data" size="small" border style="width: 100%;">
      <el-table-column prop="username" label="姓名"/>
      <el-table-column prop="sex" label="性别"/>
      <el-table-column prop="idCard" label="身份证号码"/>
      <el-table-column prop="address" label="住址"/>
      <el-table-column prop="type" label="人员类型"/>
      <el-table-column prop="oldTime" label="年龄段"/>

      <el-table-column prop="image" label="图片">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="lookimg(scope.row.image)">查看图片</el-button>
        </template>
      </el-table-column>
      <el-dialog :visible.sync="picbox" append-to-body title="查看图片" width="500px" style="text-align:center">
        <img v-if="picbox" :src="bigimg" alt="">
      </el-dialog>

      <el-table-column prop="createTime" label="创建时间">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150px" align="center">
        <template slot-scope="scope">
          <edit
            v-if="checkPermission(['FACE_ALL','FACE_EDIT','ADMIN'])"
            :data="scope.row"
            :roles="roles"
            :sup_this="sup_this"/>
          <el-button v-else disabled size="mini" type="success">编辑</el-button>
          `

          <el-popover
            v-if="checkPermission(['FACE_ALL','FACE_DELETE','ADMIN'])"
            v-model="scope.row.delPopover"
            placement="top"
            width="180">
            <p>确定删除本条数据吗？</p>
            <div style="text-align: right; margin: 0">
              <el-button size="mini" type="text" @click="scope.row.delPopover = false">取消</el-button>
              <el-button :loading="delLoading" type="primary" size="mini" @click="subDelete(scope.$index, scope.row)">
                确定
              </el-button>
            </div>
            <el-button slot="reference" type="danger" size="mini" @click="scope.row.delPopover = true">删除</el-button>
          </el-popover>
          <el-button v-else slot="reference" disabled type="danger" size="mini" @click="scope.row.delPopover = true">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <el-pagination
      :total="total"
      style="margin-top: 8px;"
      layout="total, prev, pager, next, sizes"
      @size-change="sizeChange"
      @current-change="pageChange"/>
  </div>
</template>

<script>
import checkPermission from '@/utils/permission'
import initData from '../../../mixins/initData'
import { del } from '@/api/userData'
// import { getRoleTree } from '@/api/role'
import { parseTime } from '@/utils/index'
import eHeader from './module/header'
import edit from './module/edit'

export default {
  components: { eHeader, edit },
  mixins: [initData],
  data() {
    return {
      roles: [], delLoading: false, sup_this: this,
      picbox: false,
      bigimg: ''
    }
  },
  created() {
    // this.getRoles()
    this.$nextTick(() => {
      this.init()
    })
  },
  methods: {
    parseTime,
    checkPermission,
    beforeInit() {
      this.url = 'api/userData'
      const sort = 'id,desc'
      const query = this.query
      const type = query.type
      const value = query.value
      const enabled = query.enabled
      this.params = { page: this.page, size: this.size, sort: sort }
      if (type && value) {
        this.params[type] = value
      }
      if (enabled !== '' && enabled !== null) {
        this.params['enabled'] = enabled
      }
      return true
    },
    subDelete(index, row) {
      this.delLoading = true
      del(row.id).then(res => {
        this.delLoading = false
        row.delPopover = false
        this.init()
        this.$notify({
          title: '删除成功',
          type: 'success',
          duration: 2500
        })
      }).catch(err => {
        this.delLoading = false
        row.delPopover = false
        console.log(err.response.data.message)
      })
    },
    lookimg(imglook) {
      this.picbox = true
      this.bigimg = imglook
    }
  }
}
</script>

<style scoped>

</style>
