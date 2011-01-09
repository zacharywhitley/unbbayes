///**
// * 
// */
//package unbbayes.gui.mebn.ontology.protege;
//
//import org.protege.editor.core.editorkit.EditorKit;
//import org.protege.editor.core.editorkit.EditorKitFactory;
//import org.protege.editor.owl.OWLEditorKitFactory;
//import org.protege.editor.owl.model.OWLModelManager;
//
//import unbbayes.prs.mebn.ontology.protege.UnBBayesOWLModelManager;
//
///**
// * This class extends OWLEditorKitFactory in order to build
// * instances of {@link UnBBayesOWLEditorKit} instead of
// * building instances of the default editor kits.
// * This is useful if the original protege classes shows some
// * problems when using in non-OSGI environments.
// * @author Shou Matsumoto
// *
// */
//public class UnBBayesOWLEditorKitFactory extends OWLEditorKitFactory {
//
//	private OWLModelManager modelManager;
//	
//	/**
//	 * The default constructor is not private to allow inheritance.
//	 * @deprecated use {@link #newInstance()} instead.
//	 */
//	protected UnBBayesOWLEditorKitFactory() {
//		// TODO Auto-generated constructor stub
//	}
//	
//	/**
//	 * Constructor method initializing fields. It calls {@link #setModelManager(OWLModelManager)}.
//	 * @param modelManager
//	 * @return a new instance of the factory
//	 */
//	public static EditorKitFactory newInstance(OWLModelManager modelManager) {
//		UnBBayesOWLEditorKitFactory ret = new UnBBayesOWLEditorKitFactory();
//		ret.setModelManager(modelManager);
//		return ret;
//	}
//	
//	/**
//	 * It creates an instance of this class initializing the value of {@link #getModelManager()}
//	 * to a default value ({@link UnBBayesOWLModelManager}).
//	 * @return a new instance with {@link #getModelManager()} instanceof ({@link UnBBayesOWLModelManager})
//	 * @see #setModelManager(OWLModelManager)
//	 * @see #getModelManager()
//	 */
//	public static EditorKitFactory newInstance() {
//		UnBBayesOWLEditorKitFactory ret = new UnBBayesOWLEditorKitFactory();
//		ret.setModelManager(UnBBayesOWLModelManager.newInstance());
//		return ret;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.protege.editor.owl.OWLEditorKitFactory#createEditorKit()
//	 */
//	public EditorKit createEditorKit() throws Exception {
//		return UnBBayesOWLEditorKit.newInstance(this, this.getModelManager());
//	}
//
//	/**
//	 * This is the default OWLModelManager to be used to instantiate the {@link UnBBayesOWLEditorKit}
//	 * @return the modelManager
//	 */
//	public OWLModelManager getModelManager() {
//		return modelManager;
//	}
//
//	/**
//	 * This is the default OWLModelManager to be used to instantiate the {@link UnBBayesOWLEditorKit}
//	 * @param modelManager the modelManager to set
//	 */
//	public void setModelManager(OWLModelManager modelManager) {
//		this.modelManager = modelManager;
//	}
//
//	
//	
//}
