package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestGroup {

    private String testName;

    private List<Variant> variantList;

    public TestGroup() {
    }

    public TestGroup(String testName, Map<String, String> variantMap) {
        this.testName = testName;
        variantList = new ArrayList<Variant>();
        for (String variantName : variantMap.keySet()) {
            variantList.add(new Variant(variantName, new Double(variantMap.get(variantName))));
        }
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public List<Variant> getVariantList() {
        return variantList;
    }

    public void setVariantList(List<Variant> variantList) {
        this.variantList = variantList;
    }
}
