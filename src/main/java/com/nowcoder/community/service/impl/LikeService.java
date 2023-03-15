package com.nowcoder.community.service.impl;

import com.nowcoder.community.entity.Blog;
import com.nowcoder.community.service.IBlogService;
import com.nowcoder.community.service.ILikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LikeService implements ILikeService , CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IBlogService discussPostService;

    /** 点赞 */
    @Override
    public void like(int userId, int entityType, int entityId,int entityUserId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
//        if(isMember){
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else {
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                String likePostKey = null;
                if(entityType == ENTITY_TYPE_BLOG){
                    likePostKey = RedisKeyUtil.getLikePostKey(userId);
                }

                boolean isMember = operations.opsForSet().isMember(entityLikeKey,userId);

                operations.multi();

                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                    if(likePostKey != null)operations.opsForZSet().remove(likePostKey,entityId);
                }else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                    if(likePostKey != null)operations.opsForZSet().add(likePostKey,entityId,System.currentTimeMillis());
                }
                return operations.exec();
            }
        });
    }

    /** 查询某实体点赞的数量 */
    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /** 查询某人对某实体的点赞状态 */
    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;
    }

    /** 查询某个用户获得的赞 */
    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }

    @Override
    public long findLikePostCount(int userId) {
        String likePostKey = RedisKeyUtil.getLikePostKey(userId);
        return redisTemplate.opsForZSet().zCard(likePostKey);
    }

    @Override
    public List<Blog> findLikePosts(int userId, int offset, int limit) {
        List<Blog> blogs = new ArrayList<>();
        String likePostKey = RedisKeyUtil.getLikePostKey(userId);
        Set<Integer> postIds = redisTemplate.opsForZSet().reverseRange(likePostKey,offset,offset + limit - 1);
        for (Integer postId : postIds) {
            Blog post = discussPostService.findBlogById(postId);
            blogs.add(post);
        }
        return blogs;
    }

    @Override
    public void deleteLikePosts(int id) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(ENTITY_TYPE_BLOG,id);
                String likePostKey = null;
                Set<Integer> userIdsSet = new HashSet<>();
                userIdsSet = redisTemplate.opsForSet().members(entityLikeKey);

                operations.multi();

                List<Integer> userIds = new ArrayList<>(userIdsSet);
                for(int i = 0;i < userIds.size();i++){
                    likePostKey = RedisKeyUtil.getLikePostKey(userIds.get(i));
                    operations.opsForZSet().remove(likePostKey,id);
                }
                return operations.exec();
            }
        });

    }


}
