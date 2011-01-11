/**
 * 
 */
package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ontology.protege.IBundleLauncher;
import unbbayes.prs.mebn.ontology.protege.ProtegeBundleLauncher;
import unbbayes.util.Debug;

/**
 * This class extends {@link OWLAPICompatiblePROWL2IO} in order
 * to fill {@link MultiEntityBayesianNetwork#getStorageImplementor()}
 * using {@link ProtegeStorageImplementorDecorator}
 * @author Shou Matsumoto
 *
 */
public class Protege41CompatiblePROWL2IO extends OWLAPICompatiblePROWL2IO {

	private IBundleLauncher protegeBundleLauncher;
	
	/**
	 * This is public just to enable plug-in compatibility
	 * @deprecated
	 */
	public Protege41CompatiblePROWL2IO() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method.
	 * @return
	 */
	public static MebnIO newInstance() {
		Protege41CompatiblePROWL2IO ret = new Protege41CompatiblePROWL2IO();
		ret.setResource(unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.io.mebn.resources.IoMebnResources.class.getName(),
				Locale.getDefault(),
				Protege41CompatiblePROWL2IO.class.getClassLoader()
			));
		ret.setWrappedLoaderPrOwlIO(new LoaderPrOwlIO());
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.OWLAPICompatiblePROWL2IO#loadMebn(java.io.File)
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException,
			IOMebnException {
		
		OWLEditorKit kit = null;	// kit to extract ontology and fill storage implementor (of mebn)
		
		try {
			// specify the bundle laucher that the desired URI is from file
			this.getProtegeBundleLauncher().setDefaultOntolgyURI(file.toURI());
			
			// load ontology using protege
			this.getProtegeBundleLauncher().startProtegeBundles();
			
			// obtain manager. We expect it to be an instance of ProtegeManager
			ProtegeManager manager = (ProtegeManager)this.getProtegeBundleLauncher().getProtegeManager();
			
			// obtain the last opened kit (which is the one opened now)
			kit = (OWLEditorKit)manager.getEditorKitManager().getEditorKits().get(manager.getEditorKitManager().getEditorKitCount() - 1);
			
			// indicate the super class to use the ontology loaded by protege
			this.setLastOWLOntology(kit.getOWLModelManager().getActiveOntology());
			
			try {
				Debug.println(this.getClass(), "Ontology loaded by Protege : " + kit.getOWLModelManager().getActiveOntology());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("Could not use protege's ontology loader. Using OWLAPI instead...");
		}
		
		// load mebn using the super class
		MultiEntityBayesianNetwork ret =  super.loadMebn(file);
		
		// fill mebn with protege's storage implementor if we could load protege previously
		if (kit != null) {
			// set storage implementor as the protege's decorator
			try {
				ret.setStorageImplementor(ProtegeStorageImplementorDecorator.newInstance(kit));
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not update storage implementor.");
			}
			try {
				Debug.println(this.getClass(), "Storage implementor set to protege editor kit: " + kit);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		return ret;
	}

	/**
	 * This is a bundle laucher which launches protege using osgi.
	 * @return the protegeBundleLauncher. A non null value (it will lazily instantiate if none was specified)
	 */
	public IBundleLauncher getProtegeBundleLauncher() {
		if (protegeBundleLauncher == null) {
			protegeBundleLauncher = ProtegeBundleLauncher.getInstance();
		}
		return protegeBundleLauncher;
	}

	/**
	 * This is a bundle laucher which launches protege using osgi.
	 * @param protegeBundleLauncher the protegeBundleLauncher to set
	 */
	public void setProtegeBundleLauncher(IBundleLauncher protegeBundleLauncher) {
		this.protegeBundleLauncher = protegeBundleLauncher;
	}
	
	

}
