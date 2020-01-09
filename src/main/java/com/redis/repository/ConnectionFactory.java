package com.redis.repository;

import java.util.HashSet;
import java.util.Set;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

public class ConnectionFactory {

    private static final JedisCluster jedis;

    static {
        Set<HostAndPort> jedisClusterNode = new HashSet<>();
        jedisClusterNode.add(new HostAndPort("localhost", 7000));
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(16);
        cfg.setMaxIdle(8);
        cfg.setMaxWaitMillis(10000);
        cfg.setTestOnBorrow(true);
        jedis = new JedisCluster(jedisClusterNode, 10000, 1, cfg);
    }

    public static JedisCluster getJedis() {
        return jedis;

    }
}
