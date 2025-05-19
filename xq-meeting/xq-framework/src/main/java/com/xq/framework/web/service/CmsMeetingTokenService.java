package com.xq.framework.web.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.xq.common.constant.CacheConstants;
import com.xq.common.constant.Constants;
import com.xq.common.core.redis.RedisCache;
import com.xq.common.utils.DateUtils;
import com.xq.common.utils.ServletUtils;
import com.xq.common.utils.StringUtils;
import com.xq.common.utils.uuid.IdUtils;
import com.xq.system.domain.meeting.CmsMeeting;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CmsMeeting token验证处理
 *
 * @author xq
 */
@Component
@Slf4j
public class CmsMeetingTokenService {

    private static final long MILLIS_SECOND = 1000;

    private static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    @Value("${sign.token.expireTime}")
    private int expireTime;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取CmsMeeting身份信息
     *
     * @return CmsMeeting信息
     */
    public CmsMeeting getCmsMeeting(String token) {
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的权限以及CmsMeeting信息
                String uuid = (String) claims.get(Constants.CMS_MEETING_KEY);
                String meetingKey = getTokenKey(uuid);
                String meeting = redisCache.getCacheObject(meetingKey);
                return JSON.parseObject(meeting, new TypeReference<CmsMeeting>() {});
            } catch (Exception e) {
                log.error("获取CmsMeeting信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 设置CmsMeeting身份信息
     */
    public void setCmsMeeting(CmsMeeting cmsMeeting) {
        if (cmsMeeting != null && StringUtils.isNotEmpty(cmsMeeting.getToken())) {
            refreshToken(cmsMeeting);
        }
    }

    /**
     * 删除CmsMeeting身份信息
     */
    public void delCmsMeeting(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String meetingKey = getTokenKey(token);
            redisCache.deleteObject(meetingKey);
        }
    }

    /**
     * 创建令牌
     *
     * @param cmsMeeting 用户信息
     * @return 令牌
     */
    public String createToken(CmsMeeting cmsMeeting) {
        String token = IdUtils.fastUUID();
        cmsMeeting.setToken(token);
        refreshToken(cmsMeeting);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.CMS_MEETING_KEY, token);
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param cmsMeeting
     * @return 令牌
     */
    public void verifyToken(CmsMeeting cmsMeeting) {
        long expireTime = cmsMeeting.getExpireTimer();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(cmsMeeting);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param cmsMeeting 登录信息
     */
    public void refreshToken(CmsMeeting cmsMeeting) {
        cmsMeeting.setCreateTime(DateUtils.getNowDate());
        // 根据uuid将cmsMeeting缓存
        String meetingKey = getTokenKey(cmsMeeting.getToken());
        redisCache.setCacheObject(meetingKey, JSON.toJSONString(cmsMeeting), expireTime, TimeUnit.MINUTES);
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims) {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, "secret").compact(); // 使用硬编码的密钥
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey("secret") // 使用硬编码的密钥
                .parseClaimsJws(token)
                .getBody();
    }

    public String getTokenKey(String uuid) {
        return CacheConstants.QRCODE_TOKEN_KEY + uuid;
    }
}
