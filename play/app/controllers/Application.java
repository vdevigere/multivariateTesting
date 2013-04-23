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
   // private static TestDataForm testData;
    private static final Random random = new Random();

    /**
     * The main functionality that sets the cookie in a response.
     * 
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static Result index(final String callback) throws JsonGenerationException, JsonMappingException, IOException {
          Promise<TestDataForm> promiseOfTestData = RedisPersister.getPromiseOfTestData();
          return async(promiseOfTestData.map(new Function<TestDataForm, Result>() {

              @Override
              public Result apply(TestDataForm testData) throws Throwable {
                  return generateRandomVariantResult(testData,callback);
              }
          }));
          
    }

    /**
     * Returns all tests and variants.
     * 
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static Result allTests(final String callback) throws JsonGenerationException, JsonMappingException, IOException {
        Promise<TestDataForm> promiseOfTestData = RedisPersister.getPromiseOfTestData();
        return async(promiseOfTestData.map(new Function<TestDataForm, Result>() {

            @Override
            public Result apply(TestDataForm testData) throws Throwable {
                Json json = new Json();
                JsonNode jsonNode = json.toJson(testData);
                return ok(jsonp(callback, jsonNode)).as("application/json");
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
        return redirect("/gui/test.html");
    }

    private static Result generateRandomVariantResult(TestDataForm testData, String callback) throws IOException,
            JsonGenerationException, JsonMappingException {
        Map<String, String> randomVariants = new HashMap<String, String>();
        for (TestGroup testGroup : testData.getTestGroupList()) {
            randomVariants.put(testGroup.getTestName(), randomVariant(testGroup.getVariantList()));
        }
        Json json = new Json();
        JsonNode jsonNode = json.toJson(randomVariants);
        return ok(jsonp(callback, jsonNode)).as("application/json");
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
