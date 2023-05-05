package com.xxy.blog.controller;

import com.xxy.blog.util.CommunityConstant;
import com.xxy.blog.util.CommunityUtil;
import com.xxy.blog.util.LoginUser;
import com.xxy.blog.entity.Conversation;
import com.xxy.blog.entity.Page;
import com.xxy.blog.entity.User;
import com.xxy.blog.service.IConversationService;
import com.xxy.blog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class ConversationController implements CommunityConstant {

    @Autowired
    private IConversationService conversationService;

    @Autowired
    private IUserService userService;

    @Autowired
    private LoginUser loginUser;

    @RequestMapping("/conversation/list")
    public String getConversationList(Model model, Page page){
        User user = loginUser.getUser();
        /** 分页信息 */
        page.setLimit(6);
        page.setPath("/conversation/list");
        page.setRows(conversationService.findConversationCount(user.getId()));
        /** 会话列表 */
        List<Conversation> conversationList = conversationService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for (Conversation conversation : conversationList) {
                Map<String,Object> map = new HashMap<>();
                if(conversation.getContent().length()>50){
                    conversation.setContent(conversation.getContent().substring(0,50) + "...");
                }
                map.put("conversation",conversation);
                map.put("conversationCount",conversationService.findConversationCount(conversation.getConversationId()));
                map.put("unreadCount",conversationService.findConversationUnreadCount(user.getId(), conversation.getConversationId()));
                int targetId = (user.getId() == conversation.getFromId()?conversation.getToId() : conversation.getFromId());
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        return "/page/conversation";
    }

    @RequestMapping("/conversation/detail/{conversationId}")
    public String getConversationDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        String [] conversationIds = conversationId.split("_");
        if(!conversationIds[0].equals("" + loginUser.getUser().getId())&&!conversationIds[1].equals("" + loginUser.getUser().getId()))return "/error/500";
        /** 分页信息 */
        page.setLimit(5);
        page.setPath("/conversation/detail/" + conversationId);
        page.setRows(conversationService.findConversationCount(conversationId));
        List<Conversation> conversationList = conversationService.findConversationsById(conversationId,page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(int i = conversationList.size() - 1;i >= 0;i--){
                Map<String,Object> map = new HashMap<>();
                Conversation conversation = conversationList.get(i);
                map.put("conversation",conversation);
                map.put("fromUser",userService.findUserById(conversation.getFromId()));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        model.addAttribute("target",getConversationTarget(conversationId));
        /** 设置已读 */
        List<Integer> ids = getConversationIds(conversationList);
        if(!ids.isEmpty()){
            conversationService.readConversation(ids);
        }

        return "/page/conversation-detail";
    }

    private List<Integer> getConversationIds(List<Conversation> conversationList){
        List<Integer> ids = new ArrayList<>();
        if(conversationList != null){
            for (Conversation conversation : conversationList) {
                if(loginUser.getUser().getId() == conversation.getToId() && conversation.getStatus() == 0){
                    ids.add(conversation.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(path = "/conversation/send",method = RequestMethod.POST)
    @ResponseBody
    public String addConversation(String toName,String content){
        if(content==null||content.equals("")){
            return CommunityUtil.getJSONString(404,"输入不为空");
        }
        User target = userService.findUserByName(toName);
        Conversation conversation = new Conversation();
        conversation.setFromId(loginUser.getUser().getId());
        conversation.setToId(target.getId());
        conversation.setConversationId(createConversationId(conversation.getFromId(), conversation.getToId()));
        conversation.setContent(content);
        conversation.setCreateTime(new Date());
        conversationService.addConversation(conversation);

        return CommunityUtil.getJSONString(0);
    }


    private User getConversationTarget(String conversationId){
        String [] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(loginUser.getUser().getId()==id0){
            return userService.findUserById(id1);
        }else return userService.findUserById(id0);
    }

    public String createConversationId(int id1,int id2){
        if(id1 < id2)return id1 + "_" + id2;
        else return id2 + "_" + id1;
    }

}
