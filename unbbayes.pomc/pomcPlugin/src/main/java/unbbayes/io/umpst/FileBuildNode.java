package unbbayes.io.umpst;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import unbbayes.model.umpst.ObjectModel;

/**
 * Instance and set node properties with argument values
 * 
 * @author Diego Marques
 */
public class FileBuildNode {
	
	private String nameNode = null;
	private String nameObject = null;
	private Element nodeFather = null;
	
	/**
	 * 
	 * @param doc
	 * @param parent
	 * @param type
	 * @return rootNode
	 */
	
	protected Element buildNode (Document doc, Element parent, ObjectModel type) {		
		
		Element rootNode = doc.createElement(nameNode);
		parent.appendChild(rootNode);
		
		Element nodeId = doc.createElement("id");		
		nodeId.appendChild(doc.createTextNode(type.getId()));
		rootNode.appendChild(nodeId);
		
		Element nodeName = doc.createElement(nameObject);		
		nodeName.appendChild(doc.createTextNode(type.getName()));
		rootNode.appendChild(nodeName);
		
		Element nodeComments = doc.createElement("comments");		
		nodeComments.appendChild(doc.createTextNode(type.getComments()));
		rootNode.appendChild(nodeComments);
		
		Element nodeAuthor = doc.createElement("author");		
		nodeAuthor.appendChild(doc.createTextNode(type.getAuthor()));
		rootNode.appendChild(nodeAuthor);
		
		Element nodeDate = doc.createElement("date");		
		nodeDate.appendChild(doc.createTextNode(type.getDate()));
		rootNode.appendChild(nodeDate);
		
		return rootNode;
	}
	
	protected void setNameNode(String nameNode) {
		this.nameNode = nameNode;
	}
	
	protected String getNameNode() {
		return nameNode;
	}
	
	protected void setNameObject(String nameObject) {
		this.nameObject = nameObject;
	}
	
	protected String getNameObject() {
		return nameObject;
	}

	public void setNodeFather(Element nodeFather) {
		this.nodeFather = nodeFather;
	}

	public Element getNodeFather() {
		return nodeFather;
	}
	
	protected void setFatherNodeElement(Element nodeFather) {
		this.nodeFather = nodeFather;		
	}
	
	protected Element getFatherNodeElement() {
		return nodeFather;		
	}
}
