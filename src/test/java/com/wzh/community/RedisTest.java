package com.wzh.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author wzh
 * @data 2022/9/23 -16:54
 */
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testHyperLogLog() {
        String redisKey = "test:hll:01";

        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 0; i < 1000; i++) {
            int r = (int)(Math.random() * 10000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }


    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";
        String redisKey3 = "test:hll:03";
        String redisKey4 = "test:hll:04";

        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        for (int i = 5001; i < 15000; i++) {

            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        for (int i = 15001; i < 20000; i++) {

            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:hll:union";
        Long size = redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);
        System.out.println(size);
    }




}
