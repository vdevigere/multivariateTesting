package controllers;

import static play.data.Form.form;
import static play.libs.Jsonp.jsonp;

import java.io.IOException;
import java.util.ArrayList;
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
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
    private final static Form<TestDataForm> testDataForm = form(TestDataForm.class);
    private static TestDataForm testData;
    private static final Random random = new Random();


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
            Promise<TestDataForm> promiseOfTestData = RedisPersister.getPromiseOfTestData();
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
    public static Result allTests(String callback) throws JsonGenerationException, JsonMappingException, IOException {
        if (testData == null) {
            Promise<TestDataForm> promiseOfTestData = RedisPersister.getPromiseOfTestData();
            Application.testData = promiseOfTestData.get();
        }
        Json json = new Json();
        JsonNode jsonNode = json.toJson(testData);
        return ok(jsonp(callback, jsonNode)).as("application/json");
    }

    /**
     * Post Tests
     * 
     * @return
     */
    public static Result saveTestData() {
        Form<TestDataForm> form = testDataForm.bindFromRequest();
        if (form.hasErrors()) {
            System.out.println(form.errorsAsJson().toString());
        }
        Application.testData = form.get();
        Promise<Boolean> promiseOfStore = RedisPersister.getPromiseOfStoreTestData(testData);
        return ok(promiseOfStore.get().toString());
    }

    public static Result seedTestData() {
        List<TestGroup> testGroupList = new ArrayList<TestGroup>();
        Map<String, String> variantMap = new HashMap<String, String>();
        variantMap.put("Experience A: Default", "70");
        variantMap.put("Experience B: Alternate", "30");
        TestGroup testGroup = new TestGroup("Header Flyouts Test", variantMap);
        testGroupList.add(testGroup);
        Application.testData = new TestDataForm(testGroupList);
        Promise<Boolean> promiseOfStore = RedisPersister.getPromiseOfStoreTestData(testData);
        return ok(promiseOfStore.get().toString());
    }

    private static Result generateRandomVariantResult(TestDataForm testData, String callback) throws IOException,
            JsonGenerationException, JsonMappingException {
        Map<String, String> randomVariants = new HashMap<String, String>();
        for (TestGroup testGroup : testData.getTestGroupList()) {
            randomVariants.put(testGroup.getTestName(),randomVariant(testGroup.getVariantList()));
        }
        Json json = new Json();
        JsonNode jsonNode = json.toJson(randomVariants);
        return ok(jsonp(callback, jsonNode)).as("application/json");
    }
    
    private static String randomVariant(List<Variant> variantList) {
        Double sumOfWeights = 0.0;
        for(Variant variant : variantList){
            sumOfWeights  += variant.getWeight();
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
