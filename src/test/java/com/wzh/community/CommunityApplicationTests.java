package com.wzh.community;

import com.wzh.community.entity.DiscussPost;
import com.wzh.community.entity.LoginTicket;
import com.wzh.community.entity.Message;
import com.wzh.community.mapper.DiscussPostMapper;
import com.wzh.community.mapper.LoginTicketMapper;
import com.wzh.community.mapper.MessageMapper;
import com.wzh.community.service.impl.DiscussServerImpl;
import com.wzh.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testMessage() {



        int i1 = messageMapper.selectLetterUnreaderCount(111, null);
        System.out.println(i1);
    }

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("K1", 1);

        System.out.println(redisTemplate.opsForValue().get("k1"));


    }
}
