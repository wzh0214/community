package com.wzh.community.quartz;

import com.wzh.community.entity.DiscussPost;
import com.wzh.community.service.impl.DiscussServerImpl;
import com.wzh.community.service.impl.ElasticSearchServiceImpl;
import com.wzh.community.service.impl.LikeServerImpl;
import com.wzh.community.util.CommunityConstant;
import com.wzh.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wzh
 * @data 2022/9/24 -16:12
 */
public class PostScoreRefreshJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussServerImpl discussPostService;

    @Autowired
    private LikeServerImpl likeService;

    @Autowired
    private ElasticSearchServiceImpl elasticSearchService;

    private static final Date epoch;
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化初始时间失败！",e);
        }
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String scoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(scoreKey);
        if(operations.size()==0){
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数：" +operations.size());
        while (operations.size()>0){
            this.refresh((Integer)operations.pop());
        }
        // 用完清空 避免占用内容过大
        redisTemplate.delete(scoreKey);
        logger.info("[任务结束] 帖子分数已刷新");
    }


    // 刷新帖子的分数的方法
    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if(post==null){
            logger.error("该帖子不存在： id = " + postId);
            return;
        }
        boolean wonderful = post.getStatus() == 1;
        int commentCount = post.getCommentCount();
        long likeCount = likeService.findEntityLikeCount((int) CommunityConstant.valueOf("ENTITY_TYPE_POST").getValue(),postId);

        //先计算权重
        double w = (wonderful?75:0) + commentCount * 10 + likeCount * 2;
        // 分数 = 权重+ 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }
}
