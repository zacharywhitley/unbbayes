package unbbayes.prs.mebn.table;

import java.util.ArrayList;
import java.util.List;

public class TableFunction {

	private List<IfClause> listIfClause = new ArrayList<IfClause>();
	
	public void addIfClause(IfClause ifClause) {
		listIfClause.add(ifClause);
	}

}
