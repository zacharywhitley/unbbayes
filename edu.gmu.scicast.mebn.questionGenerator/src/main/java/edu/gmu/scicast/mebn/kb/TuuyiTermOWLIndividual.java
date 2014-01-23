/**
 * 
 */
package edu.gmu.scicast.mebn.kb;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * @author Shou Matsumoto
 *
 */
public class TuuyiTermOWLIndividual extends OWLNamedIndividualImpl {

	private Integer termID;

	/**
	 * @param dataFactory
	 */
	public TuuyiTermOWLIndividual(IRI iri, Integer termID) {
		super(null, iri);
		this.setTermID(termID);
	}


	/**
	 * @return the termID
	 */
	public Integer getTermID() {
		return termID;
	}

	/**
	 * @param termID the termID to set
	 */
	public void setTermID(Integer termID) {
		this.termID = termID;
	}


	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl#toStringID()
	 */
	public String toStringID() {
		if (termID == null) {
			return super.toStringID();
		}
		return ""+termID;
	}


	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof OWLObject) {
			return this.compareObjectOfSameType((OWLObject)obj) == 0;
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl#compareObjectOfSameType(org.semanticweb.owlapi.model.OWLObject)
	 */
	protected int compareObjectOfSameType(OWLObject object) {
		if (object instanceof TuuyiTermOWLIndividual) {
			TuuyiTermOWLIndividual otherTerm = (TuuyiTermOWLIndividual) object;
			if (otherTerm.getTermID() != null && this.getTermID() != null) {
				return this.getTermID().intValue() - otherTerm.getTermID().intValue();
			}
		}
		return super.compareObjectOfSameType(object);
	}
	
	

}
