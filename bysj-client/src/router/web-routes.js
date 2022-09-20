
export default [
{
    path:"yonghuadd",
        name:"IndexyonghuAdd",
    component: () => import("@/views/yonghu/webadd"),
    meta: { title:"用户添加"  }
},
{
    path:"xinwenxinxi",
        name:"IndexxinwenxinxiList",
    component: () => import("@/views/xinwenxinxi/index"),
    meta: { title:"论坛信息列表"  }
},
{
    path:"xinwenxinxidetail",
        name:"IndexxinwenxinxiDetail",
    props:route=>({id:route.query.id}),
    component: () => import("@/views/xinwenxinxi/webdetail"),
    meta: { title:"论坛信息详情"  }
},
{
    path:"liuyanbanadd",
        name:"IndexliuyanbanAdd",
    component: () => import("@/views/liuyanban/webadd"),
    meta: { title:"留言板添加" ,authLogin:true,msg:true }
},
{
    path:"jingdianxinxi",
        name:"IndexjingdianxinxiList",
    component: () => import("@/views/jingdianxinxi/index"),
    meta: { title:"景点信息列表"  }
},
{
    path:"jingdianxinxidetail",
        name:"IndexjingdianxinxiDetail",
    props:route=>({id:route.query.id}),
    component: () => import("@/views/jingdianxinxi/webdetail"),
    meta: { title:"景点信息详情"  }
},
{
    path:"lvyouxianlu",
        name:"IndexlvyouxianluList",
    component: () => import("@/views/lvyouxianlu/index"),
    meta: { title:"旅游线路列表"  }
},
{
    path:"lvyouxianludetail",
        name:"IndexlvyouxianluDetail",
    props:route=>({id:route.query.id}),
    component: () => import("@/views/lvyouxianlu/webdetail"),
    meta: { title:"旅游线路详情"  }
},
{
    path:"yudingadd",
        name:"IndexyudingAdd",
    component: () => import("@/views/yuding/webadd"),
    meta: { title:"预定添加" ,authLogin:true,msg:true }
},
{
    path:"difangmeishi",
        name:"IndexdifangmeishiList",
    component: () => import("@/views/difangmeishi/index"),
    meta: { title:"地方美食列表"  }
},
{
    path:"difangmeishidetail",
        name:"IndexdifangmeishiDetail",
    props:route=>({id:route.query.id}),
    component: () => import("@/views/difangmeishi/webdetail"),
    meta: { title:"地方美食详情"  }
},

    {
        path:"hotel",
        name:"HotelIndex",
        component: () => import("@/views/hotel/index"),
        meta: { title:"酒店列表"  }
    },
    {
        path:"traffic",
        name:"trafficIndex",
        component: () => import("@/views/traffic/index"),
        meta: { title:"交通信息列表"  }
    },
    {
        path:"trafficwebdetail",
        name:"trafficWebDetail",
        props:route=>({id:route.query.id}),
        component: () => import("@/views/traffic/webdetail"),
        meta: { title:"交通信息详情"  }
    },


    {
        path:"hotelwebdetail",
        name:"hotelWebDetail",
        props:route=>({id:route.query.id}),
        component: () => import("@/views/hotel/webdetail"),
        meta: { title:"酒店详情"  }
    },
]
