package unbbs.manipulation;

import javax.ejb.FinderException;
import javax.naming.NamingException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.traversal.NodeIterator;

import unbbayes.io.XMLIO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.XMLUtil;
import unbbayes.utility.HomeFactory;
import unbbs.persistence.model.ModelKey;
import unbbs.persistence.model.ModelLocal;
import unbbs.persistence.model.ModelLocalHome;

/**
 * Bean implementation class for Enterprise Bean: ModelManipulation
 */
public class ModelManipulationBean implements javax.ejb.SessionBean {
	public String compile(int modelId) {
		try {
			ProbabilisticNetwork net = getNetwork(modelId);
			net.compile();
			return makeMarginals(net);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String reinitialize(int modelId) {
		return compile(modelId);
	}

	public String propagate(int modelId, Document evidences) {
		try {
			ProbabilisticNetwork net = getNetwork(modelId);
			net.compile();
			CachedXPathAPI xpath = new CachedXPathAPI();
			NodeIterator nodeIterator =
				xpath.selectNodeIterator(evidences, "/EVIDENCES/EVIDENCE");
			org.w3c.dom.Node elNode;
			while ((elNode = nodeIterator.nextNode()) != null) {
				String nodeName = xpath.selectSingleNode(elNode, "@NODE").getNodeValue();
				String state = xpath.selectSingleNode(elNode, "@STATE").getNodeValue();
				TreeVariable var = (TreeVariable) net.getNode(nodeName);
				var.addFinding(Integer.parseInt(state));
			}
			net.updateEvidences();
			return makeMarginals(net);
			
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeMarginals(ProbabilisticNetwork net) {
		StringBuffer xml =
			new StringBuffer("<NET NAME='" + net.getName() + "' >");
		for (int i = 0; i < net.getNodeCount(); i++) {
			TreeVariable var = (TreeVariable) net.getNodeAt(i);
			xml.append(
				"<VARIABLE NAME='"
					+ var.getName()
					+ "' DESCRIPTION='"
					+ var.getDescription()
					+ "' >");
			for (int j = 0; j < var.getStatesSize(); j++) {
				xml.append(
					"<STATE NAME='"
						+ var.getStateAt(j)
						+ "' MARGINAL='"
						+ var.getMarginalAt(j)
						+ "' />");
			}
			xml.append("</VARIABLE>");
		}
		xml.append("</NET>");
		return xml.toString();
	}

	private ProbabilisticNetwork getNetwork(int modelId)
		throws NamingException, FinderException, Exception {

		ModelLocalHome modelLocalHome =
			(ModelLocalHome) HomeFactory.singleton().getHome("ejb/Model");

		ModelLocal modelLocal =
			modelLocalHome.findByPrimaryKey(new ModelKey(modelId));
		String model = modelLocal.getModel();
		XMLIO io = new XMLIO();
		return io.load(XMLUtil.getDocument(model));
	}

	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
}
