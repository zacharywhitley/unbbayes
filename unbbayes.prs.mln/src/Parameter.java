public class Parameter {

	private String attribute = "";
	private String description = "";
	private VariableType variableType;
	private String defaultValue = "";

	public enum VariableType {
		String,
		Integer,
		Boolean,
		Float
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public VariableType getVariableType() {
		return variableType;
	}
	public void setVariableType(VariableType variableType) {
		this.variableType = variableType;
	}
	
}
