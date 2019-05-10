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
      placeholder="类型"
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
    >搜索
    </el-button>
    <!-- 新增 -->
    <div style="display: inline-block;margin: 0px 2px;">
      <el-button
        v-if="checkPermission(['ADMIN','CAMERA_ALL','CAMERA_CREATE'])"
        class="filter-item"
        size="mini"
        type="primary"
        icon="el-icon-plus"
        @click="$refs.form.dialog = true"
      >新增
      </el-button>
      <eForm ref="form" :roles="roles" :is-add="true"/>
    </div>
    <!-- 导出 -->
    <el-button
      v-if="checkPermission(['ADMIN'])"
      :loading="downloadLoading"
      size="mini"
      class="filter-item"
      type="primary"
      icon="el-icon-download"
      @click="download"
    >导出
    </el-button>
  </div>
</template>

<script>
  import checkPermission from '@/utils/permission' // 权限判断函数
  import { parseTime } from '@/utils/index'
  import eForm from './form'

  // 查询条件
  export default {
    components: { eForm },
    props: {
      roles: {
        type: Array,
        required: true
      },
      query: {
        type: Object,
        required: true
      }
    },
    created(){
      let str="aaaaaaaaaaaaabbbbc"
      
      console.log(str.startsWith("aaaaaaaaaaaaab"));
    },
    data() {
      return {
        downloadLoading: false,
        queryTypeOptions: [
          { key: 'ip', display_name: 'IP' },
          { key: 'region', display_name: '区域' }
        ],
        enabledTypeOptions: [
          { key: '0', display_name: '禁用' },
          { key: '1', display_name: '启用' }
        ]
      }
    },
    methods: {
      checkPermission,
      // 去查询
      toQuery() {
        this.$parent.page = 0
        this.$parent.init();
        console.log(this.$parent.$refs.lognum);

        for(var ref in this.$parent.$refs){
            if(ref.startsWith("lognum")){
              //this.$parent.$refs[ref].init();
               this.$parent.$refs[ref].lognumm();
            }

        }

        //this.$parent.$refs.lognum.init();
        //this.$parent.$refs.lognum.lognumm();
      },
      // 导出
      download() {
        this.downloadLoading = true
        import('@/vendor/Export2Excel').then(excel => {
          const tHeader = ['编号', '区域', '摄像头IP', '摄像头用户名', '摄像头用密码', 'RTSP端口', '状态', '注册日期']
          const filterVal = ['number', 'region', 'ip', 'username', 'password', 'port', 'status', 'createTime']
          const data = this.formatJson(filterVal, this.$parent.data)
          excel.export_json_to_excel({
            header: tHeader,
            data,
            filename: 'table-list'
          })
          this.downloadLoading = false
        })
      },
      // 数据转换
      formatJson(filterVal, jsonData) {
        return jsonData.map(v =>
          filterVal.map(j => {
            if (j === 'createTime') {
              return parseTime(v[j])
            } else if (j === 'status') {
              return parseTime(v[j]) === 0 ? '禁用' : '启用'
            } else {
              return v[j]
            }
          })
        )
      }
    }
  }
</script>
