<template>
  <div class="app-container">
    <eHeader :roles="roles" :delh="deletes"  :query="query"/>
    <!--表格渲染-->
    <el-table v-loading="loading" :data="data" size="small" border @selection-change="handleSelectionChange" style="width: 100%;">
      <el-table-column
      
      type="selection"
      width="55"
      >
    </el-table-column>
      <el-table-column prop="name" label="姓名"/>
      <el-table-column label="原始图像">
        <template slot-scope="scope">
          <img :src="scope.row.photo.startsWith('http')?scope.row.photo:baseUrl+'/'+scope.row.photo"
               class="el-avatar">
        </template>
      </el-table-column>
      <el-table-column label="人脸图像">
        <template slot-scope="scope">
          <img :src="scope.row.facePhoto.startsWith('http')?scope.row.facePhoto:baseUrl+'/'+scope.row.facePhoto"
               class="el-avatar">
        </template>
      </el-table-column>
      <el-table-column prop="idCard" label="身份证"/>
      <el-table-column prop="phone" label="手机号"/>
      <el-table-column label="状态">
        <template slot-scope="scope">
          <span v-if="scope.row.status === 0 " class="badge" style="font-weight: bold">禁用</span>
          <span v-else-if="scope.row.status === 1 " class="badge badge-bg-orange" style="font-weight: bold">黑名单</span>
          <span v-else class="badge badge-bg-green" style="font-weight: bold">白名单</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册日期">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="des" label="附加描述"/>
      <el-table-column label="操作" width="150px" align="center">
        <template slot-scope="scope">
          <edit
            v-if="checkPermission(['ADMIN','FACE_USER_ALL','FACE_USER_EDIT'])"
            :data="scope.row"
            :roles="roles"
            :sup_this="sup_this"/>
          <el-button v-else disabled size="mini" type="success">编辑</el-button>
          <el-popover
            v-if="checkPermission(['ADMIN','FACE_USER_ALL','FACE_USER_DELETE'])"
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
          <el-button v-else disabled slot="reference" type="danger" size="mini" >删除</el-button>
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
  import { del } from '@/api/face-user'
  import { getRoleTree } from '@/api/role'
  import { parseTime } from '@/utils/index'
  import eHeader from './module/header'
  import edit from './module/edit'

  export default {
    components: { eHeader, edit },
    mixins: [initData],
    data() {
      return {
        roles: [], delLoading: false, sup_this: this,
        deletes:"",//批量删除，存储id的字符串
      }
    },
    created() {
      this.getRoles();
      this.$nextTick(() => {
        this.init()
      })
    },
    methods: {
      parseTime,
      checkPermission,
      beforeInit() {
        this.url = 'api/face/users'
        this.baseUrl = process.env.BASE_API
        const sort = 'id,desc'
        const query = this.query
        const type = query.type
        const value = query.value
        const status = query.status
        this.params = { page: this.page, size: this.size, sort: sort }
        if (type && value) {
          this.params[type] = value
        }
        if (status !== '' && status !== null) {
          this.params['status'] = status
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
      getRoles() {
        getRoleTree().then(res => {
          this.roles = res
        })
      },
      handleSelectionChange(val) {
      console.log(val);
      this.deletes = "";
      for (let i = 0; i < val.length; i++) {
        this.deletes += val[i].id + ",";
      }
      this.deletes = this.deletes.substring(0, this.deletes.length - 1);
    }
    }
  }
</script>

<style scoped>

</style>
