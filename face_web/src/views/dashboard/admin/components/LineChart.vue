<template>
  <div :class="className" :style="{height:height,width:width}"></div>
</template>

<script>
import echarts from "echarts";
require("echarts/theme/macarons"); // echarts theme
import { debounce } from "@/utils";
import { getChartData } from "@/api/visits";
import { get } from "@/api/face-log";
export default {
  props: {
    className: {
      type: String,
      default: "chart"
    },
    width: {
      type: String,
      default: "100%"
    },
    height: {
      type: String,
      default: "350px"
    },
    autoResize: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      chart: null,
      sidebarElm: null,
      centerchart: [],
      chartData: {
        visitsData: [],
        ipData: [],
        count: [],
        hmd: [],
        bmd: [],
        jy: [],
        msr: []
      },
      weekDays: []
    };
  },
  mounted() {
    get().then(res => {
      let temp = {};

      let names = [];

      console.log(res);
      for (let i = 0; i < res.length; i++) {
        let obj = res[i];
        let cameraId =
          obj["cameraId"] + "," + obj["region"] + " (" + obj["ip"] + ")";
        let faceUserStatus = obj["faceUserStatus"];

        temp[cameraId] = temp[cameraId] || {
          hmd: 0,
          bmd: 0,
          jy: 0,
          msr: 0
        };
        switch (faceUserStatus) {
          case 0:
            temp[cameraId].jy = obj["count"];
            break;
          case 1:
            temp[cameraId].hmd = obj["count"];
            break;
          case 2:
            temp[cameraId].bmd = obj["count"];
            break;
          case 3:
            temp[cameraId].msr = obj["count"];
            break;
        }
      }
      console.log(temp);

      for (var name in temp) {
        names.push(name.replace(/^\d+,/, ""));
      }

     
      this.chartData.visitsData = names;

      for (var attr in temp) { 
        let obj = temp[attr];
        this.chartData.hmd.push(obj.hmd);
        this.chartData.jy.push(obj.jy);
        this.chartData.bmd.push(obj.bmd);
        this.chartData.msr.push(obj.msr);
        this.chartData.count.push(
          obj.msr + obj.bmd + obj.jy + obj.hmd
        );
      }

      // let c = [];
      // let d = {};
      // res.forEach(element => {
      //   if (!d[element.ip]) {
      //     c.push({
      //       ip: element.ip,
      //       allData: [element]
      //     });
      //     d[element.ip] = element;
      //   } else {
      //     c.forEach(ele => {
      //       if (ele.ip == element.ip) {
      //         ele.allData.push(element);
      //       }
      //     });
      //   }
      // });
      // console.log(c);
      // for (let i = 0; i < c.length; i++) {
      //   this.chartData.visitsData.push(c[i].ip);
      //   for (let j = 0; j < c[i].allDate.length; j++) {
      //     this.chartData.count.push(c[i][j].count);
      //   }
      // }

      this.initChart();
    });
    if (this.autoResize) {
      this.__resizeHandler = debounce(() => {
        if (this.chart) {
          this.chart.resize();
        }
      }, 100);
      window.addEventListener("resize", this.__resizeHandler);
    }

    // 监听侧边栏的变化
    this.sidebarElm = document.getElementsByClassName("sidebar-container")[0];
    this.sidebarElm &&
      this.sidebarElm.addEventListener(
        "transitionend",
        this.sidebarResizeHandler
      );
  },
  beforeDestroy() {
    if (!this.chart) {
      return;
    }
    if (this.autoResize) {
      window.removeEventListener("resize", this.__resizeHandler);
    }

    this.sidebarElm &&
      this.sidebarElm.removeEventListener(
        "transitionend",
        this.sidebarResizeHandler
      );

    this.chart.dispose();
    this.chart = null;
  },
  methods: {
    sidebarResizeHandler(e) {
      if (e.propertyName === "width") {
        this.__resizeHandler();
      }
    },
    setOptions({ visitsData, hmd, bmd, jy, msr, count } = {}) {
      this.chart.setOption({
        tooltip: {
          trigger: "axis"
        },
        legend: {
          data: ["总数", "禁用", "黑名单", "白名单", "陌生人"]
        },
        toolbox: {
          show: false
        },
        calculable: false,
        xAxis: [
          {
            type: "category",
            data: visitsData
          }
        ],
        yAxis: [
          {
            type: "value"
          }
        ],
        series: [
          {
            name: "总数",
            type: "bar",
            data: count
          },
          {
            name: "禁用",
            type: "bar",
            data: jy
          },
          {
            name: "黑名单",
            type: "bar",
            data: hmd
          },
          {
            name: "白名单",
            type: "bar",
            data: bmd
          },
          {
            name: "陌生人",
            type: "bar",
            data: msr
          }
        ]
      });
    },
    initChart() {
      this.chart = echarts.init(this.$el, "macarons");
      this.setOptions(this.chartData);
    }
  }
};
</script>
