<template>
    <div>
        <el-row :gutter="20" class="mgb20">
            <el-col :span="24">
                <h3 class="mgb20" v-text="data1"></h3>
                <div style="background-color:white">
                    <ve-line :data="songStyle1"></ve-line>
                </div>
            </el-col>
        </el-row>
        <el-row :gutter="20" class="mgb20">
            <el-col :span="24">
                <h3 class="mgb20" v-text="data2"></h3>
                <div style="background-color:white">
                    <ve-line :data="songStyle2"></ve-line>
                </div>
            </el-col>
        </el-row>
    </div>
</template>
<script>
    export default {
        data() {
            return {
                data1: '2021年月度财务分析',
                data2: '2020年月度财务分析',
                songStyle1: {
                    columns: ['月份', '支出', '收入', '利润'],
                    rows: []
                },
                songStyle2: {
                    columns: ['月份', '支出', '收入', '利润'],
                    rows: []
                }
            }
        },
        created() {

        },
        mounted() {
            this.getSinger1()
            this.getSinger2()
        },
        methods: {
            getSinger1() { //  数量
                    this.songStyle1.rows = [{"收入":0,"支出":0,"利润":0,"月份":"01"},{"收入":0,"支出":0,"利润":0,"月份":"02"},{"收入":0,"支出":0,"利润":0,"月份":"03"},{"收入":0,"支出":0,"利润":0,"月份":"04"},{"收入":0,"支出":0,"利润":0,"月份":"05"},{"收入":0,"支出":0,"利润":0,"月份":"06"},{"收入":0,"支出":0,"利润":0,"月份":"07"},{"收入":0,"支出":0,"利润":0,"月份":"08"},{"收入":0,"支出":0,"利润":0,"月份":"09"},{"收入":0,"支出":0,"利润":0,"月份":"10"},{"收入":0,"支出":0,"利润":0,"月份":"11"},{"收入":0,"支出":0,"利润":0,"月份":"12"}]
            },
            getSinger2() { //  数量
                this.songStyle2.rows = [{"收入":3900,"支出":0,"利润":3900,"月份":"01"},{"收入":11500,"支出":1800,"利润":9700,"月份":"02"},{"收入":52000,"支出":0,"利润":52000,"月份":"03"},{"收入":0,"支出":0,"利润":0,"月份":"04"},{"收入":11800,"支出":0,"利润":11800,"月份":"05"},{"收入":12800,"支出":0,"利润":12800,"月份":"06"},{"收入":52000,"支出":2800,"利润":49200,"月份":"07"},{"收入":12800,"支出":0,"利润":12800,"月份":"08"},{"收入":0,"支出":0,"利润":0,"月份":"09"},{"收入":24300,"支出":2800,"利润":21500,"月份":"10"},{"收入":3900,"支出":1923,"利润":1977,"月份":"11"},{"收入":123,"支出":0,"利润":123,"月份":"12"}]
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
