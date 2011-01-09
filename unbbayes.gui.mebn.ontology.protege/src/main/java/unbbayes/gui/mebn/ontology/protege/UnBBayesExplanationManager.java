/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.explanation.ExplanationManager;

import unbbayes.util.Debug;

/**
 * This class extends protege's explanation manager in order to stop loading
 * osgi plug-ins. This is because UnBBayes does not use osgi plugins and
 * protege seems to fail when osgi is not used.
 * @author Shou Matsumoto
 *
 */
public class UnBBayesExplanationManager extends ExplanationManager {

	private OWLEditorKit editorKit;
	
	/**
	 * The constructors are not private in order to allow inheritance
	 * @deprecated use {@link #getInstance(OWLEditorKit)} instead
	 */
	protected UnBBayesExplanationManager() {
		this(null);
	}
	
	/**
	 * The constructors are not private in order to allow inheritance
	 * @param editorKit : the editor kit to delegate
	 * @see #setEditorKit(OWLEditorKit)
	 * @deprecated use {@link #getInstance(OWLEditorKit)} instead
	 */
	protected UnBBayesExplanationManager(OWLEditorKit editorKit) {
		super(editorKit);
		this.setEditorKit(editorKit);
	}
	
	/**
	 * Constructor method using fields.
	 * @param editorKit
	 * @return a new instance of explanation manager
	 */
	public static ExplanationManager getInstance(OWLEditorKit editorKit) {
		UnBBayesExplanationManager ret = new UnBBayesExplanationManager(editorKit);
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.ui.explanation.ExplanationManager#reload()
	 */
	public void reload() {
		if (this.getOWLEditorKit() == null) {
			return;	// do nothing if kit is not ready
		}
		this.getExplainers().clear();	// it seems that this is the only thing done by super other than reloading plugins
		// do not reload plugins
//		super.reload();
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.ui.explanation.ExplanationManager#getModelManager()
	 */
	public OWLModelManager getModelManager() {
		if (this.getOWLEditorKit() == null) {
			return null;
		}
		return this.getOWLEditorKit().getModelManager();
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.ui.explanation.ExplanationManager#getOWLEditorKit()
	 */
	public OWLEditorKit getOWLEditorKit() {
		if (this.editorKit != null) {
			return this.editorKit;
		} else {
			// the editor kits are not synchronized.
			try {
				// synchronize to upper class
				OWLEditorKit kit = super.getOWLEditorKit();
				this.setEditorKit(kit);
				return kit;
			} catch (Throwable e) {
				Debug.println(this.getClass(),"OWLEditorKit == null",e);
			}
		}
		return null;
	}

	/**
	 * @param editorKit the editorKit to set
	 */
	public void setEditorKit(OWLEditorKit editorKit) {
		this.editorKit = editorKit;
	}
	
}
