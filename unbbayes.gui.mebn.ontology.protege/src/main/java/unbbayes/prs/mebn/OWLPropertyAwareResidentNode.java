/**
 * 
 */
package unbbayes.prs.mebn;

import org.semanticweb.owlapi.model.IRI;

/**
 * This class extends {@link ResidentNode} and adds an attribute to store the 
 * IRI of a property. This is because OWL (or RDF) properties have a direct
 * semantic equivalence to resident nodes, so, if a resident node is generated
 * based on a pre-existing owl property (i.e. if a resident node is explicitely
 * representing a probabilistic conterpart of an OWL/RDF property), 
 * it is interesting to have a reference to the original OWL/RDF property.
 * @author Shou Matsumoto
 *
 */
public class OWLPropertyAwareResidentNode extends ResidentNode implements IOWLPropertyAwareNode {

	private IRI propertyIRI;
	
	/**
	 * The default constructor is visible to allow inheritance
	 * @deprecated use {@link #getInstance(String, MFrag)} instead
	 */
	protected OWLPropertyAwareResidentNode() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The default constructor is visible to allow inheritance
	 * @param name
	 * @param mFrag
	 * @deprecated use {@link #getInstance(String, MFrag)} instead
	 */
	protected OWLPropertyAwareResidentNode(String name, MFrag mFrag) {
		super(name, mFrag);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor method using fields.
	 * @param name : the name of the node
	 * @param mFrag : the mfrag where this node resides.
	 * @param propertyIRI : the IRI indicating the location of the OWL/RDF property representing the deterministic counterpart of this node.
	 * @return an instance of OWLPropertyAwareResidentNode
	 */
	public static OWLPropertyAwareResidentNode getInstance(String name, MFrag mFrag, IRI propertyIRI) {
		OWLPropertyAwareResidentNode ret = new OWLPropertyAwareResidentNode(name, mFrag);
		ret.setPropertyIRI(propertyIRI);
		return ret;
	}
	
	/**
	 * Default constructor method. It is equivalent to {@link #getInstance(String, MFrag, null)}.
	 * @param name : the name of the node
	 * @param mFrag : the mfrag where this node resides.
	 * @return an instance of OWLPropertyAwareResidentNode
	 * @see #getInstance(String, MFrag, IRI)
	 */
	public static OWLPropertyAwareResidentNode getInstance(String name, MFrag mFrag) {
		return OWLPropertyAwareResidentNode.getInstance(name, mFrag, null);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.IOWLPropertyAwareNode#getPropertyIRI()
	 */
	public IRI getPropertyIRI() {
		return propertyIRI;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.IOWLPropertyAwareNode#setPropertyIRI(org.semanticweb.owlapi.model.IRI)
	 */
	public void setPropertyIRI(IRI propertyIRI) {
		this.propertyIRI = propertyIRI;
	}

}
