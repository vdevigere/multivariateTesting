package model;

public class Variant {

    private String variantName;

    private Double weight;

    public Variant() {
        // TODO Auto-generated constructor stub
    }
    
    public Variant(String variantName, Double weight) {
        this.variantName = variantName;
        this.weight = weight;
    }

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
