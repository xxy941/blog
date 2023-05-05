package com.xxy.blog.service;

import com.xxy.blog.entity.Conversation;

import java.util.List;

public interface IConversationService {

    public List<Conversation> findConversations(int userId, int offset, int limit);

    public int findConversationCount(int userId);

    public List<Conversation> findConversationsById(String conversationId, int offset, int limit);

    public int findConversationCount(String conversationId);

    public int findConversationUnreadCount(int userId, String conversationId);

    public int addConversation(Conversation conversation);

    public int readConversation(List<Integer> ids);

}
