package com.wzh.community.controller;

import com.google.code.kaptcha.Producer;
import com.wzh.community.entity.User;
import com.wzh.community.service.impl.UserServerImpl;
import com.wzh.community.util.CommunityConstant;
import com.wzh.community.util.CommunityUtil;
import com.wzh.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wzh
 * @data 2022/7/31 -12:58
 */
@Controller
public class LoginController {
    public static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserServerImpl userServer;

    @Autowired
    private Producer kaptchaProducer; // 声明了配置类，直接从容器中获取

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @PostMapping("/register")
    public String register(Model model, User user) { // springMVC会自动为user注入值，并放入model，因为user是实体类，基于同名原则
        Map<String, Object> map = userServer.register(user);
        if (map == null || map.isEmpty()) { // 说明注册成功
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封邮件，请尽快激活！");
            model.addAttribute("target", "/index"); // 激活成功后，要跳到的目标页面
            return "/site/operate-result"; // 注册成功页面
        } else { // 注册失败，返回注册页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";

        }

    }

    // http://localhost:8080/community/activation/101/code
    // 101 指用户id code 指激活码
    // @PathVariable可以将URL中占位符参数{xxx}绑定到处理器类的方法形参中@PathVariable(“xxx“)
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        CommunityConstant result = userServer.activation(userId, code);
        if (result == CommunityConstant.valueOf("ACTIVATION_SUCCESS")) {
            model.addAttribute("msg", "激活成功，您的账号可以使用了！");
            model.addAttribute("target", "/login");
        } else if (result == CommunityConstant.valueOf("ACTIVATION_REPEAT")) {
            model.addAttribute("msg", "无效激活，该账号已经激活过了！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码有误！");
            model.addAttribute("target", "/index");
        }

        return "/site/operate-result";
    }

    // 获取验证码图片
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();

        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码传给session
        //session.setAttribute("kaptcha", text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");

        try {
            ServletOutputStream outputStream = response.getOutputStream(); // ServletOutputStream 流不用自动关闭，springMVC会自动关闭
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }

    }


    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) { // 从cookie中取值，因为生成验证码时有传// 普通属性就不会放入model，可以自己加入或者 从request请求中取在login页面中th:value中有写
        // 先检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String reidsKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(reidsKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) { // equalsIgnoreCase()：不区分大小写
            model.addAttribute("codeMsg", "验证码错误");
            return "/site/login";
        }

        // 检查账号密码
        long expiredSeconds = rememberme ? CommunityConstant.valueOf("REMEMBER_EXPIRED_SECONDS").getValue() : CommunityConstant.valueOf("DEFAULT_EXPIRED_SECONDS").getValue(); // 登陆过期时间
        Map<String, Object> map = userServer.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) { // 说明账号密码没错
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString()); // 创建cookie
            cookie.setPath(contextPath); // 设置cookie生效范围
//             expiredSeconds = (int)(expiredSeconds / 1000);
            cookie.setMaxAge((int) expiredSeconds); // 设置cookie的生存时间(秒)，只能传int类型的，所以把long类型除1000转int
            response.addCookie(cookie); // 发送cookie
            return "redirect:/index"; // 用户登陆后，要重定向在发一个请求处理
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg")); // 如果没值，反正显示为空
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }



    }


    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) { //@CookieValue 获取请求的cookie值
        userServer.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}
