package unbbayes.prm.controller.prm;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.PotentialTable;

/**
 * Methods to manage a PRM schema.
 * 
 * @author David Saldaña.
 *
 */
public interface IPrmController {
	

	/**
	 * Add a parent to the probabilistic model.
	 * 
	 * @param parent relationship
	 */
	void addParent(ParentRel parent);
	
	/**
	 * Remove a parent of the probabilistic model.
	 * 
	 * @param parent
	 */
	void removeParent(ParentRel parent);
	
	/**
	 * Get all the elements of the probabilistic model.
	 * 
	 * @return a set of children.
	 */
	Attribute[] getProbElements();
	
	/**
	 * Ask if an attribute is a child.
	 * @param attribute
	 * @return 
	 */
	boolean isProbElement(Attribute attribute);
	

	/**
	 * Get the parents for an attribute.
	 * @param attribute interested attribute.
	 * @return parents.
	 */
	Attribute[] parentsOf(Attribute attribute);
	
	/**
	 * Define a Conditional Probability Distribution for an attribute.
	 * @param attribute
	 * @param table
	 */
	void setCPD(Attribute attribute, PotentialTable table);
	
	
	/**
	 * Compile the current probabilistic definition.
	 */
	
	Graph compile();
}
