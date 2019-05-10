<template>
  <div>
    <el-upload
      ref="upload"
      :on-preview="handlePictureCardPreview"
      :before-remove="handleBeforeRemove"
      :beforeUpload="handlebeforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :headers="headers"
      :file-list="fileList"
      :action="uploadFacePicApi"
      list-type="picture-card">
      <i class="el-icon-plus"/>
    </el-upload>
    <img :src="dialogImageUrl" width="100%" alt="">
  </div>
</template>

<script>
  import { getToken } from '@/utils/auth'
  import { mapGetters } from 'vuex'
  import { del } from '@/api/picture'

  export default {
    data() {
      return {
        headers: {
          'Authorization': 'Bearer ' + getToken()
        },
        dialogg: false,
        dialogImageUrl: '',
        dialogVisible: false,
        fileList: [],
        pictures: []
      }
    },
    computed: {
      ...mapGetters([
        'uploadFacePicApi'
      ])
    },
    methods: {
      clearimg(){
        this.$refs.upload.clearFiles(); 
      },
      handleSuccess(response, file, fileList) {
        const uid = file.uid
        const id = response.id
        this.pictures = [{ uid, id }]
        this.fileList = [file]
        this.$emit('updatePhoto', response.data[0], response.data[1], response.embedding)
      },
      // 上传前 删除所有图片
      handlebeforeUpload(file, fileList) {
        for (let i = 0; i < this.pictures.length; i++) {
          del(this.pictures[i].id).then(res => {
          })
        }
      },
      handleBeforeRemove(file, fileList) {
        for (let i = 0; i < this.pictures.length; i++) {
          if (this.pictures[i].uid === file.uid) {
            del(this.pictures[i].id).then(res => {

            })
            return true
          }
        }
      },
      handlePictureCardPreview(file) {
        this.dialogImageUrl = file.url
        this.dialogVisible = true
      },
      // 刷新列表数据
      doSubmit() {
        this.fileList = []
        this.dialogVisible = false
        this.dialogImageUrl = ''
        this.dialog = false
        this.$parent.$parent.init()
      },
      // 监听上传失败
      handleError(e, file, fileList) {
        const msg = JSON.parse(e.message)
        this.$notify({
          title: msg.message,
          type: 'error',
          duration: 2500
        })
      }
    }
  }
</script>

<style scoped>

</style>
