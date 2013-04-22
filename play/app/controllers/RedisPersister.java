package controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import model.TestDataForm;
import model.TestGroup;
import model.Variant;
import play.libs.F.Promise;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class RedisPersister {
    private static JedisPool pool;
    static {
        String sURL = System.getenv("REDISTOGO_URL");
        try {
            if (sURL != null) {
                URI redisURI = new URI(sURL);
                pool = new JedisPool(new JedisPoolConfig(), redisURI.getHost(), redisURI.getPort(),
                        Protocol.DEFAULT_TIMEOUT, redisURI.getUserInfo().split(":", 2)[1]);
            } else {
                pool = new JedisPool(new JedisPoolConfig(), "127.0.0.1");
            }
        } catch (URISyntaxException e) {
        }
    }
    
    public static Promise<Boolean> getPromiseOfStoreTestData(final TestDataForm testData) {
        return play.libs.Akka.future(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Jedis jedis = pool.getResource();
                try {
                    jedis.del("testgroups");
                    for (TestGroup testGroup : testData.getTestGroupList()) {
                        jedis.sadd("testgroups", testGroup.getTestName());
                        for (Variant variant : testGroup.getVariantList()) {
                            jedis.hset(testGroup.getTestName(), variant.getVariantName(), variant.getWeight()
                                    .toString());
                        }
                    }
                } finally {
                    pool.returnResource(jedis);
                }
                return true;
            }
        });
    }

    public static Promise<TestDataForm> getPromiseOfTestData() {
        return play.libs.Akka.future(new Callable<TestDataForm>() {

            @Override
            public TestDataForm call() throws Exception {
                Jedis jedis = pool.getResource();
                List<TestGroup> testGroupList = new ArrayList<TestGroup>();
                try {
                    Set<String> testGroups = jedis.smembers("testgroups");
                    for (String sTestGroup : testGroups) {
                        Map<String, String> variantList = jedis.hgetAll(sTestGroup);
                        TestGroup testGroup = new TestGroup(sTestGroup, variantList);
                        testGroupList.add(testGroup);
                    }

                    TestDataForm testDataForm = new TestDataForm(testGroupList);
                    return testDataForm;
                } finally {
                    pool.returnResource(jedis);
                }
            }
        });
    }
}
