<template>
  <div>
    <el-button  size="mini" type="success" @click="to()">编辑</el-button>
    <eForm ref="form" :roles="roles" :sup_this="sup_this" :is-add="false"/>
  </div>
</template>
<script>
import eForm from './form'
export default {
  components: { eForm },
  props: {
    roles: {
      type: Array,
      required: true
    },
    data: {
      type: Object,
      required: true
    },
    // index.vue 的this 可用于刷新数据
    sup_this: {
      type: Object,
      required: true
    }
  },
  methods: {
    to() {
      console.log(this.data);
      const _this = this.$refs.form;
      _this.roleIds = [];
      _this.form = { id: this.data.id,number: this.data.number, region: this.data.region, ip: this.data.ip,state:this.data.state};
      // this.data.roles.forEach(function(data, index) {
        _this.roleIds.push(this.data.id);
      // })
      _this.dialog = true
    }
  }
}
</script>

<style scoped>
  div{display: inline-block;margin-right: 3px;}
</style>
