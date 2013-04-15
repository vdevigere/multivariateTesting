package model;

public class VariantsAndWeight {
	public VariantsAndWeight(String variantName, Double weight) {
		this.variantName = variantName;
		this.weight = weight;
	}

	private String variantName;
	
	private Double weight;

	public String getVariantName() {
		return variantName;
	}

	public void setVariantName(String variantName) {
		this.variantName = variantName;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
}
