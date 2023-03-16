package com.wzh.community.controller;

import com.wzh.community.annotation.LoginRequired;
import com.wzh.community.entity.User;
import com.wzh.community.service.impl.FollowServerImpl;
import com.wzh.community.service.impl.LikeServerImpl;
import com.wzh.community.service.impl.UserServerImpl;
import com.wzh.community.util.CommunityConstant;
import com.wzh.community.util.CommunityUtil;
import com.wzh.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Handler;

/**
 * @author wzh
 * @data 2022/8/3 -14:18
 */
@Controller
@RequestMapping("/user")
public class UserController {
    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domail}")
    private String domail;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.update}")
    private String uploadPath;

    @Autowired
    private UserServerImpl userServer;

    @Autowired
    private HostHolder hostHolder; // 获取当前对象

    @Autowired
    private LikeServerImpl likeServer;

    @Autowired
    private FollowServerImpl followServer;

//    @LoginRequired // 自定义注解
    @GetMapping("/setting")
    // 到账号设置页面
    public String getSettingPage() {
        return "/site/setting";
    }


    /**
     *  上传头像
     *
     *  SpringMVC中将上传的文件封装到MultipartFile对象中，通过此对象可以获取文件相关信息
     *  文件上传要求form表单的请求方式必须为post，并且添加属性enctype="multipart/form-data"
     */

    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headImage, Model model) {
        if (headImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }
        // 获取上传的文件的文件名
        String fileName = headImage.getOriginalFilename();
        // 处理文件重名问题
        String suffix = fileName.substring(fileName.lastIndexOf(".")); // 获取图片后缀名
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }
        fileName =  CommunityUtil.generateUUID() + suffix; // 生成不重复的文件名
        // 确定存放文件的路径
        File dest =  new File(uploadPath + "/" + fileName);

        try {
            // 实现上传功能
            headImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常！", e);
        }

        // 更新当前用户的头像的路径
        // http://localhost8080/community/user/header/XXX.png
        User user = hostHolder.getUser();
        String headerUrl = domail + contextPath + "/user/header/" + fileName;
        userServer.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }


    /**
     * 获取头像，/header/{fileName}就是用户头像路径的后半段
     * 因为index页面的要去访问用户的头像，并显示
     */

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
       fileName =  uploadPath + "/" + fileName;
       // 文件后缀不带.
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("image/" + suffix);


        try (
                FileInputStream fis = new FileInputStream(fileName);  // 因为是手动创建的流要关闭，java7新特性在括号中声明流会自动关闭
                ){
            ServletOutputStream os = response.getOutputStream(); // response流springMvc会自动关闭
            byte[] buffer = new byte[20];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
           logger.error("读取头像失败：" + e.getMessage() );
        }

    }


    /**
     * 更改密码
     */

    @PostMapping("/updatePassword")
    public String changePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userServer.updatePassword(user.getId(), oldPassword, newPassword);
        // 等于null是判断有没有分配空间，isEmpty是判断有没有键值对，如果不加isEmpty判断就不会执行跳转
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));

            return "/site/setting";
        }

    }

    /**
     * 个人主页
     */

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userServer.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user", user);

        // 点赞数量
        int likeCount = likeServer.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followServer.findFolloweeCount(userId, (int) CommunityConstant.valueOf("ENTITY_TYPE_USER").getValue());
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = followServer.findFollowerCount((int) CommunityConstant.valueOf("ENTITY_TYPE_USER").getValue(), userId);
        model.addAttribute("followerCount", followerCount);

        // 是否已关注，没登陆也可以看
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followServer.hasFollowed(hostHolder.getUser().getId(), (int)CommunityConstant.valueOf("ENTITY_TYPE_USER").getValue(), userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";

    }
}
