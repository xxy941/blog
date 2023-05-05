package com.xxy.blog.dao;

import com.xxy.blog.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConversationMapper {

    /** 查询当前用户的会话列表,针对每个会话只返回一条最新的私信 */
    List<Conversation> selectConversations(int userId, int offset, int limit);

    /** 查询当前用户的会话数量 */
    int selectConversationsCount(int userId);

    /** 查询某个会话所包含的私信列表 */
    List<Conversation> selectConversationsById(String conversationId, int offset, int limit);

    /** 查询某个会话所包含的私信数量 */
    int selectConversationCount(String conversationId);

    /** 查询未读私信的数量 */
    int selectConversationUnreadCount(int userId, String conversationId);

    /** 新增消息 */
    int insertConversation(Conversation conversation);

    /** 修改消息的状态 */
    int updateStatus(List<Integer> ids,int status);

}
