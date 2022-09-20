<template>
    <div class="difangmeishi-detail" v-loading="loading">
        <div>
<e-container>
            <div  style="margin:10px 0 0 0">
<e-module-model-box title="详情">
    
<div class="">
    <div class="goods-info clearfix">
        <div class="gallery-list">
            <e-shangpinban :images="map.hotelPicture"></e-shangpinban>
        </div>
        <div class="goods-right-content">
            <h3 class="title" v-text="map.hotelName"></h3>
            <div class="descount">
                                    <div>
                        <span class="name">
                            酒店地址：
                        </span>
                        <span class="val">
                            {{  map.hotelAddress  }}                        </span>
                    </div>

                                    <div>
                        <span class="name">
                            酒店价格：
                        </span>
                        <span class="val">
                            {{ map.hotelMoney }}                        </span>
                    </div>
                                    <div>
                        <span class="name">
                            酒店简介：
                        </span>
                        <span class="val">
                            {{ map.hotelIntroduce }}                        </span>
                    </div>
                            </div>

            
            
        </div>
    </div>

</div>

</e-module-model-box>

</div>    

</e-container>
</div>    </div>
</template>
<style type="text/scss" scoped lang="scss">
    </style>
<script>
    import api from '@/api';
    import { extend } from '@/utils/extend';
        export default {
        data() {
            return {
                loading:false,   // 加载
                                isCollect:false, // 是否已经收藏
                                map:{
                                    hotelPicture:'',
                                    hotelName:'',
                                    hotelAddress:'',
                                    hotelMoney:'',
                                    hotelIntroduce:'',
                            },
                            }
        },
                props:{
            id:{
                type:[String,Number],
                required:true
            }
        },
        watch: {
            id:{
                handler(){
                    this.loadDetail();
                },
                immediate:true
            },
                    },
        computed: {
                    },
        methods: {
            loadDetail(){
                if(this.loading) return;
                this.loading = true;
                this.$get(api.hotel.webdetail , {
                    id:this.id
                }).then(res=>{
                    this.loading = false;
                    if(res.code == api.code.OK){
                        extend(this,res.data);
                    }else{
                        this.$message.error(res.msg);
                    }
                }).catch(err=>{
                    this.loading = false;
                    this.$message.error(err.message);
                });
            },

                    },
        created() {
                    },
        mounted() {
                    },
        destroyed() {
                    }
    }
</script>
