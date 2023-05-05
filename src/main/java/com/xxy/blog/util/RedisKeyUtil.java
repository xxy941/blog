package com.xxy.blog.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    /** 关注目标 */
    private static final String PREFIX_FOLLOWEE = "followee";
    /** 粉丝 */
    private static final String PREFIX_FOLLOWER = "follower";
    /** 验证码 */
    private static final String PREFIX_KAPTCHA = "kaptcha";
    /** 登录凭证 */
    private static final String PREFIX_TICKET = "ticket";

    /** 用户点赞的帖子 */
    private static final String PREFIX_LIKE_POST = "like:post";




    /** 某个实体的赞 */
    /** like:entity:entityType:entityId -> set(userId) */
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + ":" + entityType + ":" + entityId;
    }
    /** 某个用户的赞 */
    /** like:user:userId -> int */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + ":" + userId;
    }

    /** 某个用户关注的实体 */
    /** followee:userId:entityType -> zset(entityId,now) */
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + ":" + userId + ":" + entityType;
    }

    /** 某个用户拥有的粉丝 */
    /** follower:entityType:entityId -> zset(userId,now) */
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + ":" + entityType + ":" + entityId;
    }

    /** 登录验证码 */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + ":" + owner;
    }

    /** 登录凭证 */
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + ":" + ticket;
    }

    /** 用户点赞的帖子 */
    public static String getLikePostKey(int userId){
        return PREFIX_LIKE_POST + ":" + userId;
    }

}











