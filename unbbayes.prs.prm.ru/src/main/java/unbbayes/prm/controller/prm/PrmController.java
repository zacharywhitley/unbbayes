package unbbayes.prm.controller.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.bn.PotentialTable;

/**
 * PRM algorithm implementation.
 * 
 * @author David Saldaña
 * 
 */
public class PrmController implements IPrmController {
	Logger log = Logger.getLogger(PrmController.class);

	List<ParentRel> parents;

	/**
	 * Dictionary to store every potential table from an attribute. Normally an
	 * attribute only has a PotentialTable, but there is a special case when the
	 * same attribute act as parent and child at the same time. Just for this
	 * case is required more tan a potential table.
	 * 
	 * PotentialTable[] is stored initially every parent and then the child. Eg.
	 * {parent1CPD, parent2CPD, parent3CPD, childCPD}.
	 */
	HashMap<Attribute, PotentialTable[]> cpds;

	public PrmController() {
		parents = new ArrayList<ParentRel>();
		cpds = new HashMap<Attribute, PotentialTable[]>();
	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public void addParent(ParentRel parent) {
		log.debug("New parent added "
				+ parent.getParent().getAttribute().getName() + " -> "
				+ parent.getChild().getAttribute().getName());

		parents.add(parent);

	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public void removeParent(ParentRel parent) {
		parents.remove(parents.indexOf(parent));
	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public Attribute[] getProbElements() {

		Set<Attribute> elements = getElements();

		return elements.toArray(new Attribute[0]);
	}

	private Set<Attribute> getElements() {
		Set<Attribute> elements = new HashSet<Attribute>();

		for (ParentRel parent : parents) {
			elements.add(parent.getParent());
			elements.add(parent.getChild());
		}
		return elements;
	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public boolean isProbElement(Attribute attribute) {
		return getElements().contains(attribute);
	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public void setCPD(Attribute attribute, PotentialTable table) {
		setCPD(attribute, new PotentialTable[] { table });
	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public void setCPD(Attribute attribute, PotentialTable[] table) {
		cpds.put(attribute, table);
	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public PotentialTable getCPD(Attribute attribute) {
		PotentialTable[] potentialTables = cpds.get(attribute);

		if (potentialTables != null) {
			return cpds.get(attribute)[0];
		}
		return null;
	}
	
	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public PotentialTable[] getCPDs(Attribute attribute) {
		return cpds.get(attribute);
	}

	

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public ParentRel[] parentsOf(Attribute attribute) {

		List<ParentRel> parentsOf = new ArrayList<ParentRel>();
		for (ParentRel parent : parents) {
			if (parent.getChild().equals(attribute)) {
				parentsOf.add(parent);
			}
		}
		return parentsOf.toArray(new ParentRel[0]);
	}

}
