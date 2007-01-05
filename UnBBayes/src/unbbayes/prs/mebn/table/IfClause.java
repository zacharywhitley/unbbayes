package unbbayes.prs.mebn.table;

import java.util.ArrayList;
import java.util.List;

public class IfClause {

	private List<BooleanFunction> listBooleanFunction = new ArrayList<BooleanFunction>();
	private List<LogicOperator> listLogicOperator = new ArrayList<LogicOperator>();
	private IfOperator ifOperator;
	
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

}
