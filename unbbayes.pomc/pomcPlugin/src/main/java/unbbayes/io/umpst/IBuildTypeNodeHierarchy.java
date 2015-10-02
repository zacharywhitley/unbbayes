package unbbayes.io.umpst;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import unbbayes.model.umpst.project.UMPSTProject;

public interface IBuildTypeNodeHierarchy {
	
	public abstract void goalNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject);
	
	public abstract void hypothesisNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject);
	
	public abstract void entityNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject);
	
	public abstract void attributeNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject);
	
	public abstract void relationshipNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject);
	
	public abstract void ruleNodeHierarchy(Document doc, Element parent, UMPSTProject umpstProject);

}