package controllers;

import static play.data.Form.form;
import static play.libs.Jsonp.jsonp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import model.TestDataForm;
import model.TestGroup;
import model.Variant;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;

import play.Play;
import play.data.Form;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class Application extends Controller {
    private final static Form<TestDataForm> testDataForm = form(TestDataForm.class);
    private static TestDataForm testData;
    private static final Random random = new Random();
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

    /**
     * The main functionality that sets the cookie in a response.
     * 
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static Result index(String callback) throws JsonGenerationException, JsonMappingException, IOException {
        if (testData == null) {
            Promise<TestDataForm> promiseOfTestData = getPromiseOfTestData();
            Application.testData = promiseOfTestData.get();
        }
        return generateRandomVariantResult(testData, callback);
    }

    /**
     * Returns all tests and variants.
     * 
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static Result getTests(String callback) throws JsonGenerationException, JsonMappingException, IOException {
        if (testData == null) {
            Promise<TestDataForm> promiseOfTestData = getPromiseOfTestData();
            Application.testData = promiseOfTestData.get();
        }
        return allTestsResults(testData, callback);
    }

    /**
     * Post Tests
     * 
     * @return
     */
    public static Result testsCreateUpdate() {
        Form<TestDataForm> form = testDataForm.bindFromRequest();
        if (form.hasErrors()) {
            System.out.println(form.errorsAsJson().toString());
        }
        Application.testData = form.get();
        Promise<Result> promiseOfStore = getPromiseOfStoreTestData();
        return async(promiseOfStore);
    }

    public static Result seedTestData() {
        List<TestGroup> testGroupList = new ArrayList<TestGroup>();
        Map<String, String> variantMap = new HashMap<String, String>();
        variantMap.put("Experience A: Default", "70");
        variantMap.put("Experience B: Alternate", "30");
        TestGroup testGroup = new TestGroup("Header Flyouts Test", variantMap);
        testGroupList.add(testGroup);
        Application.testData = new TestDataForm(testGroupList);
        Promise<Result> promiseOfStore = getPromiseOfStoreTestData();
        return async(promiseOfStore);
    }

    /*
     * ---------------------------------------------- HELPER METHODS
     * ------------------------------------------
     */
    private static Result allTestsResults(TestDataForm testData, String callback) throws JsonGenerationException,
            JsonMappingException, IOException {
        Json json = new Json();
        JsonNode jsonNode = json.toJson(testData);
        return ok(jsonp(callback, jsonNode)).as("application/json");
    }

    private static Result generateRandomVariantResult(TestDataForm testData, String callback) throws IOException,
            JsonGenerationException, JsonMappingException {
        List<String> randomVariants = new ArrayList<String>();
        for (TestGroup testGroup : testData.getTestGroupList()) {
            randomVariants.add(randomVariant(testGroup.getVariantList()));
        }
        Json json = new Json();
        JsonNode jsonNode = json.toJson(randomVariants);
        String domain = Play.application().configuration().getString("application.defaultCookieDomain");
        if (domain != null && !domain.isEmpty()) {
            response().setCookie("Context", jsonNode.toString(), 1209600, "/", domain);
        } else {
            response().setCookie("Context", jsonNode.toString(), 1209600, "/");
        }

        return ok(jsonp(callback, jsonNode)).as("application/json");
    }

    private static Promise<Result> getPromiseOfStoreTestData() {
        return play.libs.Akka.future(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
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
                return ok();
            }
        });
    }

    private static Promise<TestDataForm> getPromiseOfTestData() {
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

    private static String randomVariant(List<Variant> variantList) {
        Double randomDouble = random.nextDouble() * 100;
        for (Variant variant : variantList) {
            randomDouble -= variant.getWeight();
            if (randomDouble <= 0.0d) {
                return variant.getVariantName();
            }
        }
        return null;
    }
}
