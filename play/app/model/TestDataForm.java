package model;

import java.util.List;

public class TestDataForm {

    public TestDataForm() {
        // TODO Auto-generated constructor stub
    }

    public TestDataForm(List<TestGroup> testGroupList) {
        this.testGroupList = testGroupList;
    }

    private List<TestGroup> testGroupList;

    public List<TestGroup> getTestGroupList() {
        return testGroupList;
    }

    public void setTestGroupList(List<TestGroup> testGroupList) {
        this.testGroupList = testGroupList;
    }

}
