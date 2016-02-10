package unbbayes.io.umpst.implementation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.implementation.BuiltInRV;
import unbbayes.model.umpst.implementation.EnumSubType;
import unbbayes.model.umpst.implementation.EnumType;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.NodeFormulaTree;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVAnd;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVEqualTo;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVExists;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVForAll;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVIff;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVImplies;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVNot;
import unbbayes.model.umpst.implementation.builtInRV.BuiltInRVOr;

/**
 * Builder of node properties
 * 
 * @author Diego Marques
 */
public class FileBuildImplementationNode {
	
	
	public Element buildNodeName (Document doc, NodeFormulaTree fatherNode) {		
		Element ncNodeName = doc.createElement("ncNodeName");			
		ncNodeName.appendChild(doc.createTextNode(fatherNode.getName()));
		return ncNodeName;
	}

	public Element buildNodeMnemonic (Document doc, NodeFormulaTree fatherNode) {
		Element ncNodeMnemonic = doc.createElement("ncNodeMnemonic");			
		ncNodeMnemonic.appendChild(doc.createTextNode(fatherNode.getMnemonic()));
		return ncNodeMnemonic;
	}
	
	public Element buildSentenceNodeVariable (Document doc, NodeFormulaTree fatherNode) {
		Element ncNodeVariable = doc.createElement("ncNodeVariable");
		ncNodeVariable.appendChild(doc.createTextNode("BuiltInRV"));
		return ncNodeVariable;
	}
	
	public Element buildSentenceNodeVariableOperands (Document doc, NodeFormulaTree fatherNode) {
		int num = ((BuiltInRV)fatherNode.getNodeVariable()).getNumOperandos();
		String numOp = Integer.toString(num);
		
		Element ncNodeVariableNumOperands = doc.createElement("ncNodeVariableNumOperands");
		ncNodeVariableNumOperands.appendChild(doc.createTextNode(numOp));
		return ncNodeVariableNumOperands;
	}
	
	public Element buildTypeNode (Document doc, NodeFormulaTree fatherNode) {
		Element ncNodeTypeNode = doc.createElement("ncNodeTypeNode");				
		ncNodeTypeNode.appendChild(doc.createTextNode(((EnumType)fatherNode.getTypeNode()).name()));
		return ncNodeTypeNode;
	}
	
	public Element buildSubTypeNode (Document doc, NodeFormulaTree fatherNode) {		
		Element ncNodeSubTypeNode = doc.createElement("ncNodeSubTypeNode");			
		ncNodeSubTypeNode.appendChild(doc.createTextNode(((EnumSubType)fatherNode.getSubTypeNode()).name()));
		return ncNodeSubTypeNode;
	}	
	
	public Element buildUnitNodeVariable (Document doc, NodeFormulaTree fatherNode) {
		
		Element ncNodeVariable = doc.createElement("ncNodeVariable");		
		if (fatherNode.getNodeVariable().getClass().equals(OrdinaryVariableModel.class)) {
			// OV
			Element ncNodeVariableOV = doc.createElement("ncNodeVariableOV");
			String ov = ((OrdinaryVariableModel)fatherNode.getNodeVariable()).getTypeEntity();
			ncNodeVariableOV.appendChild(doc.createTextNode(ov));
			ncNodeVariable.appendChild(ncNodeVariableOV);
			
		} else if (fatherNode.getNodeVariable().getClass().equals(EventNCPointer.class)) {
			// Relationship
			Element ncNodeVariableEventId = doc.createElement("ncNodeVariableEventId");
			String eventId = ((EventNCPointer)fatherNode.getNodeVariable()).
					getEventVariable().getId();				
			ncNodeVariableEventId.appendChild(doc.createTextNode(eventId));
			ncNodeVariable.appendChild(ncNodeVariableEventId);
			
			// EventType
			Element ncNodeVariableEventType = doc.createElement("ncNodeVariableEventType");
			String eventType = ((EventNCPointer)fatherNode.getNodeVariable()).
					getEventVariable().getTypeEvent().name();			
			ncNodeVariableEventType.appendChild(doc.createTextNode(eventType));
			ncNodeVariable.appendChild(ncNodeVariableEventType);
			
			if (((EventNCPointer)fatherNode.getNodeVariable()).getOvArgumentList().size() > 0) {				
				// Arguments
				Element ncNodeVariableEventOVList = doc.createElement("ncNodeVariableEventOVList");
				ncNodeVariable.appendChild(ncNodeVariableEventOVList);
				
				int numArg = ((EventNCPointer)fatherNode.getNodeVariable()).getOvArgumentList().size();					
				for (int i = 0; i < numArg; i++) {
					
					Element ncNodeVariableEventOVId = doc.createElement("ncNodeVariableEventOVId");
					String ovId = ((EventNCPointer)fatherNode.getNodeVariable()).getOvArgumentList().
							get(i).getId();
					ncNodeVariableEventOVId.appendChild(doc.createTextNode(ovId));
					ncNodeVariableEventOVList.appendChild(ncNodeVariableEventOVId);
					
				}
			}
		}
		return ncNodeVariable;
	}		
}
