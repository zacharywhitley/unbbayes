/**
 * 
 */
package unbbayes.io.mebn;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntology;

import unbbayes.util.IBridgeImplementor;

/**
 * It extends OWLAPIStorageImplementorDecorator in order to
 * carry protege's {@link OWLEditorKit} (a reference to OWL workspace and its contents) as well
 * @author Shou Matsumoto
 *
 */
public class ProtegeStorageImplementorDecorator extends OWLAPIStorageImplementorDecorator {
	
	private OWLEditorKit owlEditorKit;

	/**
	 * The default constructor is not private in order to allow inheritance.
	 * @deprecated use {@link #getInstance(OWLModelManager)} instead.
	 */
	protected ProtegeStorageImplementorDecorator() {
		super();
	}
	
	/**
	 * Construction method using fields
	 * @param owlEditorKit
	 * @return an instance of ProtegeStorageImplementorDecorator
	 */
	public static IBridgeImplementor newInstance(OWLEditorKit owlEditorKit) {
		ProtegeStorageImplementorDecorator ret = new ProtegeStorageImplementorDecorator();
		ret.setOWLEditorKit(owlEditorKit);
		return ret;
	}
	
	
	
	/**
	 * @return the owlEditorKit
	 */
	public OWLEditorKit getOWLEditorKit() {
		return owlEditorKit;
	}

	/**
	 * @param owlEditorKit the owlEditorKit to set
	 */
	public void setOWLEditorKit(OWLEditorKit owlEditorKit) {
		this.owlEditorKit = owlEditorKit;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.OWLAPIStorageImplementorDecorator#getAdaptee()
	 */
	public OWLOntology getAdaptee() {
		if (this.getOWLEditorKit() == null || this.getOWLEditorKit().getModelManager() == null) {
			return null;
		}
		return this.getOWLEditorKit().getModelManager().getActiveOntology();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.OWLAPIStorageImplementorDecorator#setAdaptee(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public void setAdaptee(OWLOntology adaptee) {
		if (this.getOWLEditorKit() == null || this.getOWLEditorKit().getModelManager() == null) {
			return;
		}
		this.getOWLEditorKit().getModelManager().setActiveOntology(adaptee);
	}
	
}
