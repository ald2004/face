<template>
  <el-dialog :append-to-body="true" :visible.sync="dialog" :title="isAdd ? '新增摄像头' : '编辑摄像头'" width="500px">
    <el-form ref="form" :model="form" :rules="rules" size="small" label-width="66px">
      <el-form-item label="编号" prop="number">
        <el-input v-model="form.number" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="区域" prop="region">
        <el-input v-model="form.region" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="IP" prop="ip">
        <el-input v-model="form.ip" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="状态" prop="state">
        <el-input v-model="form.state" style="width: 370px;"/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="text" @click="cancel">取消</el-button>
      <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { add, edit } from '@/api/camera'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
export default {
  name: 'Form',
  components: { Treeselect },
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
      dialog: false, loading: false, form: { number: '', region: '', IP: '', state:'' },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    cancel() {
      this.resetForm()
    },
    doSubmit() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.loading = true;
          this.form.roles = [];
          const _this = this;
          // this.roleIds.forEach(function(data, index) {
          //   const role = { id: data }
          //   _this.form.roles.push(role)
          // })
          if (this.isAdd) {
            this.doAdd()
          } else this.doEdit()
        } else {
          return false
        }
      })
    },
    doAdd() {
      add(this.form).then(res => {
        this.resetForm();
        this.$notify({
          title: '添加成功',
          message: '默认密码：123456',
          type: 'success',
          duration: 2500
        });
        this.loading = false;
        this.$parent.$parent.init()
      }).catch(err => {
        this.loading = false;
        console.log(err.response.data.message)
      })
    },
    doEdit() {
      edit(this.form).then(res => {
        this.resetForm();
        this.$notify({
          title: '修改成功',
          type: 'success',
          duration: 2500
        });
        this.loading = false;
        this.sup_this.init()
      }).catch(err => {
        this.loading = false;
        console.log(err.response.data.message)
      })
    },
    resetForm() {
      this.dialog = false;
      this.$refs['form'].resetFields();
      this.roleIds = [];
      this.form = {  number: '', region: '', IP: '', state:''  }
    }
  }
}
</script>

<style scoped>

</style>
