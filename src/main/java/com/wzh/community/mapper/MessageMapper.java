package com.wzh.community.mapper;

import com.wzh.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author wzh
 * @data 2022/8/7 -14:32
 */
@Mapper
public interface MessageMapper {
    // 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户会话的数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信数量
    int selectLetterUnreaderCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息为其他状态，比如已读
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    Message selectLastNotice(int userId, String topic);

    // 查询某个主题包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}
