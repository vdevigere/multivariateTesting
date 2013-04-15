package model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestGroup {

	private String name;
	
	private Set<VariantsAndWeight> variantsAndWeight = new HashSet<VariantsAndWeight>();

	public TestGroup(String testGroupName, Map<String, String> variantMap) {
		this.name = testGroupName;
		for (String variantName : variantMap.keySet()) {
			variantsAndWeight.add(new VariantsAndWeight(variantName,
					new Double(variantMap.get(variantName))));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<VariantsAndWeight> getVariantsAndWeight() {
		return variantsAndWeight;
	}

	public void setVariantsAndWeight(Set<VariantsAndWeight> variantsAndWeight) {
		this.variantsAndWeight = variantsAndWeight;
	}
}
