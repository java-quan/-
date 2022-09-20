package com.spring.controller;

import com.spring.dao.TrafficMapper;
import com.spring.entity.Traffic;
import com.spring.service.TrafficService;
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
 * 交通管理 */
@Controller
public class TrafficController extends BaseController
{
    @Autowired
    private TrafficMapper dao;
    @Autowired
    private TrafficService service;

    /**
     *  后台列表页
     *
     */
    @RequestMapping("/traffic_list")
    public String list()
    {

        // 检测是否有登录，没登录则跳转到登录页面
        if(!checkLogin()){
            return showError("尚未登录" , "./login.do");
        }
        int    pagesize = Request.getInt("pagesize" , 12); // 获取前台一页多少行数据
        Example example = new Example(Traffic.class); //  创建一个扩展搜索类
        Example.Criteria criteria = example.createCriteria();
        String where = " 1=1 ";   // 创建初始条件为：1=1
        where += getWhere();      // 从方法中获取url 上的参数，并写成 sql条件语句
        criteria.andCondition(where);   // // 创建一个扩展搜索条件类
        int page = request.getParameter("page") == null ? 1 : Integer.valueOf(request.getParameter("page"));  // 获取前台提交的URL参数 page  如果没有则设置为1
        page = Math.max(1 , page);  // 取两个数的最大值，防止page 小于1
        List<Traffic> list = service.selectPageExample(example , page , pagesize);   // 获取当前页的行数
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
            if(!Request.get("trafficName").equals("")) {
            where += " AND traffic_name LIKE '%"+ Request.get("trafficName")+"%' ";
        }

            return where;
    }


    /**
    *  前台列表页
    *
    */
    @RequestMapping("/traffic_index")
    public String index()
    {
        Example example = new Example(Traffic.class);
        Example.Criteria criteria = example.createCriteria();
        String where = " 1=1 ";
        where += getWhere();
        criteria.andCondition(where);
        int page = request.getParameter("page") == null ? 1 : Integer.valueOf(request.getParameter("page"));
        page = Math.max(1 , page);
        List<Traffic> list = service.selectPageExample(example , page , 12);
        assign("list" , list);
        assign("where" , where);
        return json();
    }



        @RequestMapping("/traffic_add")
    public String add()
    {
        _var = new LinkedHashMap(); // 重置数据
        return json();   // 将数据写给前端
    }

    @RequestMapping("/traffic_updt")
    public String updt(Integer id)
    {
        _var = new LinkedHashMap(); // 重置数据
         id = Request.getInt("id");
        Traffic mmm = service.find(id);
        assign("mmm" , mmm);
        assign("updtself" , 0);
        return json();   // 将数据写给前端
    }
    /**
     * 添加内容
     * @return
     */
    @RequestMapping("/traffic_insert")
    public String insert(Traffic traffic)
    {
        _var = new LinkedHashMap(); // 重置数据
        String tmp="";
        traffic.setId(null);
        service.insert(traffic);// 插入数据
        int charuid = traffic.getId().intValue();
        if(isAjax()){
            return jsonResult(traffic);
        }
        return showSuccess("保存成功" , Request.get("referer").equals("") ? request.getHeader("referer") : Request.get("referer"));
    }

    /**
    * 更新内容
    * @return
    */
    @RequestMapping("/traffic_update")
    public String update(Traffic traffic)
    {
        _var = new LinkedHashMap(); // 重置数据
                service.update(traffic); // 更新数据
        int charuid = traffic.getId().intValue();

        if(isAjax()){
            return jsonResult(traffic);
        }

        return showSuccess("保存成功" , Request.get("referer")); // 弹出保存成功，并跳转到前台提交的 referer 页面
    }
    /**
     *  后台详情
     */
    @RequestMapping("/traffic_detail")
    public String detail()
    {
        _var = new LinkedHashMap(); // 重置数据
        int id = Request.getInt("id");
        Traffic map = service.find(id);  // 根据前台url 参数中的id获取行数据
        assign("map" , map);  // 把数据写到前台
            return json();   // 将数据写给前端
    }
    /**
     *  前台详情
     */
    @RequestMapping("/traffic_index_detail")
    public String detailweb()
    {
        _var = new LinkedHashMap(); // 重置数据
        int id = Request.getInt("id");
        Traffic map = service.find(id);


        assign("map" , map);
        return json();   // 将数据写给前端
    }
        /**
    *  删除
    */
    @RequestMapping("/traffic_delete")
    public String delete()
    {
        _var = new LinkedHashMap(); // 重置数据
        if(!checkLogin()){
            return showError("尚未登录");
        }
        int id = Request.getInt("id");  // 根据id 删除某行数据
        HashMap map = Query.make("traffic").find(id);

                service.delete(id);// 根据id 删除某行数据
                return showSuccess("删除成功",request.getHeader("referer"));//弹出删除成功，并跳回上一页
    }
}
