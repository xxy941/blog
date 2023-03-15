package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogMapper {
    List<Blog> selectBlogs(int userId, int offset, int limit, int orderMode);

    //@Param注解用于给参数取别名
    //如果只有一个参数，并且在<if>里使用，则必须加别名

    int selectBlogRows(@Param("userId") int userId);

    int insertBlog(Blog blog);

    List<Blog> selectBlogsByTag(String tag, int offset, int limit);

    int selectTagBlogRows(String tag);

    Blog selectBlogById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);

    int updateScore(int id,double score);
}
