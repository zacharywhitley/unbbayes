package unbbs.controller.model;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingException;

import unbbayes.utility.HomeFactory;
import unbbs.persistence.model.DomainKey;
import unbbs.persistence.model.DomainLocal;
import unbbs.persistence.model.DomainLocalHome;
import unbbs.persistence.model.DomainValue;
import unbbs.persistence.model.ModelKey;
import unbbs.persistence.model.ModelLocal;
import unbbs.persistence.model.ModelLocalHome;
import unbbs.persistence.model.ModelValue;

/**
 * Bean implementation class for Enterprise Bean: ModelSession
 */
public class ModelSessionBean implements javax.ejb.SessionBean {
	private javax.ejb.SessionContext mySessionCtx;

	public void saveModel(
		int id,
		String name,
		String description,
		String model,
		int domainId) {
		try {

			DomainLocalHome domainLocalHome =
				(DomainLocalHome) HomeFactory.singleton().getHome("ejb/Domain");

			ModelLocalHome modelLocalHome =
				(ModelLocalHome) HomeFactory.singleton().getHome("ejb/Model");

			DomainLocal domainLocal =
				domainLocalHome.findByPrimaryKey(new DomainKey(domainId));

			modelLocalHome.create(id, name, description, model, domainLocal);

		} catch (NamingException e) {
			System.err.println("Error looking up homes! " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(
				"General error while saving new model! " + e.getMessage());
			e.printStackTrace();
		}
	}

	public ModelValue detailModel(int id) {
		try {

			ModelLocalHome modelLocalHome =
				(ModelLocalHome) HomeFactory.singleton().getHome("ejb/Model");

			ModelLocal modelLocal =
				modelLocalHome.findByPrimaryKey(new ModelKey(id));

			ModelValue mv = new ModelValue();
			DomainValue dv = new DomainValue();
			dv.setId(
				((DomainKey) modelLocal.getDomain().getPrimaryKey()).getId());
			dv.setName(modelLocal.getDomain().getName());
			mv.setId(id);
			mv.setName(modelLocal.getName());
			mv.setDescription(modelLocal.getDescription());
			mv.setModel(modelLocal.getModel());
			mv.setDomain(dv);

			return mv;

		} catch (Exception e) {
			System.err.println(
				"General error while detailing model "
					+ id
					+ "! "
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public void editModel(
		int id,
		String name,
		String description,
		String model,
		int domainId) {

		try {

			ModelLocalHome modelLocalHome =
				(ModelLocalHome) HomeFactory.singleton().getHome("ejb/Model");

			DomainLocalHome domainLocalHome =
				(DomainLocalHome) HomeFactory.singleton().getHome("ejb/Domain");

			DomainLocal domainLocal =
				domainLocalHome.findByPrimaryKey(new DomainKey(domainId));

			ModelLocal modelLocal =
				modelLocalHome.findByPrimaryKey(new ModelKey(id));

			modelLocal.setName(name);
			modelLocal.setDescription(description);
			modelLocal.setModel(model);
			modelLocal.setDomain(domainLocal);

		} catch (Exception e) {
			System.err.println(
				"General error while editing model "
					+ id
					+ "! "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	public ModelValue[] searchModel(String search) {

		try {

			ModelLocalHome modelLocalHome =
				(ModelLocalHome) HomeFactory.singleton().getHome("ejb/Model");

			// Search for the words in the search String in any position of 
			// name and description --> that is why "%" + search + "%"
			Collection modelLocals =
				modelLocalHome.findByNameOrDescription("%" + search + "%");

			ModelValue[] mvs = new ModelValue[modelLocals.size()];

			Iterator model = modelLocals.iterator();
			ModelLocal modelLocal;
			ModelValue mv;
			DomainValue dv;
			int i = 0;
			while (model.hasNext()) {
				modelLocal = (ModelLocal) model.next();
				mv = new ModelValue();
				dv = new DomainValue();
				dv.setId(
					((DomainKey) modelLocal.getDomain().getPrimaryKey())
						.getId());
				dv.setName(modelLocal.getDomain().getName());
				mv.setId(((ModelKey) modelLocal.getPrimaryKey()).getId());
				mv.setName(modelLocal.getName());
				mv.setDescription(modelLocal.getDescription());
				mv.setModel(modelLocal.getModel());
				mv.setDomain(dv);
				
				mvs[i] = mv;
				i++;
			}
			
			return mvs;

		} catch (Exception e) {
			System.err.println(
				"General error while searching model with name and/or "
					+ "description \""
					+ search
					+ "\" ! "
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public DomainValue[] searchAllDomain() {

		try {

			DomainLocalHome domainLocalHome =
				(DomainLocalHome) HomeFactory.singleton().getHome("ejb/Domain");

			Collection domainLocals =
				domainLocalHome.findAll();

			DomainValue[] dvs = new DomainValue[domainLocals.size()];

			Iterator domain = domainLocals.iterator();
			DomainLocal domainLocal;
			DomainValue dv;
			int i = 0;
			while (domain.hasNext()) {
				domainLocal = (DomainLocal) domain.next();
				dv = new DomainValue();
				dv.setId(
					((DomainKey) domainLocal.getPrimaryKey())
						.getId());
				dv.setName(domainLocal.getName());
				
				dvs[i] = dv;
				i++;
			}
			
			return dvs;

		} catch (Exception e) {
			System.err.println(
				"General error while searching for all domains available! "
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

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
