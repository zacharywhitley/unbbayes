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
	
	private String idNode = null;
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
	
	protected Element buildNode(Document doc, Element parent, ObjectModel type) {		
		
		Element rootNode = doc.createElement(nameNode);
		parent.appendChild(rootNode);
		
		Element nodeId = doc.createElement(idNode);		
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
	
	/**
	 * @param nameNode the nameNode to set
	 */
	protected void setNameNode(String nameNode) {
		this.nameNode = nameNode;
	}
	
	/** 
	 * @return the nameNode
	 */
	protected String getNameNode() {
		return nameNode;
	}
	
	/** 
	 * @param nameObject the nameObject to set
	 */
	protected void setNameObject(String nameObject) {
		this.nameObject = nameObject;
	}
	
	/**
	 * @return the nameObject
	 */
	protected String getNameObject() {
		return nameObject;
	}

	/**
	 * @param nodeFather the nodeFather to set
	 */
	protected void setNodeFather(Element nodeFather) {
		this.nodeFather = nodeFather;
	}
	
	/** 
	 * @return the nodeFather
	 */
	protected Element getNodeFather() {
		return nodeFather;
	}	

	/**
	 * @return the idNode
	 */
	protected String getIdNode() {
		return idNode;
	}

	/**
	 * @param idNode the idNode to set
	 */
	protected void setIdNode(String idNode) {
		this.idNode = idNode;
	}
}
