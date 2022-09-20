<template>
    <div>
        <el-row :gutter="20" class="mgb20">
            <el-col :span="24">
                <h3 class="mgb20" v-text="data1"></h3>
                <div style="background-color:white">
                    <ve-pie :data="songStyle1"></ve-pie>
                </div>
            </el-col>
        </el-row>
    </div>
</template>
<script>
    export default {
        data() {
            return {
                data1: '营业收入增长额',
                songStyle1: {
                    columns: ['年度', '营业收入增长率'],
                    rows: []
                }
            }
        },
        created() {

        },
        mounted() {
            this.getSinger1()
        },
        methods: {
            getSinger1() { //  数量
                    this.songStyle1.rows = [{"营业收入增长率":0.0,"年度":"2015"},{"营业收入增长率":600.0,"年度":"2016"},{"营业收入增长率":35.714285714285715,"年度":"2017"},{"营业收入增长率":3.1578947368421053,"年度":"2018"},{"营业收入增长率":0.0,"年度":"2019"},{"营业收入增长率":7.244897959183673,"年度":"2020"},{"营业收入增长率":76.13986679352998,"年度":"2021"}]
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
