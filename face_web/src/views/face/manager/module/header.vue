<template>
  <div class="head-container">
    <!-- 搜索 -->
    <el-input
      v-model="query.value"
      clearable
      placeholder="输入关键字搜索"
      style="width: 200px;"
      class="filter-item"
      @keyup.enter.native="toQuery"
    />
    <el-select
      v-model="query.type"
      clearable
      placeholder="身份证"
      class="filter-item"
      style="width: 130px"
    >
      <el-option
        v-for="item in queryTypeOptions"
        :key="item.key"
        :label="item.display_name"
        :value="item.key"
      />
    </el-select>
    <el-select
      v-model="query.status"
      clearable
      placeholder="状态"
      class="filter-item"
      style="width: 90px"
      @change="toQuery"
    >
      <el-option
        v-for="item in enabledTypeOptions"
        :key="item.key"
        :label="item.display_name"
        :value="item.key"
      />
    </el-select>
    <el-button
      class="filter-item"
      size="mini"
      type="primary"
      icon="el-icon-search"
      @click="toQuery"
    >搜索</el-button>
    <!-- 新增 -->
    <div style="display: inline-block;margin: 0px 2px;">
      <el-button
        v-if="checkPermission(['ADMIN','FACE_USER_ALL','FACE_USER_CREATE'])"
        class="filter-item"
        size="mini"
        type="primary"
        icon="el-icon-plus"
        @click="$refs.form.dialog = true"
      >新增</el-button>
      <eForm ref="form" :roles="roles" :is-add="true"/>
    </div>
    <div style="display: inline-block;margin: 0px 2px;">
      <!-- 导出 -->
      <el-button
        v-if="checkPermission(['ADMIN'])"
        :loading="downloadLoading"
        size="mini"
        class="filter-item"
        type="primary"
        icon="el-icon-download"
        @click="download"
      >导出</el-button>
    </div>
    <div style="display: inline-block;margin: 0px 2px;">
      <el-popover placement="bottom" width="200" trigger="manual" v-model="piliang">
        <p>确定删除选中数据吗？</p>
        <div style="text-align: right; margin: 0">
          <el-button size="mini" type="text" @click="piliang = false">取消</el-button>
          <el-button :loading="delLoading" type="primary" @click="dell()" size="mini">确定</el-button>
        </div>
        <el-button
          slot="reference"
          class="filter-item"
          type="danger"
          size="mini"
          @click="panduan()"
        >批量删除</el-button>
      </el-popover>
    </div>

    <div style="display: inline-block;margin: 0px 2px;">
      <upload/>
    </div>
  </div>
</template>

<script>
import checkPermission from "@/utils/permission"; // 权限判断函数
import { parseTime } from "@/utils/index";
import { del } from "@/api/face-user";
import eForm from "./form";
import upload from "./uploadUsers";

// 查询条件
export default {
  components: { eForm, upload },
  props: {
    roles: {
      type: Array,
      required: true
    },
    query: {
      type: Object,
      required: true
    },
    delh: {
      type: String,
      default() {
        return false;
      }
    }
  },
  data() {
    return {
      downloadLoading: false,
      delLoading: false,
      uploadLoading: false,
      piliang: false,
      switchStatusData: this.delh,
      queryTypeOptions: [
        { key: "name", display_name: "姓名" },
        { key: "idCard", display_name: "身份证" },
        { key: "phone", display_name: "手机号" }
      ],
      enabledTypeOptions: [
        { key: "0", display_name: "禁用" },
        { key: "1", display_name: "黑名单" },
        { key: "2", display_name: "白名单" }
      ]
    };
  },
  computed: {
    switchStatus: function() {
      // console.log(this.switchStatusData);
      return this.delh; // 直接监听props里的status状态
    }
  },
  watch: {
    delh(newV, oldV) {
      // watch监听props里status的变化，然后执行操作
      console.log(newV, oldV);
      this.switchStatusData = newV;
    }
  },
  methods: {
    checkPermission,
    reload() {
      this.toQuery();
    },
    // 去查询
    toQuery() {
      this.$parent.page = 0;
      this.$parent.init();
    },
    //导入
    importUsers() {
      this.uploadLoading = true;
      this.uploadLoading = false;
    },
    // 导出
    download() {
      this.downloadLoading = true;
      import("@/vendor/Export2Excel").then(excel => {
        const tHeader = [
          "id",
          "姓名",
          "身份证",
          "手机号",
          "原始图像地址",
          "状态",
          "注册日期",
          "人脸图像地址"
        ];
        const filterVal = [
          "id",
          "name",
          "idCard",
          "phone",
          "photo",
          "status",
          "createTime",
          "facePhoto"
        ];
        const data = this.formatJson(filterVal, this.$parent.data);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: "table-list"
        });
        this.downloadLoading = false;
      });
    },
    // 数据转换
    formatJson(filterVal, jsonData) {
      return jsonData.map(v =>
        filterVal.map(j => {
          if (j === "createTime") {
            return parseTime(v[j]);
          } else if (j === "status") {
            return parseTime(v[j]) === 0
              ? "禁用"
              : parseTime(v[j]) === 1
              ? "黑名单"
              : "白名单";
          } else {
            return v[j];
          }
        })
      );
    },
    dell() {
      this.delLoading = true;
      del(this.switchStatusData)
        .then(res => {
          this.delLoading = false;
          this.piliang = false;
          this.$parent.init();
          this.$notify({
            title: "删除成功",
            type: "success",
            duration: 2500
          });
        })
        .catch(err => {
          this.delLoading = false;
          this.piliang = false;
          console.log(err.response.data.message);
        });
    },
    panduan() {
      if (this.switchStatusData == "") {
        this.$notify.error({
          title: "您还没有选中要删除的行",
          type: "success",
          duration: 1500
        });
        this.piliang = false;
        console.log(this.switchStatusData, "kong");
      } else {
        this.piliang = true;
        console.log(this.switchStatusData, "buk");
      }
    }
  }
};
</script>
