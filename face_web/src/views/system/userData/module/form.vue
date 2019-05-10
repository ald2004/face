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
      <el-form-item label="身份证号码" style="overflow:hidden;position: relative;" prop="idCard">
        <el-input v-model="form.idCard" style="width: 370px;"/>
      </el-form-item>
      <el-form-item label="图片" prop="image" style>
        <div class="imgbox" @click="tudofile">
          <i class="el-icon-plus"></i>
          <img
            v-if="form.image!=''"
            :src="form.image"
            ref="faceimg"
            alt
            style="position:absolute;top:0;left:0;width:100px;height:100px;float:left;border-radius: 6px;"
          >
        </div>
        <!-- <el-button  @click="tudofile" ref='imgbutton'  type="primary" >选择图片</el-button>
        <img src="" ref='faceimg' alt="">-->
        <input @change="uploadPhoto($event)" type="file" ref="uplodefile" style="display:none">

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
          { min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur" }
        ],
        idCard:[
          // { required: true, message: "请输入身份证号码", trigger: "blur" },
          { min: 18, max: 18, message: "长度为18个字符", trigger: "change" }
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
      var self = this;
      reader.onload = function(e) {
        // 读取到的图片base64 数据编码 将此编码字符串传给后台即可
        var imgcode = e.target.result;
        self.imageUrl = imgcode;
        self.form.image = self.imageUrl;
        // self.$refs.faceimg.src=self.imageUrl;
        // console.log(self.imageUrl);
      };
    },
    tudofile() {
      this.$refs.uplodefile.click();
    },
    doSubmit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.loading = true;
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
      add(this.form)
        .then(res => {
          this.resetForm();
          this.$notify({
            title: "添加成功",
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
            title: "修改成功",
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
      this.$refs.faceimg.src = "";
      this.form = {
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
.imgbox {
  background-color: #fbfdff;
  border: 1px dashed #c0ccda;
  border-radius: 6px;
  box-sizing: border-box;
  width: 100px;
  height: 100px;
  cursor: pointer;
  line-height: 100px;
  text-align: center;
  vertical-align: top;
  position: relative;
}
.imgbox i {
  font-size: 28px;
  color: #8c939d;
}
.el-form-item__error{
  position: unset;
}
</style>
