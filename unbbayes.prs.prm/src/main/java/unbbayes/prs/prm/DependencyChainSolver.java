/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
			
			// start finding out who are the actual children. Let's start finding out who are the objects specified by the FKs
			
			// prepare a list to be used by getObjectsByForeignKey(currentObjects, fk)
			List<IPRMObject> currentlyEvaluatedObjects = new ArrayList<IPRMObject>();
			currentlyEvaluatedObjects.add(prmNode.getContainerObject());	// initialize evaluation of linked parents starting by "my" node
			
			// obtain the linked objects recursively by running through each fk.
			if (dependencyChain != null && dependencyChain.getForeignKeyChain() != null) {
				// note that since we want the path from parent to child, the fk chain is in correct order (so, we don't need to revert it first as in solveParents())
				for (IForeignKey fk : dependencyChain.getForeignKeyChain()) {
					currentlyEvaluatedObjects = getObjectsByForeignKey(currentlyEvaluatedObjects, fk);
				}
			}
			
			// extract 
			for (IPRMObject childObj : currentlyEvaluatedObjects) {
				// Note that if fk == null || dependencyChain.getForeignKeyChain() == null, this dependency connects attributes from same object
				IAttributeValue valueToAdd = childObj.getAttributeValueMap().get(dependencyChain.getDependencyTo().getAttributeDescriptor());
				if (valueToAdd != null) {
					ret.add(valueToAdd);
				} else {
					throw new RuntimeException(
							"There was an internal error solving the parents of " 
							+ prmNode
							+ ". " 
							+ childObj
							+ " was expected to be a parent and to contain " 
							+ dependencyChain.getDependencyFrom().getAttributeDescriptor()
							+ " as its attribute.");
				}
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
			
			// start finding out who are the actual parents. Let's start finding out who are the objects specified by the FKs
			
			// prepare a list to be used by getObjectsByForeignKey(currentObjects, fk)
			List<IPRMObject> currentlyEvaluatedObjects = new ArrayList<IPRMObject>();
			currentlyEvaluatedObjects.add(prmNode.getContainerObject());	// initialize evaluation of linked parents starting by "my" node
			
			// obtain the linked objects recursively by running through each fk.
			if (dependencyChain != null && dependencyChain.getForeignKeyChain() != null) {
				// note that since we want the path from child to parent, the fk chain is in iverse order (so, we must revert it first)
				List<IForeignKey> reverseFKList = new ArrayList<IForeignKey>(dependencyChain.getForeignKeyChain());
				Collections.reverse(reverseFKList);
				for (IForeignKey fk : reverseFKList) {
					currentlyEvaluatedObjects = getObjectsByForeignKey(currentlyEvaluatedObjects, fk);
				}
			}
			
			// extract 
			for (IPRMObject parentObj : currentlyEvaluatedObjects) {
				// Note that if fk == null || dependencyChain.getForeignKeyChain() == null, this dependency connects attributes from same object
				IAttributeValue valueToAdd = parentObj.getAttributeValueMap().get(dependencyChain.getDependencyFrom().getAttributeDescriptor());
				if (valueToAdd != null) {
					ret.add(valueToAdd);
				} else {
					throw new RuntimeException(
							"There was an internal error solving the parents of " 
							+ prmNode
							+ ". " 
							+ parentObj
							+ " was expected to be a parent and to contain " 
							+ dependencyChain.getDependencyFrom().getAttributeDescriptor()
							+ " as its attribute.");
				}
			}						
						
		}
		
		return ret;
	}

	/**
	 * @param currentObjects : OBS. these objects' classes must be consistent to those specified by fk (i.e. at least the fk
	 * must be pointing from or to the objects' classes).
	 * @param fk
	 * @return a list of objects linked to currentObjects by the given FK.
	 * If fk == null, it returns currentObjects. If currentObjects == null, returns null.
	 */
	protected List<IPRMObject> getObjectsByForeignKey (List<IPRMObject> currentObjects, IForeignKey fk) {
		
		// initial assertives
		if (fk == null || currentObjects == null) {
			return currentObjects;
		} 
		
		// return
		List<IPRMObject> ret = new ArrayList<IPRMObject>();
		
		for (IPRMObject currentObject : currentObjects) {
			if (fk.getClassFrom().equals(currentObject.getPRMClass())) {
				// this is direct FK (from myself to parent).
				// use FK value from the evaluated node and PK value from destination node
				// TODO remove ALPHA restriction that only allows 1 PK attribute
				IAttributeValue fkValue = currentObject.getAttributeValueMap().get(fk.getKeyAttributesFrom().iterator().next());
				if (fkValue != null && fkValue.getValue() != null) {
					// find out what object has its pk's value equal to my fk value
					for (IPRMObject targetObj : fk.getClassTo().getPRMObjects()) {
						if (fkValue.getValue().equals(targetObj.getAttributeValueMap().get(fk.getKeyAttributesTo().iterator().next()).getValue())) {
							ret.add(targetObj);
							break;	// we obviously assume there is only one PK
						}
					}
				}
			} else if (fk.getClassTo().equals(currentObject.getPRMClass())) {
				// this is inverse FK (from parents to myself)
				// use PK value from evaluated node and FK value from destination node
				// TODO remove ALPHA restriction that only allows 1 PK attribute
				IAttributeValue pkValue = currentObject.getAttributeValueMap().get(fk.getKeyAttributesTo().iterator().next());
				if (pkValue != null && pkValue.getValue() != null) {
					// find out what object has its fk's value equal to my pk value
					for (IPRMObject parentObj : fk.getClassFrom().getPRMObjects()) {
						if (pkValue.getValue().equals(parentObj.getAttributeValueMap().get(fk.getKeyAttributesFrom().iterator().next()).getValue())) {
							ret.add(parentObj);
						}
					}
				}
			} else {
				// fk is inconsistent, because it does not form a chain...
				throw new IllegalArgumentException(
							fk 
							+ " is expected to link to/from " 
							+ currentObject);
			}
		}
		return ret;
	}
	
}
