package unbbs.manipulation;

import java.io.File;
import java.io.IOException;

import javax.ejb.FinderException;
import javax.naming.NamingException;

import unbbayes.io.BaseIO;
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
			ModelLocalHome modelLocalHome =
				(ModelLocalHome) HomeFactory.singleton().getHome("ejb/Model");

			ModelLocal modelLocal =
				modelLocalHome.findByPrimaryKey(new ModelKey(modelId));
			String model = modelLocal.getModel();
			XMLIO io = new XMLIO();
			ProbabilisticNetwork net = io.load(XMLUtil.getDocument(model));
			net.compile();

			StringBuffer xml = new StringBuffer("<NET NAME='" + modelLocal.getName() + "' >");
			for (int i = 0; i < net.getNodeCount(); i++) {
				TreeVariable var = (TreeVariable) net.getNodeAt(i);
				xml.append(
					"<VARIABLE NAME='"
						+ var.getName()
						+ "' DESCRIPTION='"
						+ var.getDescription()
						+ "' >");
				for (int j = 0; j < var.getStatesSize(); j++) {
					xml.append("<STATE NAME='" + var.getStateAt(j) + "' MARGINAL='" + var.getMarginalAt(j) + "' />");
				}
				xml.append("</VARIABLE>");
			}
			xml.append("</NET>");
			return xml.toString();

		} catch (NamingException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
