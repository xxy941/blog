package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Blog;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.IElasticsearchService;
import com.nowcoder.community.service.ILikeService;
import com.nowcoder.community.service.IUserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private IElasticsearchService elasticsearchService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ILikeService likeService;

    /** search?keyword=xxx */
    @RequestMapping("/search")
    public String search(String keyword, Page page, Model model){
        /** 搜索帖子 */
        SearchPage<Blog> searchPage = elasticsearchService.searchBlog(keyword, page.getCurrent() - 1, page.getLimit());

        List<Map<String, Object>> blogs = new ArrayList<Map<String, Object>>();
        if (searchPage != null) {
            for (SearchHit<Blog> blogSearchHit : searchPage) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                Blog blog = blogSearchHit.getContent();
                map.put("blog", blog);
                // 作者
                map.put("user", userService.findUserById(blog.getUserId()));
                //点赞
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_BLOG, blog.getId()));

                blogs.add(map);
            }
        }

        model.addAttribute("blogs", blogs);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchPage == null ? 0 : (int) searchPage.getTotalElements());

        return "/site/search";
    }

}
