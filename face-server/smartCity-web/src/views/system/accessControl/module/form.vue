<template>
  <el-dialog
    :append-to-body="true"
    :visible.sync="dialog"
    :title="isAdd ? '新增用户' : '编辑用户'"
    width="540px"
  >
    <efForm ref="fform"/>
    <el-form ref="form" :model="form" :rules="rules" size="small" label-width="82px">
      <el-form-item label="姓名" prop="username">
        <el-input v-model="form.username" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <!-- <el-input v-model="form.sex" style="width: 370px;"/> -->
        <el-radio v-model="form.sex" label="男" value="男"></el-radio>
        <el-radio v-model="form.sex" label="女" value="女"></el-radio>
      </el-form-item>
      <el-form-item label="身份证号码" style="overflow:hidden" prop="idCard">
        <el-input v-model="form.idCard" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="图片" prop="image">
        <el-button type="primary" @click="tudofile">选择图片</el-button>
        <input @change="uploadPhoto($event)" ref="uplodefile" type="file" style="display:none">
        <!-- <el-upload
          class="avatar-uploader"
          :action="imagesUploadApi"
          :show-file-list="false"
          :on-success="beforeAvatarUpload"
        >
        <template slot-scope="scope">
        </template>
        <img v-if="imageUrl" :src="form.image" class="avatar">
        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>-->
        <!-- <el-input v-model="imageUrl" style="width: 370px;display:none"/> -->
      </el-form-item>
      <el-form-item label="住址" prop="address">
        <el-input v-model="form.address" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="人员类型" prop="type">
        <el-input v-model="form.type" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="年龄段" prop="oldTime">
        <el-input v-model="form.oldTime" style="width: 370px;"/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="text" @click="cancel">取消</el-button>
      <el-button :loading="loading" type="primary" @click="doSubmit">确认</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { mapGetters } from "vuex";
import checkPermission from "@/utils/permission";
import { add, edit } from "@/api/userData";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
// import eForm from "./form"
import efForm from "../../../tools/picture/module/form.vue";
let Base64 = require("js-base64").Base64;
export default {
  name: "Form",
  components: { Treeselect, efForm },
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
      imageUrl: "",
      dialog: false,
      loading: false,
      form: {
        username: "",
        sex: "",
        idCard: "",
        address: "",
        type: "",
        oldTime: "",
        image: ""
      },
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { min: 1, max: 20, message: "长度在 1 到 20 个字符", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    ...mapGetters(["imagesUploadApi"])
  },
  methods: {
    checkPermission,
    cancel() {
      this.resetForm();
    },

    uploadPhoto(e) {
      // 利用fileReader对象获取file
      var file = e.target.files[0];
      var filesize = file.size;
      var filename = file.name;
      var reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = function(e) {
        // 读取到的图片base64 数据编码 将此编码字符串传给后台即可
        var imgcode = e.target.result;
        this.imageUrl=imgcode;
      };
    },
    tudofile() {
      this.$refs.uplodefile.click();
    },
    doSubmit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.loading = true;
          this.form.roles = [];
          const _this = this;
          // debugger;
          // this.roleIds.forEach(function(data, index) {
          //   const role = { id: data }
          //   _this.form.roles.push(role)
          // })
          if (this.isAdd) {
            this.doAdd();
          } else this.doEdit();
        } else {
          return false;
        }
      });
    },
    doAdd() {
      this.form.image=this.imageUrl;
      add(this.form)
        .then(res => {
          this.resetForm();
          this.$notify({
            title: '添加成功',
            message: '默认密码：123456',
            type: "success",
            duration: 2500
          });
          this.loading = false;
          this.$parent.$parent.init();
        })
        .catch(err => {
          this.loading = false;
          console.log(err.response.data.message);
        });
    },
    doEdit() {
      edit(this.form)
        .then(res => {
          this.resetForm();
          this.$notify({
            title: '修改成功',
            type: "success",
            duration: 2500
          });
          this.loading = false;
          this.sup_this.init();
        })
        .catch(err => {
          this.loading = false;
          console.log(err.response.data.message);
        });
    },
    resetForm() {
      this.dialog = false;
      this.$refs["form"].resetFields();
      this.roleIds = [];
      this.form ={
        username: "",
        sex: "",
        idCard: "",
        address: "",
        type: "",
        oldTime: "",
        image: ""
    };
    }
  }
};
</script>

<style>
.el-form-item__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
