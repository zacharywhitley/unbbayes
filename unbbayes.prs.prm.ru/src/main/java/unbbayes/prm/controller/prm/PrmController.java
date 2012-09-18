package unbbayes.prm.controller.prm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.Graph;
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

	public PrmController() {
		parents = new ArrayList<ParentRel>();

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

	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public Graph compile() {

		Graph g = null;

		// TODO everything to do

		return g;
	}

	/**
	 * @see unbbayes.prm.controller.prm.IPrmController
	 */
	@Override
	public Attribute[] parentsOf(Attribute attribute) {

		List<Attribute> parentsOf = new ArrayList<Attribute>();
		for (ParentRel parent : parents) {
			if (parent.getChild().equals(attribute)) {
				parentsOf.add(parent.getParent());
			}
		}
		return parentsOf.toArray(new Attribute[0]);
	}

}
