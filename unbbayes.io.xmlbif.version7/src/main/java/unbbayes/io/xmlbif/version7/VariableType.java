package unbbayes.io.xmlbif.version7;

public enum VariableType {
	
	DISCRETEPROBABILISTIC("DiscreteProbabilistic"),
	CONTINUOUSPROBABILISTIC("ContinuousProbabilistic"),
	DECISION("Decision"),
	UTILITY("Utility");
	
	private String name;
	
	VariableType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

}
