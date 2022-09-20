<template>
    <div class="difangmeishi-add" v-loading="loading">
        <el-card class="box-card">
            <div slot="header" class="clearfix updt">
                                    <el-page-header @back="$router.go(-1)" content="编辑交通信息">
                    </el-page-header>
                            </div>
            <div class="form-database-form">

                                <el-form :model="form" ref="formModel" label-width="130px" status-icon validate-on-rule-change>
                                <el-form-item label="交通工具名称" prop="trafficName" >
                                            <el-input  style="width:250px;" v-model="form.trafficName" />                                    </el-form-item>

                                <el-form-item label="价格" prop="trafficMoney">
                                            <el-input style="width:450px;" v-model="form.trafficMoney" />
                                </el-form-item>

                                <el-form-item label="相关备注" prop="trafficRemark">
                                            <el-input style="width:450px;" v-model="form.trafficRemark" />
                                </el-form-item>
                                <el-form-item label="相关图片" prop="trafficPicture">
                                            <e-upload-images v-model="form.trafficPicture">
                                            </e-upload-images>                                   </el-form-item>

                                                    

                                <el-form-item v-if="btnText">
                    <el-button type="primary" @click="submit">{{ btnText }}</el-button>
                </el-form-item>
            </el-form>

            </div>
        </el-card>
    </div>
</template>
<style type="text/scss" scoped lang="scss">
// .difangmeishi-add{

// }
</style>
<script>
    import api from '@/api'
    import rule from '@/utils/rule'
    import { extend } from '@/utils/extend'

    
    export default {
        name:'hotel-updt',
        data() {
            return {
                rule,
                loading:false,
                form:{
                    trafficName:'',
                    trafficMoney:'',
                    trafficRemark:'',
                    trafficPicture:'',
                },

            }
        },
                watch: {
                        id: {
                handler() {
                    this.loadInfo();
                }
            }
                    },
        props: {
            isRead:{
                type:Boolean,
                default:true
            },
            btnText:{
                type:String,
                default:'提交'
            },
                        id: {
                type: [String, Number],
                required: true
            },
                    },

        computed: {},
        methods: {
            submit(){
                this.$refs.formModel.validate().then(res=>{
                    if(this.loading)return;
                    this.loading = true;
                    var form = this.form;

                    this.$post(api.traffic.update,form).then(res=>{
                        this.loading = false;
                        if(res.code == api.code.OK){
                            this.$message.success('修改成功');
                            this.$emit('success' , res.data);
                            this.$refs.formModel.resetFields();
                                                        this.loadInfo();
                                                    }else{
                            this.$message.error(res.msg);
                        }
                    }).catch(err=>{
                        this.loading = false;
                        this.$message.error(err.message);
                    })

                }).catch(err=>{
                    console.log(err.message);
                })

            },
            loadInfo(){

                                // 更新数据,获取数据
                this.loading = true;
                var form = this.form;
                this.$post(api.traffic.edit , {
                    id:this.id
                }).then(res=>{
                    this.loading = false;
                    if(res.code == api.code.OK){
                        extend(this , res.data);
                        this.form = res.data.mmm;

                    }else{
                        this.$message.error(res.msg);
                        this.$router.go(-1);
                    }
                }).catch(err=>{
                    this.$message.error(err.message);
                    this.$router.go(-1);
                });
                            },
                    },
        created() {
            this.loadInfo();
                    },
        mounted() {
                    },
        destroyed() {
                    }
    }
</script>
