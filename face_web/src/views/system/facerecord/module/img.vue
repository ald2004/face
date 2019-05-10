<template>
  <div>
    <el-button size="mini" type="primary" @click="doImg()">查看原图</el-button>
    <el-dialog :append-to-body="dialog" :visible.sync="dialog" style="margin-left: 50px;" title="图片" width="1064px">
      <img :src="src+'?x='+row.x+'&y='+row.y+'&w='+row.width +'&h='+row.height" style="max-width: 1024px" alt="原图"/>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    props: {
      src: {
        type: String,
        required: true
      },
      row: {
        type: Object,
        required: true
      }
    },
    data() {
      return {
        dialog: false, delLoading: false, sup_this: this
      }
    },
    methods: {
      doImg() {
        this.dialog = true
      }
    }
  }
</script>

<style scoped>

</style>
