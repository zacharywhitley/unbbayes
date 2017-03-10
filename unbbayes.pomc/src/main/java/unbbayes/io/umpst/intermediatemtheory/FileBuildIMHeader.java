package unbbayes.io.umpst.intermediatemtheory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileBuildIMHeader {
	
	/* Render Header */
	public Element  buildIMHeader (Document doc, Element parent, String versionPlugin, String versionUnBBayes,
			String modelName, String author, String date, String updateDate) {
		
		Element headerTag = doc.createElement("header");
		parent.appendChild(headerTag);
		
		Element headerVersionPlugin = doc.createElement("versionPlugin");
		headerVersionPlugin.appendChild(doc.createTextNode(versionPlugin));
		headerTag.appendChild(headerVersionPlugin);
		
		Element headerVersionUnBBayes = doc.createElement("versionUnBBayes");
		headerVersionUnBBayes.appendChild(doc.createTextNode(versionUnBBayes));
		headerTag.appendChild(headerVersionUnBBayes);
		
		Element headerModelName = doc.createElement("modelName");
		headerModelName.appendChild(doc.createTextNode(modelName));
		headerTag.appendChild(headerModelName);
		
		Element headerAuthor = doc.createElement("author");
		headerAuthor.appendChild(doc.createTextNode(author));
		headerTag.appendChild(headerAuthor);
		
		Element headerDate = doc.createElement("date");
		headerDate.appendChild(doc.createTextNode(date));
		headerTag.appendChild(headerDate);
		
		Element headerUpdateDate = doc.createElement("updateDate");
		headerUpdateDate.appendChild(doc.createTextNode(updateDate));
		headerTag.appendChild(headerUpdateDate);
		
		return headerTag;
	}

}