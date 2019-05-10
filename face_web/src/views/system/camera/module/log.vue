<template>
  <span ref="abc">
    <el-badge :value="number" :max="99" class="item">
      <el-button size="mini" type="primary" @click="doLog()">查看访问记录</el-button>
    </el-badge>

    <el-dialog
      :append-to-body="true"
      :visible.sync="dialog"
      style="margin-left: 50px;"
      title="访问记录"
      width="1240px"
    >
      <!-- 搜索 -->
      <div class="head-container">
        <el-input
          v-model="query.value"
          clearable
          placeholder="输入人员姓名、身份证、手机号搜索"
          style="width: 200px;"
          class="filter-item"
          @keyup.enter.native="toQuery"
        />
        <el-select
          v-model="query.type"
          clearable
          placeholder="搜索类型"
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
          placeholder="身份类型"
          clearable
          class="filter-item"
          style="width: 110px"
          @change="toQuery"
        >
          <el-option
            v-for="item in enabledTypeOptions"
            :key="item.key"
            :label="item.display_name"
            :value="item.key"
          />
        </el-select>
        <el-date-picker
          v-model="query.startTime"
          type="datetime"
          format="yyyy-MM-dd HH:mm:ss"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="height:30.5px"
          class="filter-item"
          placeholder="开始日期"
        ></el-date-picker>
        <el-date-picker
          v-model="query.endTime"
          type="datetime"
          format="yyyy-MM-dd HH:mm:ss"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="height:30.5px"
          class="filter-item"
          placeholder="结束日期"
        ></el-date-picker>
        <el-button
          class="filter-item"
          size="mini"
          type="primary"
          icon="el-icon-search"
          @click="toQuery"
        >搜索</el-button>
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
      <!--表格渲染-->
      <el-table
        v-loading="loading"
        :data="data"
        size="small"
        border
        @selection-change="handleSelectionChange"
        style="width: 100%;margin-top: -10px;"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column :show-overflow-tooltip="true" prop="name" label="姓名"/>
        <el-table-column label="人脸图像" width="80px">
          <template slot-scope="scope">
            <img
              v-if="scope.row.status === 3"
              :src="(scope.row.photo.startsWith('http')?scope.row.logImg:baseUrl+'/api/no-user/faceLog/subimages/'+scope.row.cameraId+'/'+scope.row.logImg)+'?x='+scope.row.x+'&y='+scope.row.y+'&w='+scope.row.width +'&h='+scope.row.height"
              class="el-avatar"
              alt="人脸图像"
            >

            <img
              v-else
              :src="scope.row.facePhoto.startsWith('http')?scope.row.facePhoto:baseUrl+'/'+scope.row.facePhoto"
              class="el-avatar"
              alt="人脸图像"
            >
          </template>
        </el-table-column>
        <el-table-column :show-overflow-tooltip="true" prop="idCard" width="180px" label="身份证"/>
        <el-table-column :show-overflow-tooltip="true" prop="phone" label="手机号"/>
        <el-table-column :show-overflow-tooltip="true" prop="ip" width="120px" label="IP"/>

        <el-table-column width="100px" label="图像">
          <template slot-scope="scope">
            <imgShow
              :src="scope.row.photo.startsWith('http')?scope.row.logImg:baseUrl+'/api/no-user/faceLog/images/'+scope.row.cameraId+'/'+scope.row.logImg"
              :row="scope.row"
            />
          </template>
        </el-table-column>
        <el-table-column align="center" prop="status" width="80px" label="状态">
          <template slot-scope="scope">
            <el-tag type="warning" v-if="scope.row.status === 0">禁用</el-tag>
            <span class="badge" v-if="scope.row.status === 1">黑名单</span>
            <el-tag type="success" v-if="scope.row.status === 2">白名单</el-tag>
            <span class="badge badge-bg-orange" v-if="scope.row.status === 3">陌生人</span>
          </template>
        </el-table-column>
        <el-table-column
          :show-overflow-tooltip="true"
          prop="createTime"
          width="180px"
          style="overflow: hidden;"
          label="创建日期"
        >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :show-overflow-tooltip="true" prop="createTime" label="过去时间">
          <template slot-scope="scope">
            <el-tag
              v-if="timestampFormat(scope.row.createTime).status===0"
            >{{timestampFormat(scope.row.createTime).text}}</el-tag>
            <el-tag
              v-if="timestampFormat(scope.row.createTime).status===1"
              type="success"
            >{{timestampFormat(scope.row.createTime).text}}</el-tag>
            <el-tag
              v-if="timestampFormat(scope.row.createTime).status===2"
              type="info"
            >{{timestampFormat(scope.row.createTime).text}}</el-tag>
            <el-tag
              v-if="timestampFormat(scope.row.createTime).status===3"
              type="warning"
            >{{timestampFormat(scope.row.createTime).text}}</el-tag>
            <el-tag
              v-if="timestampFormat(scope.row.createTime).status===4"
              type="danger"
            >{{timestampFormat(scope.row.createTime).text}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template slot-scope="scope">
            <el-popover
              v-if="checkPermission(['ADMIN','FACE_USER_ALL','FACE_USER_DELETE'])"
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
                  @click="subDelete(scope.$index, scope.row)"
                  size="mini"
                >确定</el-button>
              </div>
              <el-button
                slot="reference"
                type="danger"
                size="mini"
                @click="scope.row.delPopover = true"
              >删除</el-button>
            </el-popover>
            <el-button v-else disabled slot="reference" type="danger" size="mini">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!--分页组件-->
      <el-pagination
        :total="total"
        style="margin-top:8px;"
        layout="total, prev, pager, next"
        @size-change="sizeChange"
        @current-change="pageChange"
      />
    </el-dialog>
  </span>
</template>

<script>
import checkPermission from "@/utils/permission";
import initData from "../../../../mixins/initData";
import { getRoleTree } from "@/api/role";
import { parseTime } from "@/utils/index";
import imgShow from "./img";
import edit from "./edit";
import { del } from "@/api/face-log";
import { getnum } from "@/api/face-log";

export default {
  components: { imgShow, edit },
  mixins: [initData],
  props: {
    row: {
      type: Object,
      required: false
    }
  },
  created() {
    this.$nextTick(() => {
      this.init();
      this.lognumm();
      // console.log(this.row);
    });
  },
  // 0 禁用 1 黑名单 2 白名单 3 陌生人
  data() {
    return {
      piliang: false,
      deletes: "", //批量删除，存储id的字符串
      roles: [],
      number: 0,
      value1: "",
      buttonvalue: 0,
      dialog: false,
      delLoading: false,
      sup_this: this,
      enabledTypeOptions: [
        { key: "0", display_name: "禁用" },
        { key: "1", display_name: "黑名单" },
        { key: "2", display_name: "白名单" },
        { key: "3", display_name: "陌生人" }
      ],
      queryTypeOptions: [
        { key: "name", display_name: "姓名" },
        { key: "idCard", display_name: "身份证" },
        { key: "phone", display_name: "手机号" }
      ]
    };
  },
  methods: {
    parseTime,
    checkPermission,
    lognumm() {
      console.log(this.row);
      getnum(this.row.id).then(res => {
        this.number = res;
      });
    },
    doInit() {
      this.$nextTick(() => {
        this.init();
      });
    },
    toQuery() {
      this.page = 0;
      this.doInit();
      // if(query.startTime!==""||query.endTime===""){
      //   this.$notify.error({
      //     title: "您还没有选择结束时间",
      //     type: "success",
      //     duration: 1500
      //   });
      // }
      // if(query.endTime!==""||query.startTime===""){
      //   this.$notify.error({
      //     title: "您还没有选择开始时间",
      //     type: "success",
      //     duration: 1500
      //   });
      // }
    },
    beforeInit() {
      this.url = "api/faceLog/logs";
      this.baseUrl = process.env.BASE_API;
      const sort = "id,desc";
      const query = this.query;
      const value = query.value;
      const type = query.type ? query.type : "name";
      const status = query.status;
      const startTime = query.startTime ? query.startTime : "";
      const endTime = query.endTime ? query.endTime : "";
      this.params = { page: this.page, size: this.size, sort: sort };
      if (value) {
        this.params[type] = value;
      }
      if (status !== "" && status !== null) {
        this.params["status"] = status;
      }
      if (this.row && this.row.id) {
        this.params["cameraId"] = this.row.id;
      }
      if (startTime !== "" && startTime !== null) {
        this.params["startTime"] = startTime;
      }
      if (endTime !== "" && endTime !== null) {
        this.params["endTime"] = endTime;
      }
      this.$refs.abc.parentElement.style.overflow = "visible";
      // console.log(this.$refs.abc.parentElement.style.overflow);
      

      return true;
    },
    getRoles() {
      getRoleTree().then(res => {
        this.roles = res;
      });
    },
    doLog() {
      this.dialog = true;
      this.doInit();
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
    dell() {
      this.delLoading = true;
      del(this.deletes)
        .then(res => {
          this.delLoading = false;
          this.piliang = false;
          this.init();
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
      if (this.deletes == "") {
        this.$notify.error({
          title: "您还没有选中要删除的行",
          type: "success",
          duration: 1500
        });
        this.piliang = false;
        console.log(this.deletes, "kong");
      } else {
        this.piliang = true;
        console.log(this.deletes, "buk");
      }
    },
    handleSelectionChange(val) {
      console.log(val);
      this.deletes = "";
      for (let i = 0; i < val.length; i++) {
        this.deletes += val[i].id + ",";
      }
      this.deletes = this.deletes.substring(0, this.deletes.length - 1);
    },
    timestampFormat(timestamp) {
      //日期格式美化
      function zeroize(num) {
        return (String(num).length == 1 ? "0" : "") + num;
      }

      let curTimestamp = parseInt(new Date().getTime() / 1000); //当前时间戳
      let timestampDiff = curTimestamp - timestamp / 1000; // 参数时间戳与当前时间戳相差秒数

      let curDate = new Date(curTimestamp * 1000); // 当前时间日期对象
      let tmDate = new Date(timestamp); // 参数时间戳转换成的日期对象
      let Y = tmDate.getFullYear(),
        m = tmDate.getMonth() + 1,
        d = tmDate.getDate();
      let H = tmDate.getHours(),
        i = tmDate.getMinutes(),
        s = tmDate.getSeconds();

      if (timestampDiff < 60) {
        // 一分钟以内
        return { text: timestampDiff+"秒前", status: 0 };
      } else if (timestampDiff < 3600) {
        // 一小时前之内
        return { text: Math.floor(timestampDiff / 60) + "分钟前", status: 1 };
      } else if (
        curDate.getFullYear() == Y &&
        curDate.getMonth() + 1 == m &&
        curDate.getDate() == d
      ) {
        return { text: (zeroize(curDate.getHours())- zeroize(H) )+"小时前", status: 2 };
      } else {
        let newDate = new Date((curTimestamp - 86400) * 1000); // 参数中的时间戳加一天转换成的日期对象
        if (
          newDate.getFullYear() == Y &&
          newDate.getMonth() + 1 == m &&
          newDate.getDate() == d
        ) {
          return { text: "昨天", status: 3 };
        } else if (curDate.getMonth()+1 == m) {
          return {
            text:
              (zeroize(curDate.getDate())-zeroize(d))+"天前",
            status: 4
          };
        } else {
          return {
            text:
              (curDate.getMonth()+1 -zeroize(m))+"月前",
            status: 4
          };
        }
      }
    }
  }
};
</script>
