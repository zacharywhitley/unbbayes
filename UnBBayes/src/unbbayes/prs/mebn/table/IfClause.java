package unbbayes.prs.mebn.table;

import java.util.ArrayList;
import java.util.List;

public class IfClause {

	private List<BooleanFunction> listBooleanFunction = new ArrayList<BooleanFunction>();
	private List<LogicOperator> listLogicOperator = new ArrayList<LogicOperator>();
	private IfOperator ifOperator;
	
	/** 
	 * The name of the set of parameters that return true to this if clause.
	 */
	private String ifParameterSetName;
	
	private ProbabilityFunction probabilityFunciton;
	
	public void addBooleanFunction(BooleanFunction booleanFunction) {
		listBooleanFunction.add(booleanFunction);
	}
	
	public void addLogicOperator(LogicOperator logicOperator) {
		listLogicOperator.add(logicOperator);
	}
	
	public void setIfOperator(IfOperator ifOperator) {
		this.ifOperator = ifOperator;
	}
	
	public IfOperator getIfOperator() {
		return ifOperator;
	}
	
	public String getIfParameterSetName() {
		return ifParameterSetName;
	}
	
	public void setIfParameterSetName(String ifParameterSetName) {
		this.ifParameterSetName = ifParameterSetName;
	}

	public ProbabilityFunction getProbabilityFunciton() {
		return probabilityFunciton;
	}

	public void setProbabilityFunciton(ProbabilityFunction probabilityFunciton) {
		this.probabilityFunciton = probabilityFunciton;
	}

}
