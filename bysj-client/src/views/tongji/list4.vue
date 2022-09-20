
<template>
    <div class="middleview">
        <div class="maps">
            <div class="tit flexcc">
                <span>西藏维吾尔自治区景点信息展示</span>
            </div>
            <div class="map" v-if="mapShow">
                <ve-map
                        :data="mapData"
                        width="100%"
                        height="650px"
                        :legend-visible="false"
                        :settings="mapSettings"
                        :extend="mapExtend"
                        :events="mapEvents"
                ></ve-map>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "middleview",
        data() {
            var img =
                "image://data:image/png;base64,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";
            this.mapSettings = {
                labelMap: {
                    siteNum: "景点数量"
                },
                // position: "province/zhejiang",
                // roam: "move", //是否开启鼠标缩放和平移漫游
                // zoom:3,
                // aspectScale:1,
                // scaleLimit:{ min: 3, max: 3 },
                mapGrid: {
                    left: "3%",
                    right: "3%",
                    top: "3%",
                    bottom: "3%"
                },
                scaleLimit: { min: 1, max: 1 }, //滚轮缩放的极限控制
                itemStyle: {
                    areaColor: "white", //rgba(44,245,195.0.1)   "#2CF5C3"
                    borderColor: "#6EB8A8",
                    borderWidth: 2,
                    shadowColor: "rgba(74,166,138,0.5)",
                    shadowBlur: 10,
                    shadowOffsetX: 10,
                    shadowOffsetY: 10,
                    emphasis: {
                        //高亮状态
                        areaColor: "#4eF7e5",
                        borderColor: "#8fdaca"
                    }
                    // normal: {}
                },

                //标签
                label: {
                    show: true,
                    color: "#5E1D0B",
                    fontSize: 12,
                    fontWeight: 600,
                    align: "center",
                    position: "insideTop",
                    distance: 55,
                    rotate: 30,
                    offset: [30, 0],
                    lineHeight: 14
                    // backgroundColor: {
                    //   image: "http://qiniu.renmating.com/map.png",
                    //   width:'80px',height:"100px"
                    // },
                    // formatter: v => {
                    //   // console.log(v)
                    //   return v.data
                    //     ? " \n   " + v.name + "   " + "\n" + v.data.value + " \n \n"
                    //     : "";
                    // }
                },
                positionJsonLink: "https://geo.datav.aliyun.com/areas_v3/bound/650000_full.json",
                // positionJsonLink:"https://geo.datav.aliyun.com/areas_v2/bound/330000_full.json",
                position: "xinjiang"
            };
            this.mapEvents = {
                click: v => {
                    // console.log(v);
                    if (this.isCity) return;
                    this.isCity = !this.isCity;
                    this.mapShow = !this.mapShow;
                    this.area.forEach(item => {
                        if (item.name === v.name) {
                            this.mapSettings.positionJsonLink = item.positionJsonLink;
                            this.mapSettings.position = item.position;
                            this.areaName = item.name;
                            // this.getData({ type: "区", areaId: item.areaId });//获取新数据
                        }
                    });
                    this.$nextTick(function() {
                        this.mapShow = !this.mapShow;
                    });
                }
            };

            return {
                areaName: "1",
                isCity: false,
                mapShow: true,
                area: [
                    {
                        positionJsonLink: "http://qiniu.renmating.com/zhejiang.json",
                        position: "zhejiang",
                        name: "1",
                        areaId: 330000
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/hangzhou.json",
                        position: "hangzhou",
                        name: "1",
                        areaId: 330100
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/huzhou.json",
                        position: "huzhou",
                        name: "1",
                        areaId: 330500
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/jiaxing.json",
                        position: "jiaxing",
                        name: "1",
                        areaId: 330400
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/zhoushan.json",
                        position: "zhoushan",
                        name: "1",
                        areaId: 330900
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/shaoxing.json",
                        position: "shaoxing",
                        name: "1",
                        areaId: 330600
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/ningbo.json",
                        position: "ningbo",
                        name: "1",
                        areaId: 330200
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/jinhua.json",
                        position: "jinhua",
                        name: "1",
                        areaId: 330700
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/taizhou.json",
                        position: "taizhou",
                        name: "1",
                        areaId: 331000
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/quzhou.json",
                        position: "quzhou",
                        name: "1",
                        areaId: 330800
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/lishui.json",
                        position: "lishui",
                        name: "1",
                        areaId: 331100
                    },
                    {
                        positionJsonLink: "http://qiniu.renmating.com/wenzhou.json",
                        position: "wenzhou",
                        name: "1",
                        areaId: 330300
                    }
                ],
                mapData: {
                    columns: ["areaName", "siteNum"],
                    rows: [
                        { areaName: "和田地区", siteNum: 123},
                        { areaName: "湖州市", siteNum: 123},
                        { areaName: "嘉兴市", siteNum: 123},
                        { areaName: "舟山市", siteNum: 123},
                        { areaName: "绍兴市", siteNum: 123},
                        { areaName: "宁波市", siteNum: 123},
                        { areaName: "金华市", siteNum: 123},
                        { areaName: "台州市", siteNum: 123},
                        { areaName: "衢州市", siteNum: 123},
                        { areaName: "丽水市", siteNum: 123},
                        { areaName: "温州市", siteNum: 123}
                    ]
                },
                mapExtend: {
                    series: {
                        //标注/标点
                        markPoint: {
                            symbol: img,
                            silent:true,
                            symbolOffset: [0, -26], //偏移
                            symbolSize: [30, 44],
                            label: {
                                show: true,
                                color: "#5E1D0B",
                                offset: [0, -6],
                                fontSize: 14,
                                fontWeight: 900
                                // position: 'inside',
                                // formatter: v => {
                                //   console.log(v);
                                //   return v.value;
                                // }
                            },
                            animation: true,
                            data: [
                                { coord: [120.153576, 30.287459], value: 59 },
                                { coord: [121.549792, 29.868388], value: 1 },
                                { coord: [120.750865, 30.762653], value: 1 },
                                { coord: [120.102398, 30.867198], value: 3 },
                                { coord: [120.582112, 29.997117], value: 4 },
                                { coord: [122.106863, 30.016028], value: 1 }
                            ]
                        }
                    }
                }
            };
        },
        methods: {
            backZhejiang() {
                this.isCity=!this.isCity
                if(!this.isCity){
                    this.mapShow=!this.mapShow
                    this.mapSettings.positionJsonLink=this.area[0].positionJsonLink
                    this.mapSettings.position=this.area[0].position
                    this.$nextTick(function(){
                        this.mapShow=!this.mapShow
                    })
                }
            }
        },
    };
</script>