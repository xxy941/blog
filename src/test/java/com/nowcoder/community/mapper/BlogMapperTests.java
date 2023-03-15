package com.nowcoder.community.mapper;

import com.nowcoder.community.dao.BlogMapper;
import com.nowcoder.community.entity.Blog;
import com.nowcoder.community.service.IBlogService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BlogMapperTests {
    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private IBlogService discussPostService;

    @Test
    public void testSelectPost(){
        List<Blog> list = blogMapper.selectBlogs(0,0,10,0);
        for (Blog blog : list) {
            System.out.println(blog);
        }
        int rows = blogMapper.selectBlogRows(0);
        System.out.println(rows);
    }

    @Test
    public void initDataForTest(){
        for(int i = 0;i < 1700000;i++){
            Blog post = new Blog();
            post.setUserId(111);
            post.setTitle("互联网求职暖村计划");
            post.setContent("今年的就业形势，确实不容乐观。过了个年，仿佛跳水一般，整个讨论区哀鸿遍野！19届真的没人要了吗？！18届被优化真的没有出路了吗？！大家的“哀嚎”与“悲惨遭遇”牵动了每日潜伏于讨论区的牛客小哥哥小姐姐们的心，于是牛客决定：是时候为大家做点什么了！为了帮助大家度过“寒冬”，牛客网特别联合60+家企业，开启互联网求职暖春计划，面向18届&19届，拯救0 offer！");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addBlog(post);
        }
    }

    @Test
    public void testCache(){
        System.out.println(discussPostService.findBlogs(0,0,10,1));
        System.out.println(discussPostService.findBlogs(0,0,10,1));
        System.out.println(discussPostService.findBlogs(0,0,10,1));
        System.out.println(discussPostService.findBlogs(0,0,10,0));
    }

    @Test
    public void testCache1(){
        int[] a = new int[] { 9, 2, 5, 6, 10, 7, 8, 1, 4, 3 };

        for (int i = 1; i < a.length; i++) {
            int temp = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > temp) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = temp;
        }
        for(int i = 0;i < a.length;i++){
            System.out.print(a[i] + " ");
        }

    }



}
