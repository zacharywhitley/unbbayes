package unbbayes.cps.gui;

import unbbayes.controller.NetworkController;
import unbbayes.cps.datastructure.CPSNode;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.ArrayMap;

public class CPSController extends CPSDialog{
  
	/**
	 * 
	 */
	private static final long serialVersionUID = -4391630180742616099L;
	
	NetworkController controller = null;
	Node curNode = null;
	
	/**
	 * This constructor was added to the original class because it looked like
	 * the controller was doing nothing...
	 * TODO check needs.
	 * @param n
	 */
	public CPSController(Node n){
		this(null, n);
	}
	
	public CPSController(NetworkController c, Node n){
		super();  
		
		
		controller = c;
		curNode = n;
		 
		// add MathNode information from UnBBayes node information
		// add current selected node without states
		math.addNode(n.getName());
		
		
		// add its parents nodes with states		
		for (Node p : curNode.getParents()) {
			if (p.getType() ==  Node.PROBABILISTIC_NODE_TYPE) {
				CPSNode un = math.addNode(p.getName()); 
				for( Integer i = 0; i < p.getStatesSize(); i++)
					math.addState(un,  p.getName(), i.toString(), p.getStateAt(i), i.toString(), "0"); //<- i.toString() this has to be changed to stateNumber
				
			} else if (p.getType() == Node.CONTINUOUS_NODE_TYPE) {
				CPSNode un = math.addNode(p.getName()); 
				for( Integer i = 0; i < p.getStatesSize(); i++)
					math.addState(un, p.getName(), i.toString(),  p.getStateAt(i), i.toString(), "0");
			}
		}
		 
		Init();
		
		//Test!!!
		if( n.getName().equals("Safety") )
			setScript("node.Safety=node.SafetyPartial+node.SafetyPartial1;");
		else if( n.getName().equals("FieldGoal") )
			setScript("node.FieldGoal=node.FieldGoalPartial+node.FieldGoalPartial1;"); 
		else if( n.getName().equals("S1") )
			setScript("node.S1=2*node.Safety+3*node.FieldGoal;"); 
		else if( n.getName().equals("TouchdownNoExtra") )
			setScript("node.TouchdownNoExtra=node.TouchdownNoExtraPartial+node.TouchdownNoExtraPartial1;"); 
		else if( n.getName().equals("S2") )
			setScript("node.S2=node.S1+6*node.TouchdownNoExtra;"); 
		else if( n.getName().equals("TouchdownConversion") )
			setScript("node.TouchdownConversion=node.TouchdownConversionPartial+node.TouchdownConversionPartial1;"); 
		else if( n.getName().equals("S3") )
			setScript("node.S3=node.S2+7*node.TouchdownConversion;"); 
		else if( n.getName().equals("TouchdownExtra") )
			setScript("node.TouchdownExtra=node.TouchdownExtraPartial+node.TouchdownExtraPartial1;"); 
		else if( n.getName().equals("FinalScore") )
			setScript("node.FinalScore=node.S3+8*node.TouchdownExtra;"); 
		    
	}

	public void onCompiled(String error)
	{
		if( error.equals("ok") ){
			
			CPSNode node = math.getNode(curNode.getName());
			
			// add UnBBayes node information from MathNode information 
			((ProbabilisticNode)curNode).removeAllStates();
						
			// add new states of current node
			ArrayMap<Integer, Integer> stateIndexMap = new ArrayMap<Integer, Integer>();
			for( int i = 0, c = 0; i < node.getNumberOfChildren(); i++ ){
				CPSNode state = node.getChild(i);		
				if (i == 0 && c == 0) {
					curNode.setStateAt(state.getNumber(), 0);
					stateIndexMap.put((int) Double.parseDouble( state.getNumber() ), c++);
				} else if( !curNode.hasState(state.getNumber())){	
					curNode.appendState(state.getNumber());
					stateIndexMap.put((int) Double.parseDouble( state.getNumber() ), c++);
				} 
			}
			
			// fill zero on table
			PotentialTable auxTab = (PotentialTable) ((IRandomVariable) curNode).getProbabilityFunction();
			int curStateSize = curNode.getStatesSize();  
			
			for (INode parent : curNode.getParentNodes()) 
				curStateSize *= parent.getStatesSize();
			
			for( int i = 0; i < curStateSize; i++ )
				auxTab.addValueAt(i, 0);
			
			auxTab.setTableSize(curStateSize);
			
			int coord[] = new int[curNode.getParentNodes().size()+1];
			      
			// add data on table
			for( int i = 0; i < node.getNumberOfChildren(); i++ )
			{				
				// Example)
				// 		C2	     state 0		state 1	
				// 		C1	state 0	state 1	state 0	state 1
				// 		0	1		0		0		0
				// C0	1	0		1		1		0
				// 		2	0		0		0		1

				// uc is C0 
				CPSNode state = node.getChild(i);
				
				// get state index of C0 and put it on coord[0]
				
				String s =  state.getNumber();
				int i1 = (int) Double.parseDouble( s );
				coord[0] = stateIndexMap.get(i1);
				//coord[0] = stateIndexMap.get((int) Double.parseDouble( state.getNumber() ));
				
				// put children coord[1~]
				if( state.getChild(0) != null )
					assignVariableCoord(auxTab, state.getChild(0), coord);
				 
				// set 1
				auxTab.setValue(coord, 1); 
			} 
			
		}
		
		setVisible(false); 
	}
	
	public void onClose(){
		setVisible(false);
	 }
	
	/*
	 *   put children coord[1~]
	 */
	public void assignVariableCoord(PotentialTable auxTab, CPSNode state, int coord[])
	{
		int variableIndex = auxTab.indexOfVariable( state.getOrigin() );
		coord[variableIndex] = Integer.parseInt( state.getIndex() );
		
		if( state.getChild(0) != null )
			assignVariableCoord( auxTab, state.getChild(0), coord );
	}
	
}
