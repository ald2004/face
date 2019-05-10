<template>
  <el-dialog :append-to-body="true" :visible.sync="dialog" @close="cancel" :title="isAdd ? '新增人脸库' : '编辑人脸库'" width="520px">
    <el-form ref="form" :model="form" :rules="rules" size="small" label-width="80px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="身份证" prop="idCard">
        <el-input v-model="form.idCard" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio v-model="form.status" label="0">禁用</el-radio>
        <el-radio v-model="form.status" label="1">黑名单</el-radio>
        <el-radio v-model="form.status" label="2">白名单</el-radio>
      </el-form-item>

      <el-form-item required="required" label="正面照片">
        <uploadPic ref="imgload" @updatePhoto="updatePhoto.apply(this,arguments)"/>
      </el-form-item>

      <el-form-item label="原始图像" prop="photo">
        <el-input v-model="form.photo" ref="photo" readonly="readonly" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="人脸图像" prop="facePhoto">
        <el-input v-model="form.facePhoto" ref="facePhoto" readonly="readonly" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="人脸特征" prop="embedding">
        <el-input v-model="form.embedding" ref="embedding" readonly="readonly" style="width: 370px;"/>
      </el-form-item>

      <el-form-item label="附加描述" prop="des">
        <el-input type="textarea" v-model="form.des" style="width: 370px;"/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="text" @click="cancel">取消</el-button>
      <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
    </div>
  </el-dialog>
</template>

<script>
  import { add, edit } from '@/api/face-user'
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import uploadPic from './upload'

  export default {
    name: 'Form',
    components: { Treeselect, uploadPic },
    props: {
      roles: {
        type: Array,
        required: true
      },
      isAdd: {
        type: Boolean,
        required: true
      },
      sup_this: {
        type: Object,
        default: null
      }
    },
    data() {
      return {
        dialog: false,
        loading: false,
        form: { name: '', idCard: '', status: '0', phone: '', photo: '', facePhoto: '', des: '', embedding: '' },
        rules: {
          name: [
            { required: true, message: '请输入姓名', trigger: 'blur' },
            { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
          ],
          idCard: [
            { required: true, message: '身份证号', trigger: 'blur' },
            { min: 15, max: 18, message: '请输入正确的身份证号', trigger: 'blur' }
            // { pattern: /^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$|^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9]|X)$/, message: '请输入合法的身份证号码' }
          ],
          phone: [
            { required: false, message: '手机号', trigger: 'blur' },
            { min: 8, max: 15, message: '请输入正确的手机号', trigger: 'blur' }
            // { pattern: /^1[34578]\d{9}$/, message: '目前只支持中国大陆的手机号码' }
          ],
          status: [
            { required: true, message: '状态不能为空', trigger: 'blur' }
          ],
          photo: [
            { required: true, message: '原始图像不能为空', trigger: 'blur' }
          ],
          facePhoto: [
            { required: true, message: '人脸图像不能为空', trigger: 'blur' }
          ],
          embedding: [
            { required: true, message: '人脸特征不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      cancel() {
        this.resetForm();
        this.$refs.imgload.clearimg();
      },
      doSubmit() {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            this.loading = true
            const _this = this
            if (this.isAdd) {
              this.doAdd()
            } else {
              this.doEdit()
            }
          } else {
            return false
          }
        })
      },
      doAdd() {
        add(this.form).then(res => {
          this.resetForm()
          this.$notify({
            title: '添加成功',
            // message: '添加成功',
            type: 'success',
            duration: 2500
          })
          this.loading = false
          this.$parent.$parent.init()
        }).catch(err => {
          this.loading = false
          console.log(err.response.data.message)
        })
      },
      doEdit() {
        edit(this.form).then(res => {
          this.resetForm()
          this.$notify({
            title: '修改成功',
            type: 'success',
            duration: 2500
          })
          this.loading = false
          this.sup_this.init()
        }).catch(err => {
          this.loading = false
          console.log(err.response.data.message)
        })
      },
      resetForm() {
        this.dialog = false
        this.$refs['form'].resetFields()
        this.form = { name: '', idCard: '', status: '0', phone: '', photo: '', facePhoto: '', des: '', embedding: '' }
      },
      updatePhoto(photo, facePhoto, embedding) {
        this.form.photo = photo
        this.form.facePhoto = facePhoto
        this.form.embedding = embedding
      }
    }
  }
</script>

<style scoped>

</style>
