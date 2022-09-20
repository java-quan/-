<template>
    <div>
        <div style="height:100%;width:100%" >
            <el-row :gutter="10" type="flex" class="grid-one-contentheight" justify="center">
                <el-col :span="24">
                    <h3 class="mgb60">年度财务分析报表</h3>
                    <div style="background-color:white">
                        <ve-histogram :data="country" :theme="options1"></ve-histogram>
                    </div>
                </el-col>
            </el-row>
        </div>

    </div>
</template>
<script>
    export default {
        data() {
            return {
                options1: {
                    color: ['red', 'blue']
                },
                country: {
                    columns: ['年度', '支出', '收入'],
                    rows: []
                }
            }
        },
        created() {

        },
        mounted() {
            this.getSinger4()
        },
        methods: {
            getSinger4() {
                    this.country.rows = [{"收入":10000,"支出":500,"年度":"2015"},{"收入":70000,"支出":18000,"年度":"2016"},{"收入":95000,"支出":21820,"年度":"2017"},{"收入":98000,"支出":85472,"年度":"2018"},{"收入":98000,"支出":800,"年度":"2019"},{"收入":105100,"支出":5060,"年度":"2020"},{"收入":185123,"支出":9323,"年度":"2021"}]
            }
        }
    }

</script>

<style scoped>
    .grid-content {
        display: flex;
        align-items: center;
        height: 50px;
    }

    .grid-cont-center {
        flex: 1;
        text-align: center;
        font-size: 14px;
        color: darkgray;
    }

    .grid-num {
        font-size: 30px;
        font-weight: bold;
    }
</style>
