package com.wzh.community.service.impl;

import com.wzh.community.entity.LoginTicket;
import com.wzh.community.mapper.LoginTicketMapper;
import com.wzh.community.mapper.UserMapper;
import com.wzh.community.entity.User;
import com.wzh.community.service.UserServer;
import com.wzh.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wang
 * @date 2022/7/28 - 19:07
 */
@Service
public class UserServerImpl implements UserServer, EventConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    //@Autowired
    //private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domail}")
    private String domail;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User findUserById(int id) {
       // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }


    @Override
    // 注册
    public Map<String, Object> register(User user) {
        HashMap<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "账号已存在");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已被注册");
            return map;
        }


        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5)); // 生成随机5为字符串
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0); // 普通用户
        user.setStatus(0); // 0表示没有激活，1表示激活
        user.setActivationCode(CommunityUtil.generateUUID()); // 设置激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000))); // 生成随机头像
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        // 发送邮件
        Context context = new Context(); // thymeleaf包中的Context对象
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domail + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map; // 如果注册成功，返回的map就是空

    }

    @Override
    // 激活用户
    public CommunityConstant activation(int userId, String code) {
        //User user = userMapper.selectById(userId);
        User user = getCache(userId);
        if (user == null) {
            user = initCache(userId);
        }
        if (user.getStatus() == 1) {
            return CommunityConstant.valueOf("ACTIVATION_REPEAT"); // valueOf()获取枚举类是否有名叫ACTIVATION_REPEAT的对象值
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return CommunityConstant.valueOf("ACTIVATION_SUCCESS");
        } else {
            return CommunityConstant.valueOf("ACTIVATION_FAILURE");
        }
    }


    @Override
    // 登陆判断
    public Map<String, Object> login(String username, String password, long expiredSeconds) { // expiredSecond 用于生成登陆凭证有效时间
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号输入有误");
            return map;
        }
        // 验证状态，账号是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活");
            return map;
        }
        // 验证密码，用户传入的密码加上字符串加密和用户实际的密码比较
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码输入有误");
            return map;
        }

        // 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000)); // 毫秒数
        //loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 把对象序列化为字符串保存到redis
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map; // 返回登陆凭证


    }

    @Override
    // 登出
    public void logout(String ticket) {
        //loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket)redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    @Override
    // 根据ticket查找并返回LoginTicket对象
    public LoginTicket findLoginTicket(String ticket) {
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }


    @Override
    // 更新头像
    public int updateHeader(int userId, String headerUrl) {
        //return userMapper.updateHeader(id, headerUrl);
        // 因为访问mysql和redis不能放在一个事务内，清除缓存放在跟新头像后面，如果放在前面如果跟新失败还清除缓存就不太好
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }


    @Override
    // 更改密码
    public Map<String, Object> updatePassword(int id, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空");
            return map;
        }

        // 原密码不能和新密码一样
        if (oldPassword.equals(newPassword)) {
            map.put("newPasswordMsg", "新密码不能和原密码相同");
            return map;
        }

        // 判断原密码
        //User user = userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }

        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPassword.equals(user.getPassword())) {
            map.put("oldPasswordMsg", "原密码输入有误");
            return map;
        }

        // 修改密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);
        clearCache(id);

        return map;
    }


    @Override
    public User findUserByName(String userName) {
        return userMapper.selectByName(userName);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;

    }

    // 3.数据变更时清除缓存
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }


    // sercurity时写的
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
