<style>
.aaa {
  overflow: visible;
}
</style>

<template>
  <div class="app-container">
    <eHeader :roles="roles" :query="query"/>
    <!--表格渲染-->
    <el-table
      v-loading="loading"
      element-loading-text="拼命加载中"
      :data="data"
      size="small"
      border
      style="width: 100%;"
    >
      <el-table-column prop="number" label="编号"/>
      <el-table-column prop="region" label="区域"/>
      <el-table-column prop="username" label="摄像头账号"/>
      <el-table-column prop="password" label="摄像头密码"/>
      <el-table-column prop="ip" label="摄像头IP"/>
      <el-table-column prop="port" label="RTSP端口"/>
      <el-table-column label="状态">
        <template slot-scope="scope">
          <span v-if="scope.row.status === 0 " class="badge" style="font-weight: bold">禁用</span>
          <span v-else class="badge badge-bg-green" style="font-weight: bold">启用</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340px" align="center">
        <template slot-scope="scope">
          <Log
            v-if="checkPermission(['FACE_LOG_ALL','FACE_LOG_SELECT','ADMIN'])"
            :row="scope.row"
            :ref="'lognum'+scope.row.id"
          />

          <el-button v-else disabled size="mini" type="primary">查看访问记录</el-button>

          <edit
            v-if="checkPermission(['CAMERA_ALL','CAMERA_EDIT','ADMIN'])"
            :data="scope.row"
            :roles="roles"
            :sup_this="sup_this"
          />
          <el-button v-else disabled size="mini" style="margin-left:15px" type="success">编辑</el-button>

          <el-popover
            v-if="checkPermission(['CAMERA_ALL','CAMERA_DELETE','ADMIN'])"
            v-model="scope.row.delPopover"
            placement="top"
            width="180"
          >
            <p>确定删除本条数据吗？</p>
            <div style="text-align: right; margin: 0">
              <el-button size="mini" type="text" @click="scope.row.delPopover = false">取消</el-button>
              <el-button
                :loading="delLoading"
                type="primary"
                size="mini"
                @click="subDelete(scope.$index, scope.row)"
              >确定</el-button>
            </div>
            <el-button
              slot="reference"
              type="danger"
              size="mini"
              @click="scope.row.delPopover = true"
            >删除</el-button>
          </el-popover>
          <el-button
            v-else
            disabled
            slot="reference"
            type="danger"
            size="mini"
            @click="scope.row.delPopover = true"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <el-pagination
      :total="total"
      style="margin-top: 8px;"
      layout="total, prev, pager, next, sizes"
      @size-change="sizeChange"
      @current-change="pageChange"
    />
  </div>
</template>

<script>
import checkPermission from "@/utils/permission";
import initData from "../../../mixins/initData";
import { del } from "@/api/camera";
import { getnum } from "@/api/face-log";
import { getRoleTree } from "@/api/role";
import { parseTime } from "@/utils/index";
import eHeader from "./module/header";
import edit from "./module/edit";
import Log from "./module/log";
import { get } from "http";
export default {
  components: { eHeader, edit, Log },
  mixins: [initData],
  data() {
    return {
      roles: [],
      delLoading: false,
      sup_this: this
    };
  },
  created() {
    this.getRoles();
    this.$nextTick(() => {
      this.init();
    });

  },
  methods: {
    parseTime,
    checkPermission,
    beforeInit() {
      this.url = "api/camera";
      const sort = "id,desc";
      const query = this.query;
      const type = query.type;
      const value = query.value;
      const status = query.status;
      this.params = { page: this.page, size: this.size, sort: sort };
      if (type && value) {
        this.params[type] = value;
      }
      if (status !== "" && status !== null) {
        this.params["status"] = status;
      }
      
      return true;
    },
    subDelete(index, row) {
      this.delLoading = true;
      del(row.id)
        .then(res => {
          this.delLoading = false;
          row.delPopover = false;
          this.init();
          this.$notify({
            title: "删除成功",
            type: "success",
            duration: 2500
          });
        })
        .catch(err => {
          this.delLoading = false;
          row.delPopover = false;
          console.log(err.response.data.message);
        });
    },
    getRoles() {
      getRoleTree().then(res => {
        this.roles = res;
      });
    }
  }
};
</script>

<style scoped>
</style>
