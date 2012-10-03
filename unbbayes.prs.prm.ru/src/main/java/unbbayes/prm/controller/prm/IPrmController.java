package unbbayes.prm.controller.prm;

import java.util.HashMap;
import java.util.List;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.FloatCollection;

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
	 * @param parent
	 *            relationship
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
	 * 
	 * @param attribute
	 * @return
	 */
	boolean isProbElement(Attribute attribute);

	/**
	 * Get the parents for an attribute.
	 * 
	 * @param attribute
	 *            interested attribute.
	 * @return parents.
	 */
	ParentRel[] parentsOf(Attribute attribute);

	/**
	 * Define a Conditional Probability Distribution for an attribute.
	 * 
	 * @param attribute
	 * @param table
	 */
	void setCPD(Attribute attribute, PotentialTable table);

	/**
	 * There is a special case when the same attribute has many CPDs. It is when
	 * the attribute act as a parent and as a child.
	 * 
	 * @param attribute
	 * @param table
	 */
	void setCPD(Attribute attribute, PotentialTable[] table);

	PotentialTable getCPD(Attribute attribute);

	PotentialTable[] getCPDs(Attribute attribute);

	/**
	 * Get the parents of the probabilistic model.
	 * 
	 * @return the parents
	 */
	List<ParentRel> getParents();

	/**
	 * Set the parents of the probabilistic model.
	 * 
	 * @param parents
	 *            the parents.
	 */
	void setParents(List<ParentRel> parents);

	/**
	 * CPD
	 * 
	 * @return
	 */
//	HashMap<String, FloatCollection[]> getCpds();
//
//	void setCpds(HashMap<String, PotentialTable[]> cpds);
}
