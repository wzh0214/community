package com.wzh.community.controller;

import com.wzh.community.service.impl.DataServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.crypto.Data;
import java.util.Date;

/**
 * @author wzh
 * @data 2022/9/23 -19:21
 */
@Controller
public class DataController {
    @Autowired
    private DataServerImpl dataServer;

    // 统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    // 统计网站UV @DateTimeFormat让服务器知道是什么日期类型好转换
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model) {
        long uv = dataServer.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start); // 把开始和结束日期也存入model，因为转发后页面还要显示开始结束日期
        model.addAttribute("uvEndDate", end);

        return "forward:/data"; // 转发给/data处理，也可以重定向直接return "/site/admin/data";

    }

    // 统计活跃用户
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model) {
        long dau = dataServer.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);

        return "forward:/data";

    }

}
