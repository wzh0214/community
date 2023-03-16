package com.wzh.community.service;

import com.wzh.community.entity.Message;

import java.util.List;

/**
 * @author wzh
 * @data 2022/8/7 -16:31
 */
public interface MessageServer {
    List<Message> findConversations(int userId, int offset, int limit);

    int findConversationCount(int userId);

    List<Message> findLetters(String conversationId, int offset, int limit);

    int findLetterCount(String conversationId);

    int findLetterUnreadCount(int userId, String conversationId);

    int addMessage(Message message);

    int readMessage(List<Integer> ids);
}
