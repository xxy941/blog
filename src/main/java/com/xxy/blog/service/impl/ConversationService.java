package com.xxy.blog.service.impl;

import com.xxy.blog.dao.ConversationMapper;
import com.xxy.blog.service.IConversationService;
import com.xxy.blog.entity.Conversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class ConversationService implements IConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Override
    public List<Conversation> findConversations(int userId, int offset, int limit) {
        return conversationMapper.selectConversations(userId,offset,limit);
    }

    @Override
    public int findConversationCount(int userId) {
        return conversationMapper.selectConversationsCount(userId);
    }

    @Override
    public List<Conversation> findConversationsById(String conversationId, int offset, int limit) {
        return conversationMapper.selectConversationsById(conversationId,offset,limit);
    }

    @Override
    public int findConversationCount(String conversationId) {
        return conversationMapper.selectConversationCount(conversationId);
    }

    @Override
    public int findConversationUnreadCount(int userId, String conversationId) {
        return conversationMapper.selectConversationUnreadCount(userId,conversationId);
    }

    @Override
    public int addConversation(Conversation conversation) {
        conversation.setContent(HtmlUtils.htmlEscape(conversation.getContent()));
        return conversationMapper.insertConversation(conversation);
    }

    @Override
    public int readConversation(List<Integer> ids) {
        return conversationMapper.updateStatus(ids,1);
    }


}
