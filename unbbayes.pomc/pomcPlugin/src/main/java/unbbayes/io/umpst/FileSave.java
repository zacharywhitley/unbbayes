package unbbayes.io.umpst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.EmptyStackException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.CommonDataUtil;

public class FileSave {

	public static final String NULL = "null";

	private UMPSTProject umpstProject;
	private File file;

	/**
	 * @param _file
	 * @param umpstProject
	 * @throws FileNotFoundException
	 */
	public void saveAsNewFormat(File _file, UMPSTProject umpstProject)
			throws FileNotFoundException {

		this.umpstProject = umpstProject;

		file = _file;
		String fileName = file.getName();
		int index = fileName.lastIndexOf(".");

		/* To be defined */
		String fileExtension = null;
		if (index >= 0) {
			fileExtension = fileName.substring(index + 1);
		}

		if ((fileExtension == null) || (!fileExtension.equals("ump"))) {
			file = new File(file.getPath() + ".ump");
		}

		Document doc = null;
		Element root = null;

		/*
		 * Enables applications to obtain a parser that produces DOM object tree
		 * from XML documents
		 */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			
			/* Use factory to get an instance of document builder */
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();

			FileBuildNodeHierarchy build = new FileBuildNodeHierarchy();
			FileSaveHeader hd = new FileSaveHeader();

			/* Root elements */
			root = doc.createElement("newFormat");
			root.setAttribute("version", "0");
			root.setAttribute("newFormatInfs",
					"http://unbbayes.sourceforge.net");
			doc.appendChild(root);

			/* ------------------- Header ------------------- */
			
			/* OBS. Needs throw an exception when one of the header attributes is null */			
			String modelName = fileName;
			if(index >= 0) {
				modelName = modelName.substring(0, index);				
			}
			
			/* OBS. The model author is the last author of some object if the model was
			 * not loaded */
			String modelAuthor = umpstProject.getAuthorModel();
			if(modelAuthor == null) {
				modelAuthor = CommonDataUtil.getInstance().getAuthorName();
			}
			
			String modelCreateDate = umpstProject.getDate();
			String modelUploadDate = CommonDataUtil.getInstance().getActualDate();
			if(modelCreateDate == null) {
				modelCreateDate = modelUploadDate;
			}
						
			Element headerTag = hd.renderHeader(doc, root, "0", "0",
					modelName, modelAuthor, modelCreateDate, modelUploadDate);
			root.appendChild(headerTag);			

			/* ------------------- Model ------------------- */
			Element modelTag = doc.createElement("model");
			root.appendChild(modelTag);

			/* Goal */
			build.goalNodeHierarchy(doc, modelTag, umpstProject);

			/* Hypothesis */
			build.hypothesisNodeHierarchy(doc, modelTag, umpstProject);
			
			/* Entity */
			build.entityNodeHierarchy(doc, modelTag, umpstProject);
			
			/* Attribute */
			build.attributeNodeHierarchy(doc, modelTag, umpstProject);

			/* Relationship */
			build.relationshipNodeHierarchy(doc, modelTag, umpstProject);

			/* Rule */
			build.ruleNodeHierarchy(doc, modelTag, umpstProject);

			/* Group */
			build.groupNodeHierarchy(doc, modelTag, umpstProject);

			try {
				/* Can transform a source tree into a result tree */
				Transformer tr = TransformerFactory.newInstance()
						.newTransformer();
				tr.setOutputProperty(OutputKeys.INDENT, "yes");
				tr.setOutputProperty(OutputKeys.METHOD, "xml");
				tr.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");
				tr.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "4");

				/* Send DOM to file */
				tr.transform(new DOMSource(doc), new StreamResult(
						new FileOutputStream(file)));
			} catch (TransformerException te) {
				System.out.println(te.getMessage());
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		} catch (ParserConfigurationException pce) {
			System.out.println("UsersXML: Error trying to instantiate DocumentBuilder "	+ pce);
		}		
	}
}