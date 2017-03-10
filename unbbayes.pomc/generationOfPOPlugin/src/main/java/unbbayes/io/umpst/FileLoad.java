package unbbayes.io.umpst;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xalan.xsltc.runtime.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.CommonDataUtil;

/**
 * Load all objects and its properties saved in New Format File
 * 
 * @author Diego Marques
 */
public class FileLoad {
	
//	private String creationModelDate = null;	
	
	/** 
	 * @param file
	 * @param _umpstProject
	 * @return umpstProject
	 * @throws InvalidClassException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public  UMPSTProject loadAsNewFormat(File file,UMPSTProject _umpstProject) 
			throws InvalidClassException, IOException, ClassNotFoundException, 
			ParserConfigurationException, SAXException {
		
		UMPSTProject umpstProject = new UMPSTProject();
		FileLoadNodeGoal loadGoal = new FileLoadNodeGoal();
		FileLoadNodeHypothesis loadHypothesis = new FileLoadNodeHypothesis();
		FileLoadNodeEntity loadEntity = new FileLoadNodeEntity();
		FileLoadNodeAttribute loadAttribute = new FileLoadNodeAttribute();
		FileLoadNodeRelationship loadRelationship = new FileLoadNodeRelationship();
		FileLoadNodeRule loadRule = new FileLoadNodeRule();
		FileLoadNodeGroup loadGroup = new FileLoadNodeGroup();		
		
		// Get document builder
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();	
		
		// Build document
		Document dom = db.parse(file);
			
		// Document root node
		Element root = dom.getDocumentElement();
		
		// Keep data header
		NodeList listHeader = root.getElementsByTagName("header");
		NodeList nodeHeader = listHeader.item(0).getChildNodes();
		Element elemHeader = (Element) nodeHeader;
		
		String modelName = elemHeader.getElementsByTagName("modelName").item(0).getTextContent();
		umpstProject.setModelName(modelName);
		
		String date = elemHeader.getElementsByTagName("date").item(0).getTextContent();
		umpstProject.setDate(date);		
		String author = elemHeader.getElementsByTagName("author").item(0).getTextContent();
		umpstProject.setAuthorModel(author);	
		
		// Load goals
		NodeList listGoals = root.getElementsByTagName("goal");
		umpstProject.setMapGoal(loadGoal.getMapGoals(listGoals, umpstProject));
		
		// Load hypothesis
		NodeList listHypothesis = root.getElementsByTagName("hypothesis");
		umpstProject.setMapHypothesis(loadHypothesis.getMapHypothesis(listHypothesis, umpstProject));

		// Load entities
		NodeList listEntities = root.getElementsByTagName("entity");
		umpstProject.setMapEntity(loadEntity.getMapEntity(listEntities, umpstProject));
		
		// Load attributes
		NodeList listAttributes = root.getElementsByTagName("attribute");
		umpstProject.setMapAtribute(loadAttribute.getMapAttribute(listAttributes, umpstProject));
		
		// Load relationship
		NodeList listRelationship = root.getElementsByTagName("relationship");
		umpstProject.setMapRelationship(loadRelationship.getMapRelationship(listRelationship, umpstProject));
		
		// Load rules
		NodeList listRules = root.getElementsByTagName("rule");
		umpstProject.setMapRules(loadRule.getMapRule(listRules, umpstProject));

		// Load groups		
		NodeList listGroups = root.getElementsByTagName("group");
		umpstProject.setMapGroups(loadGroup.getMapGroup(listGroups, umpstProject));
		
		return umpstProject;		
	}

	/**
	 * @return the creationModelDate
	 */
//	public String getCreationModelDate() {
//		return creationModelDate;
//	}

	/**
	 * @param creationModelDate the creationModelDate to set
	 */
//	public void setCreationModelDate(String creationModelDate) {
//		this.creationModelDate = creationModelDate;
//	}
	
	
}