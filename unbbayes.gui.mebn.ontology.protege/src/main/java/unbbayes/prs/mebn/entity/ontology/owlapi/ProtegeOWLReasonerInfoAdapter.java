/**
 * 
 */
package unbbayes.prs.mebn.entity.ontology.owlapi;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

/**
 * This is an adapter of {@link ProtegeOWLReasonerInfo} to {@link OWLReasonerInfo}.
 * All methods will be simply delegated to {@link #getAdaptee()}.
 * @author Shou Matsumoto
 */
public class ProtegeOWLReasonerInfoAdapter implements ProtegeOWLReasonerInfo,
		OWLReasonerInfo {

	private ProtegeOWLReasonerInfo adaptee;

	/**
	 * The default constructor is kept protected to allow easy extension.
	 * @deprecated use {@link #getInstance(ProtegeOWLReasonerInfo)} instead.
	 */
	protected ProtegeOWLReasonerInfoAdapter() {}
	
	/**
	 * Default constructor method.
	 * @param adaptee : this {@link ProtegeOWLReasonerInfo} will be adapted by this adapter.
	 * @return the adapter.
	 */
	public static ProtegeOWLReasonerInfoAdapter getInstance(ProtegeOWLReasonerInfo adaptee) {
		ProtegeOWLReasonerInfoAdapter ret = new ProtegeOWLReasonerInfoAdapter();
		ret.setAdaptee(adaptee);
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	public void initialise() throws Exception {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return;
		}
		adaptee.initialise();
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	public void dispose() throws Exception {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return;
		}
		adaptee.dispose();
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#setup(org.semanticweb.owlapi.model.OWLOntologyManager, java.lang.String, java.lang.String)
	 */
	public void setup(OWLOntologyManager manager, String id, String name) {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return;
		}
		adaptee.setup(manager, id, name);
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#getReasonerId()
	 */
	public String getReasonerId() {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return null;
		}
		return adaptee.getReasonerId();
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#getReasonerName()
	 */
	public String getReasonerName() {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return null;
		}
		return adaptee.getReasonerName();
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#setOWLModelManager(org.protege.editor.owl.model.OWLModelManager)
	 */
	public void setOWLModelManager(OWLModelManager owlModelManager) {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return;
		}
		adaptee.setOWLModelManager(owlModelManager);
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#getOWLModelManager()
	 */
	public OWLModelManager getOWLModelManager() {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return null;
		}
		return adaptee.getOWLModelManager();
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#getRecommendedBuffering()
	 */
	public BufferingMode getRecommendedBuffering() {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return null;
		}
		return adaptee.getRecommendedBuffering();
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#getConfiguration(org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor)
	 */
	public OWLReasonerConfiguration getConfiguration(
			ReasonerProgressMonitor monitor) {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return null;
		}
		return adaptee.getConfiguration(monitor);
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo#getReasonerFactory()
	 */
	public OWLReasonerFactory getReasonerFactory() {
		ProtegeOWLReasonerInfo adaptee = this.getAdaptee();
		if (adaptee == null) {
			return null;
		}
		return adaptee.getReasonerFactory();
	}

	/**
	 * @return the adaptee : all methods of this class will delegate to this object.
	 */
	public ProtegeOWLReasonerInfo getAdaptee() {
		return adaptee;
	}

	/**
	 * @param adaptee :  : all methods of this class will delegate to this object.
	 */
	public void setAdaptee(ProtegeOWLReasonerInfo adaptee) {
		this.adaptee = adaptee;
	}

}
