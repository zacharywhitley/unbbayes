package unbbayes.model.umpst.implementation;

import java.util.ArrayList;

/**
 * This class function as a intermediate node to pointer an
 * event and its arguments that will be present in necessary condition formula.
 * 
 * @author Diego Marques
 */
public class EventNCPointer {
	private NodeFormulaTree nodePlace;	
	private NecessaryConditionVariableModel ncVariableModel;
	private ArrayList<OrdinaryVariableModel> ovArgumentList;
	private EventVariableObjectModel eventVariable;
	
	public EventNCPointer(NodeFormulaTree nodePlace, NecessaryConditionVariableModel ncVariableModel,
			EventVariableObjectModel eventVariable) {
		this.nodePlace = nodePlace;
		this.ncVariableModel = ncVariableModel;
		this.eventVariable = eventVariable;
		
		ovArgumentList = new ArrayList<OrdinaryVariableModel>();		
	}
	
	public void removeOVArgumentList() {
		ovArgumentList = new ArrayList<OrdinaryVariableModel>();
	}
	
	/**
	 * @return the nodePlace
	 */
	public NodeFormulaTree getNodePlace() {
		return nodePlace;
	}
	/**
	 * @param nodePlace the nodePlace to set
	 */
	public void setNodePlace(NodeFormulaTree nodePlace) {
		this.nodePlace = nodePlace;
	}
	/**
	 * @return the ovArgumentList
	 */
	public ArrayList<OrdinaryVariableModel> getOvArgumentList() {
		return ovArgumentList;
	}
	/**
	 * @param ovArgumentList the ovArgumentList to set
	 */
	public void setOvArgumentList(ArrayList<OrdinaryVariableModel> ovArgumentList) {
		this.ovArgumentList = ovArgumentList;
	}
	/**
	 * @return the eventVariable
	 */
	public EventVariableObjectModel getEventVariable() {
		return eventVariable;
	}
	/**
	 * @param eventVariable the eventVariable to set
	 */
	public void setEventVariable(EventVariableObjectModel eventVariable) {
		this.eventVariable = eventVariable;
	}
}
