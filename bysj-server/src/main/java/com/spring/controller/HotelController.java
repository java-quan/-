package com.spring.controller;

import com.spring.dao.HotelMapper;
import com.spring.entity.Hotel;
import com.spring.service.HotelService;
import dao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import tk.mybatis.mapper.entity.Example;
import util.Request;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * 地方美食 */
@Controller
public class HotelController extends BaseController
{
    @Autowired
    private HotelMapper dao;
    @Autowired
    private HotelService service;

    /**
     *  后台列表页
     *
     */
    @RequestMapping("/hotel_list")
    public String list()
    {

        // 检测是否有登录，没登录则跳转到登录页面
        if(!checkLogin()){
            return showError("尚未登录" , "./login.do");
        }
        int    pagesize = Request.getInt("pagesize" , 12); // 获取前台一页多少行数据
        Example example = new Example(Hotel.class); //  创建一个扩展搜索类
        Example.Criteria criteria = example.createCriteria();
        String where = " 1=1 ";   // 创建初始条件为：1=1
        where += getWhere();      // 从方法中获取url 上的参数，并写成 sql条件语句
        criteria.andCondition(where);   // // 创建一个扩展搜索条件类
        int page = request.getParameter("page") == null ? 1 : Integer.valueOf(request.getParameter("page"));  // 获取前台提交的URL参数 page  如果没有则设置为1
        page = Math.max(1 , page);  // 取两个数的最大值，防止page 小于1
        List<Hotel> list = service.selectPageExample(example , page , pagesize);   // 获取当前页的行数
        // 将列表写给界面使用
        assign("totalCount" , request.getAttribute("totalCount"));
        assign("list" , list);
        return json();   // 将数据写给前端
    }
//
    public String getWhere()
    {
        _var = new LinkedHashMap(); // 重置数据
        String where = " ";
        // 以下也是一样的操作，判断是否符合条件，符合则写入sql 语句
            if(!Request.get("hotelName").equals("")) {
            where += " AND hotel_name LIKE '%"+ Request.get("hotelName")+"%' ";
        }

            return where;
    }


    /**
    *  前台列表页
    *
    */
    @RequestMapping("/hotel_index")
    public String index()
    {
//            String order = Request.get("order" , "id");
//        String sort  = Request.get("sort" , "desc");
//
        Example example = new Example(Hotel.class);
        Example.Criteria criteria = example.createCriteria();
        String where = " 1=1 ";
                where += getWhere();
        criteria.andCondition(where);
//        if(sort.equals("desc")){
//            example.orderBy(order).desc();
//        }else{
//            example.orderBy(order).asc();
//        }
        int page = request.getParameter("page") == null ? 1 : Integer.valueOf(request.getParameter("page"));
        page = Math.max(1 , page);
                    List<Hotel> list = service.selectPageExample(example , page , 12);

//                assign("mapmeishifenlei5" , new CommDAO().select("SELECT id,fenleimingcheng FROM meishifenlei"));        assign("totalCount" , request.getAttribute("totalCount"));
        assign("list" , list);
        assign("where" , where);
//        assign("orderby" , order);
//        assign("sort" , sort);
        return json();
    }



        @RequestMapping("/hotel_add")
    public String add()
    {
        _var = new LinkedHashMap(); // 重置数据
        return json();   // 将数据写给前端
    }

    @RequestMapping("/hotel_updt")
    public String updt()
    {
        _var = new LinkedHashMap(); // 重置数据
        int id = Request.getInt("id");
//        String hotelAddress = Request.get("hotelAddress");
//        System.out.println(hotelAddress);
//         获取行数据，并赋值给前台jsp页面
        Hotel mmm = service.find(id);
        assign("mmm" , mmm);
        assign("updtself" , 0);

//                    assign("jingdianxinxiList" , new CommDAO().select("SELECT * FROM jingdianxinxi ORDER BY id desc"));
//            assign("meishifenleiList" , new CommDAO().select("SELECT * FROM meishifenlei ORDER BY id desc"));
        return json();   // 将数据写给前端
    }
    /**
     * 添加内容
     * @return
     */
    @RequestMapping("/hotel_insert")
    public String insert(Hotel hotel)
    {
        _var = new LinkedHashMap(); // 重置数据
        String tmp="";
        hotel.setId(null);
        service.insert(hotel);// 插入数据
        int charuid = hotel.getId().intValue();
        if(isAjax()){
            return jsonResult(hotel);
        }
        return showSuccess("保存成功" , Request.get("referer").equals("") ? request.getHeader("referer") : Request.get("referer"));
    }

    /**
    * 更新内容
    * @return
    */
    @RequestMapping("/hotel_update")
    public String update(Hotel hotel)
    {
        _var = new LinkedHashMap(); // 重置数据

        // 创建实体类
//        Hotel post = new Hotel();
//         将前台表单数据填充到实体类
//        if(!Request.get("meishibianhao").equals(""))
//        post.setMeishibianhao(Request.get("meishibianhao"));
//                if(!Request.get("mingcheng").equals(""))
//        post.setMingcheng(Request.get("mingcheng"));
//                if(!Request.get("fujinjingdian").equals(""))
//        post.setFujinjingdian(Request.get("fujinjingdian"));
//                if(!Request.get("fenlei").equals(""))
//        post.setFenlei(Request.get("fenlei"));
//                if(!Request.get("tupian").equals(""))
//        post.setTupian(Request.get("tupian"));
//                if(!Request.get("jiage").equals(""))
//        post.setJiage(Request.getDouble("jiage"));
//            if(!Request.get("meishijianjie").equals(""))
//        post.setMeishijianjie(Request.get("meishijianjie"));
//                if(!Request.get("addtime").equals(""))
//        post.setAddtime(Request.get("addtime"));
//
//        post.setId(Request.getInt("id"));
                service.update(hotel); // 更新数据
        int charuid = hotel.getId().intValue();

        if(isAjax()){
            return jsonResult(hotel);
        }

        return showSuccess("保存成功" , Request.get("referer")); // 弹出保存成功，并跳转到前台提交的 referer 页面
    }
    /**
     *  后台详情
     */
    @RequestMapping("/hotel_detail")
    public String detail()
    {
        _var = new LinkedHashMap(); // 重置数据
        int id = Request.getInt("id");
        Hotel map = service.find(id);  // 根据前台url 参数中的id获取行数据
        assign("map" , map);  // 把数据写到前台
            return json();   // 将数据写给前端
    }
    /**
     *  前台详情
     */
    @RequestMapping("/hotel_index_detail")
    public String detailweb()
    {
        _var = new LinkedHashMap(); // 重置数据
        int id = Request.getInt("id");
        Hotel map = service.find(id);


        assign("map" , map);
        return json();   // 将数据写给前端
    }
        /**
    *  删除
    */
    @RequestMapping("/hotel_delete")
    public String delete()
    {
        _var = new LinkedHashMap(); // 重置数据
        if(!checkLogin()){
            return showError("尚未登录");
        }
        int id = Request.getInt("id");  // 根据id 删除某行数据
        HashMap map = Query.make("hotel").find(id);

                service.delete(id);// 根据id 删除某行数据
                return showSuccess("删除成功",request.getHeader("referer"));//弹出删除成功，并跳回上一页
    }
}
