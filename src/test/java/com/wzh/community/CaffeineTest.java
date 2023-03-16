package com.wzh.community;

import com.wzh.community.entity.DiscussPost;
import com.wzh.community.service.impl.DiscussServerImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * @author wzh
 * @data 2022/9/25 -14:20
 */
@SpringBootTest
public class CaffeineTest {

    @Autowired
    private DiscussServerImpl discussServer;


    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("互联网秋招");
            post.setContent("2023界是真的难啊！救救孩子吧！就业形式太惨了吧");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussServer.addDiscussPost(post);
        }
    }

    @Test
    public void testCache() {
        System.out.println(discussServer.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussServer.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussServer.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussServer.findDiscussPosts(0, 0, 10, 0));
    }
}
