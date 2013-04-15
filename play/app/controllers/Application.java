package controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import model.TestGroup;
import play.data.Form;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import com.google.gson.Gson;

public class Application extends Controller {
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
	 * @return
	 */
	public static Result index() {
		Promise<Map<String, Map<String, String>>> promiseOfTestData = getPromiseOfTestData();
		Promise<Result> promiseOfResult = promiseOfTestData
				.map(new Function<Map<String, Map<String, String>>, Result>() {
					@Override
					public Result apply(Map<String, Map<String, String>> testData) throws Throwable {
						List<String> randomVariants = new ArrayList<String>();
						for (String testGroup : testData.keySet()) {
							randomVariants.add(randomVariant(testData.get(testGroup)));
						}
						Gson gson = new Gson();
						return ok(gson.toJson(randomVariants));
					}
				});
		return async(promiseOfResult);
	}

	public static Result getTests() {
		Promise<Map<String, Map<String, String>>> promiseOfTestData = getPromiseOfTestData();
		Promise<Result> promiseOfResult = promiseOfTestData
				.map(new Function<Map<String, Map<String, String>>, Result>() {
					Set<TestGroup> testGroups = new HashSet<TestGroup>();
					@Override
					public Result apply(Map<String, Map<String, String>> testData) throws Throwable {
						for(String testGroupName : testData.keySet()){
							TestGroup testGroup = new TestGroup(testGroupName,testData.get(testGroupName));
							testGroups.add(testGroup);
						}
						Gson gson = new Gson();
						return ok(gson.toJson(testGroups));
					}
				});
		return async(promiseOfResult);

	}
	
	public static Result testsCreateUpdate(){
		Form<Map> input = Form.form(Map.class).bindFromRequest();
		System.out.println(input);
		return ok();
	}
	
	
	private static Promise<Map<String, Map<String, String>>> getPromiseOfTestData() {
		return play.libs.Akka.future(new Callable<Map<String, Map<String, String>>>() {

			@Override
			public Map<String, Map<String, String>> call() throws Exception {
				Jedis jedis = pool.getResource();
				Map<String, Map<String, String>> testData = new HashMap<String, Map<String, String>>();
				try {
					Set<String> testGroups = jedis.smembers("testgroups");
					for (String testGroup : testGroups) {
						testData.put(testGroup, jedis.hgetAll(testGroup));
					}

					return testData;
				} finally {
					pool.returnResource(jedis);
				}
			}
		});
	}
	
	protected static String randomVariant(Map<String, String> variantMap) {
		double random = Math.random() * 100;
		for (String variant : variantMap.keySet()) {
			random -= new Double(variantMap.get(variant));
			if (random <= 0.0d) {
				return variant;
			}
		}
		return null;
	}
}
