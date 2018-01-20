package common.wechat.cache;

import common.tools.StringUtil;
import common.wechat.WeChatPropertiesFile;
import common.wechat.util.ConfigBuilder;
import redis.clients.jedis.Jedis;

import java.util.Properties;

/**
 * @author LongerHsiao
 * @date 2017-12-14
 */
public final class CacheManager {
    private CacheManager() {
        init();
    }

    private static class CacheManagerHolder {
        private static final CacheManager INSTANCE = new CacheManager();
    }

    public static CacheManager getInstance() {
        return CacheManagerHolder.INSTANCE;
    }

    public boolean cacheOn = false;

    private Jedis jedis;

    private void init() {
        Properties config = ConfigBuilder.getInstance().init(WeChatPropertiesFile.filename);
        if (config == null || config.isEmpty()) {
            return;
        }
        String host = config.getProperty("cacheHost");
        String port = config.getProperty("cachePort");
        if (StringUtil.isEmpty(host)) {
            return;
        }

        if (StringUtil.isEmpty(port)) {
            jedis = new Jedis(host);
        } else {
            jedis = new Jedis(host, Integer.parseInt(port));
        }
        cacheOn = true;
    }

    private Jedis getJedis() {
        return jedis;
    }

    public String getValue(String key) {
        if (getJedis() == null) {
            return null;
        }
        return getJedis().get(key);
    }

    public void setValue(String key, String value) {
        setValue(key, value, 3600);
    }

    public void setValue(String key, String value, int secondsToLive) {
        if (getJedis() != null) {
            getJedis().setex(key, secondsToLive, value);
        }
    }
}
