package unbbayes.io.umpst.intermediatemtheory;

import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.implementation.algorithm.MFragModel;
import unbbayes.model.umpst.implementation.node.NodeObjectModel;

/**
 * Instance and set node properties with argument values
 * 
 * @author Diego Marques
 */
public class FileBuildIMMFragNode {
	
	private String nodes = "nodes";
	private String contextNode = "contextNode";
	private String inputNode = "inputNode";
	private String residentNode = "residentNode";
	private String notDefinedNode = "notDefinedNode";
	private Element nodeFather = null;
	
	protected Element buildMFrag(Document doc, Element parent, MFragModel mfragTest) {
				
		Element rootNode = doc.createElement("mfrag");
		parent.appendChild(rootNode);
		
		Element mfrag = doc.createElement(nodes);
		mfrag.setAttribute("id", mfragTest.getId());
		mfrag.setAttribute("name", mfragTest.getName());
		rootNode.appendChild(mfrag);
		
		// CONTEXT NODE
		for (int j = 0; j < mfragTest.getNodeContextList().size(); j++) {
			String id = mfragTest.getNodeContextList().get(j).getId();
			String name = mfragTest.getNodeContextList().get(j).getName();
			List<NodeObjectModel> fatherList = mfragTest.getNodeContextList().get(j).getFatherNode();
			
			Element context = doc.createElement(contextNode);
			context.setAttribute("id", id);
			context.setAttribute("name", name);
			
			if (fatherList.size() > 0) {
				String fathersId = "";
				for (int i = 0; i < fatherList.size(); i++) {
					fathersId = fathersId + fatherList.get(i).getId() + ", "; 
				}
				int index = fathersId.lastIndexOf(", ");
				String ids = fathersId.substring(0, index);
				context.setAttribute("fathersId", ids);		
			}
			mfrag.appendChild(context);
		}
		
		// INPUT NODE
		for (int j = 0; j < mfragTest.getNodeInputList().size(); j++) {
			String id = mfragTest.getNodeInputList().get(j).getId();
			String name = mfragTest.getNodeInputList().get(j).getName();
			List<NodeObjectModel> fatherList = mfragTest.getNodeInputList().get(j).getFatherNode();
			
			Element input = doc.createElement(inputNode);
			input.setAttribute("id", id);
			input.setAttribute("name", name);
			
			if (fatherList.size() > 0) {
				String fathersId = "";
				for (int i = 0; i < fatherList.size(); i++) {
					fathersId = fathersId + fatherList.get(i).getId() + ", "; 
				}
				int index = fathersId.lastIndexOf(", ");
				String ids = fathersId.substring(0, index);
				input.setAttribute("fathersId", ids);		
			}
			
			mfrag.appendChild(input);
		}
		
		// RESIDENT NODE
		for (int j = 0; j < mfragTest.getNodeResidentList().size(); j++) {
			String id = mfragTest.getNodeResidentList().get(j).getId();
			String name = mfragTest.getNodeResidentList().get(j).getName();
			List<NodeObjectModel> fatherList = mfragTest.getNodeResidentList().get(j).getFatherNode();
			
			Element resident = doc.createElement(residentNode);
			resident.setAttribute("id", id);
			resident.setAttribute("name", name);
			
			if (fatherList.size() > 0) {
				String fathersId = "";
				for (int i = 0; i < fatherList.size(); i++) {
					fathersId = fathersId + fatherList.get(i).getId() + ", "; 
				}
				int index = fathersId.lastIndexOf(", ");
				String ids = fathersId.substring(0, index);
				resident.setAttribute("fathersId", ids);		
			}
			
			mfrag.appendChild(resident);
		}
		
		// NOT DEFINED NODE
		for (int j = 0; j < mfragTest.getNodeNotDefinedList().size(); j++) {
			String id = mfragTest.getNodeNotDefinedList().get(j).getId();
			String name = mfragTest.getNodeNotDefinedList().get(j).getName();
			List<NodeObjectModel> fatherList = mfragTest.getNodeNotDefinedList().get(j).getFatherNode();
			
			Element notDefined = doc.createElement(notDefinedNode);
			notDefined.setAttribute("id", id);
			notDefined.setAttribute("name", name);
			
			if (fatherList.size() > 0) {
				String fathersId = "";
				for (int i = 0; i < fatherList.size(); i++) {
					fathersId = fathersId + fatherList.get(i).getId() + ", "; 
				}
				int index = fathersId.lastIndexOf(", ");
				String ids = fathersId.substring(0, index);
				notDefined.setAttribute("fathersId", ids);		
			}
			
			mfrag.appendChild(notDefined);
		}
		
		return rootNode;
	}	
	
}
