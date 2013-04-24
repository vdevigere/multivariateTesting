package controllers;

import static play.data.Form.form;
import static play.libs.Jsonp.jsonp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.TestDataForm;
import model.TestGroup;
import model.Variant;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;

import play.data.Form;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
    private static TestDataForm testData;
    private static final Random random = new Random();
    private static Boolean updateCache = true;

    /**
     * The main functionality that sets the cookie in a response.
     * 
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static Result index(final String callback) throws JsonGenerationException, JsonMappingException, IOException {
        if (testData == null || updateCache == true) {
            Promise<TestDataForm> promiseOfTestData = RedisPersister.getPromiseOfTestData();
            return async(promiseOfTestData.map(new Function<TestDataForm, Result>() {

                @Override
                public Result apply(TestDataForm testData) throws Throwable {
                    Application.testData = testData;
                    Application.updateCache = false;
                    return generateRandomVariantResult(testData, callback);
                }
            }));
        } else {
            return generateRandomVariantResult(testData, callback);
        }

    }

    /**
     * Returns all tests and variants.
     * 
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static Result allTests(final String callback) throws JsonGenerationException, JsonMappingException,
            IOException {
        Promise<TestDataForm> promiseOfTestData = RedisPersister.getPromiseOfTestData();
        return async(promiseOfTestData.map(new Function<TestDataForm, Result>() {

            @Override
            public Result apply(TestDataForm testData) throws Throwable {
                Json json = new Json();
                JsonNode jsonNode = json.toJson(testData);
                if (callback != null) {
                    return ok(jsonp(callback, jsonNode)).as("application/json");
                }else{
                    return ok(jsonNode).as("application/json");
                }
            }
        }));
    }

    /**
     * Post Tests
     * 
     * @return
     */
    public static Result saveTestData() {
        Form<TestDataForm> testDataForm = form(TestDataForm.class).bindFromRequest();
        Promise<Boolean> promiseOfStore = RedisPersister.getPromiseOfStoreTestData(testDataForm.get());
        return async(promiseOfStore.map(new Function<Boolean, Result>() {

            @Override
            public Result apply(Boolean arg0) throws Throwable {
                Application.updateCache = true;
                return redirect("/gui/test.html");
            }
        }));
    }

    private static Result generateRandomVariantResult(TestDataForm testData, String callback) throws IOException,
            JsonGenerationException, JsonMappingException {
        Map<String, Map<String, String>> returnJson = new HashMap<String, Map<String, String>>();
        Map<String, String> randomVariants = new HashMap<String, String>();
        for (TestGroup testGroup : testData.getTestGroupList()) {
            randomVariants.put(testGroup.getTestName(), randomVariant(testGroup.getVariantList()));
        }
        returnJson.put("EXPERIMENT", randomVariants);
        Json json = new Json();
        JsonNode jsonNode = json.toJson(returnJson);
        if (callback != null) {
            return ok(jsonp(callback, jsonNode)).as("application/json");
        } else {
            return ok(jsonNode).as("application/json");
        }
    }

    private static String randomVariant(List<Variant> variantList) {
        Double sumOfWeights = 0.0;
        for (Variant variant : variantList) {
            sumOfWeights += variant.getWeight();
        }
        Double randomDouble = random.nextDouble() * sumOfWeights;
        for (Variant variant : variantList) {
            randomDouble -= variant.getWeight();
            if (randomDouble <= 0.0d) {
                return variant.getVariantName();
            }
        }
        return null;
    }
}
