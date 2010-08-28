/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.controller.prm.IDatabaseController;

/**
 * This is a default implementation of {@link IDependencyChainSolver}
 * which uses {@link IDatabaseController} to solve dependency
 * chains.
 * @author Shou Matsumoto
 * TODO solve the ALPHA restriction which allows only single PK and 1 level of FK chain.
 */
public class DependencyChainSolver implements IDependencyChainSolver {

	private IDatabaseController dbController;
	
	/**
	 * At leas one constructor must be protected in order to allow
	 * inheritance
	 */
	protected DependencyChainSolver() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Construction method using fields
	 * @param dbController
	 * @return
	 */
	public static DependencyChainSolver newInstance(IDatabaseController dbController) {
		DependencyChainSolver ret = new DependencyChainSolver();
		ret.setDbController(dbController);
		return ret;
	}

	/**
	 * @return the dbController
	 */
	public IDatabaseController getDbController() {
		return dbController;
	}

	/**
	 * @param dbController the dbController to set
	 */
	public void setDbController(IDatabaseController dbController) {
		this.dbController = dbController;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChainSolver#solveChildren(unbbayes.prs.prm.IAttributeValue)
	 */
	public List<IAttributeValue> solveChildren(IAttributeValue prmNode) {
		
		List<IAttributeValue> ret = new ArrayList<IAttributeValue>();
		
		// argument assertion
		if (prmNode == null) {
			return ret;
		}
		
		// iterate over each outgoing dependency chain (children)
		for (IDependencyChain dependencyChain : prmNode.getAttributeDescriptor().getPRMDependency().getDependencyChains()) {
			// dependency consistency check
			if (!dependencyChain.getDependencyFrom().equals(prmNode.getAttributeDescriptor().getPRMDependency())) {
				// if this "edge" is not coming from my node, it is erroneous
				throw new IllegalArgumentException(prmNode + " contains an outgoing dependency " + dependencyChain + " which does not start from " + prmNode);
			}
			
			// TODO remove ALPHA restriction impeding fk chain to be greater than 1
			if (dependencyChain.getForeignKeyChain().size() > 1) {
				throw new RuntimeException("ALPHA version cannot manipulate a FK chain greater than 1 level: " + dependencyChain.getForeignKeyChain());
			}
			
			// solve FK binding 
			IForeignKey fk = null;
			if (!dependencyChain.getForeignKeyChain().isEmpty()) {
				fk = dependencyChain.getForeignKeyChain().get(0);
			}
			
			if (fk == null ) {
				// this dependency connects attributes from same object
				IAttributeValue child = prmNode.getContainerObject().getAttributeValueMap().get(dependencyChain.getDependencyTo().getAttributeDescriptor());
				if (child != null && !ret.contains(child)) {
					ret.add(child);
				}
			} else if (fk.getClassFrom().equals(prmNode.getAttributeDescriptor().getPRMClass())) {
				// this is direck FK (from myself to child).
				// use FK value from the evaluated node and PK value from destination node
				// TODO remove ALPHA restriction that only allows 1 PK attribute
				IAttributeValue fkValue = prmNode.getContainerObject().getAttributeValueMap().get(fk.getKeyAttributesFrom().iterator().next());
				// check fkValue
				if (fkValue != null && fkValue.getValue() != null) {
					// find out what object has its pk's value equal to my fk value
					for (IPRMObject childObj : fk.getClassTo().getPRMObjects()) {
						if (fkValue.getValue().equals(childObj.getAttributeValueMap().get(fk.getKeyAttributesTo().iterator().next()).getValue())) {
							ret.add(childObj.getAttributeValueMap().get(dependencyChain.getDependencyTo().getAttributeDescriptor()));
							break;	// we obviously assume there is only one PK
						}
					}
				}
			} else if (fk.getClassTo().equals(prmNode.getAttributeDescriptor().getPRMClass())) {
				// this is inverse FK (from children to myself)
				// use PK value from evaluated node and FK value from destination node
				// TODO remove ALPHA restriction that only allows 1 PK attribute
				IAttributeValue pkValue = prmNode.getContainerObject().getAttributeValueMap().get(fk.getKeyAttributesTo().iterator().next());
				// check pkvalue
				if (pkValue != null && pkValue.getValue() != null) {
					// find out what object has its pk's value equal to my fk value
					for (IPRMObject childObj : fk.getClassFrom().getPRMObjects()) {
						if (pkValue.getValue().equals(childObj.getAttributeValueMap().get(fk.getKeyAttributesFrom().iterator().next()).getValue())) {
							ret.add(childObj.getAttributeValueMap().get(dependencyChain.getDependencyTo().getAttributeDescriptor()));
						}
					}
				}
			} else {
				// fk is inconsistent, because it does not form a chain...
				throw new IllegalArgumentException(
							dependencyChain.getForeignKeyChain() 
							+ " is not a valid FK chain, because " 
							+ fk 
							+ " cannot be connected to to " 
							+ prmNode.getAttributeDescriptor());
			}
			
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChainSolver#solveParents(unbbayes.prs.prm.IAttributeValue)
	 */
	public List<IAttributeValue> solveParents(IAttributeValue prmNode) {

		List<IAttributeValue> ret = new ArrayList<IAttributeValue>();
		
		// argument assertion
		if (prmNode == null) {
			return ret;
		}
		
		// iterate over each incoming dependency chain (parent)
		for (IDependencyChain dependencyChain : prmNode.getAttributeDescriptor().getPRMDependency().getIncomingDependencyChains()) {
			// dependency consistency check
			if (!dependencyChain.getDependencyTo().equals(prmNode.getAttributeDescriptor().getPRMDependency())) {
				// if this "edge" is not coming from my node, it is erroneous
				throw new IllegalArgumentException(prmNode + " contains an incoming dependency " + dependencyChain + " which does not end in " + prmNode);
			}
			
			// TODO remove ALPHA restriction impeding fk chain to be greater than 1
			if (dependencyChain.getForeignKeyChain().size() > 1) {
				throw new RuntimeException("ALPHA version cannot manipulate a FK chain greater than 1 level: " + dependencyChain.getForeignKeyChain());
			}
			
			// solve FK binding 
			IForeignKey fk = null;
			if (!dependencyChain.getForeignKeyChain().isEmpty()) {
				fk = dependencyChain.getForeignKeyChain().get(0);
			}
			
			if (fk == null ) {
				// this dependency connects attributes from same object
				IAttributeValue parent = prmNode.getContainerObject().getAttributeValueMap().get(dependencyChain.getDependencyFrom().getAttributeDescriptor());
				if (parent != null && !ret.contains(parent)) {
					ret.add(parent);
				}
			} else if (fk.getClassFrom().equals(prmNode.getAttributeDescriptor().getPRMClass())) {
				// this is direck FK (from myself to parent).
				// use FK value from the evaluated node and PK value from destination node
				// TODO remove ALPHA restriction that only allows 1 PK attribute
				IAttributeValue fkValue = prmNode.getContainerObject().getAttributeValueMap().get(fk.getKeyAttributesFrom().iterator().next());
				if (fkValue != null && fkValue.getValue() != null) {
					// find out what object has its pk's value equal to my fk value
					for (IPRMObject parentObj : fk.getClassTo().getPRMObjects()) {
						if (fkValue.getValue().equals(parentObj.getAttributeValueMap().get(fk.getKeyAttributesTo().iterator().next()).getValue())) {
							ret.add(parentObj.getAttributeValueMap().get(dependencyChain.getDependencyFrom().getAttributeDescriptor()));
							break;	// we obviously assume there is only one PK
						}
					}
				}
			} else if (fk.getClassTo().equals(prmNode.getAttributeDescriptor().getPRMClass())) {
				// this is inverse FK (from parents to myself)
				// use PK value from evaluated node and FK value from destination node
				// TODO remove ALPHA restriction that only allows 1 PK attribute
				IAttributeValue pkValue = prmNode.getContainerObject().getAttributeValueMap().get(fk.getKeyAttributesTo().iterator().next());
				if (pkValue != null && pkValue.getValue() != null) {
					// find out what object has its fk's value equal to my pk value
					for (IPRMObject parentObj : fk.getClassFrom().getPRMObjects()) {
						if (pkValue.getValue().equals(parentObj.getAttributeValueMap().get(fk.getKeyAttributesFrom().iterator().next()).getValue())) {
							ret.add(parentObj.getAttributeValueMap().get(dependencyChain.getDependencyFrom().getAttributeDescriptor()));
						}
					}
				}
			} else {
				// fk is inconsistent, because it does not form a chain...
				throw new IllegalArgumentException(
							dependencyChain.getForeignKeyChain() 
							+ " is not a valid FK chain, because " 
							+ fk 
							+ " cannot be connected to to " 
							+ prmNode.getAttributeDescriptor());
			}
			
		}
		
		return ret;
	}

	
}
