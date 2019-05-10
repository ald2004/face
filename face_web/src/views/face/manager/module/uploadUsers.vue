<template>
  <div>

    <el-upload
      v-if="checkPermission(['ADMIN','FACE_USER_ALL','FACE_USER_CREATE'])"
      :show-file-list="false"
      :before-upload="handlebeforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :headers="headers"
      :action="uploadFaceUsersPicApi">
      <!--导入-->
      <el-button
        :loading="uploadLoading"
        class="filter-item"
        size="mini"
        type="danger"
        icon="el-icon-upload2">批量导入
      </el-button>
    </el-upload>
  </div>
</template>

<script>
  import checkPermission from '@/utils/permission' // 权限判断函数
  import { getToken } from '@/utils/auth'
  import { mapGetters } from 'vuex'

  export default {
    data() {
      return {
        headers: {
          'Authorization': 'Bearer ' + getToken()
        },
        dialogg: false,
        dialogVisible: false,
        uploadLoading: false,
        loading: null
      }
    },
    computed: {
      ...mapGetters([
        'uploadFaceUsersPicApi'
      ])
    },
    methods: {
      checkPermission,
      handleSuccess(response, file, fileList) {
        this.uploadLoading = false
        if (this.loading) {
          this.loading.close()
        }
        for (var i = 0; i < response.errMsg.length; i++) {
          this.$message({
            showClose: true,
            duration: 0,
            message: '导入失败：' + response.errMsg[i],
            type: 'error'
          })
          /*this.$notify.error({
            title: '批量导入错误',
            duration: 0,
            message: response.errMsg[i]
          })*/
        }
        if (response.errMsg.length === 0) {
          this.$message({
            showClose: true,
            duration: 0,
            message: '恭喜你，已导入成功！',
            type: 'success'
          })
          this.$parent.reload()
          // this.$emit('reload')
        }
      },
      // 上传前 删除所有图片
      handlebeforeUpload(file, fileList) {
        this.openFullScreen()
        this.uploadLoading = true
      },
      handleBeforeRemove(file, fileList) {
      },
      handlePictureCardPreview(file) {
      },
      // 刷新列表数据
      doSubmit() {
      },
      // 监听上传失败
      handleError(e, file, fileList) {
        this.uploadLoading = false
        if (this.loading) {
          this.loading.close()
        }
        const msg = JSON.parse(e.message)
        this.$notify({
          title: msg.message,
          type: 'error',
          duration: 2500
        })
      }, openFullScreen() {
        this.loading = this.$loading({
          lock: true,
          text: '正在批量导入中，过程较慢请耐心等待，请勿刷新页面。',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })
      }
    }
  }
</script>

<style scoped>

</style>
