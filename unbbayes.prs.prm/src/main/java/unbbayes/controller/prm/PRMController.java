/**
 * 
 */
package unbbayes.controller.prm;

import java.util.Collection;
import java.util.EventObject;

import unbbayes.io.BaseIO;
import unbbayes.io.extension.jpf.PluginAwareFileExtensionIODelegator;
import unbbayes.io.prm.DefaultSQLPRMIO;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder;
import unbbayes.prs.prm.compiler.IPRMCompiler;
import unbbayes.prs.prm.compiler.PRMToBNCompiler;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This is basically a facade
 * @author Shou Matsumoto
 *
 */
public class PRMController implements IPRMController, IBNInferenceAlgorithmHolder {

	private IDatabaseController databaseController;
	private IPRMCompiler prmCompiler;
	
	private IInferenceAlgorithm bnInferenceAlgorithm;

	private BaseIO defaultIO;
	
	/*-------------------------------------------------------------------------*/
	/* Extensions                                                              */
	/*-------------------------------------------------------------------------*/
	
	private String prmIOExtensionPointID = "PRMIO";
	private String prmModulePluginID = "unbbayes.prs.prm";
	
	/**
	 * At least one constructor must be visible to subclasses in
	 * order to allow inheritance
	 */
	protected PRMController() {
		super();
				
		this.prmCompiler = PRMToBNCompiler.newInstance();
		
		// use the same BN inference algorithm between compiler and this controller
		if (this.prmCompiler instanceof IBNInferenceAlgorithmHolder) {
			this.bnInferenceAlgorithm = ((IBNInferenceAlgorithmHolder)this.prmCompiler).getBNInferenceAlgorithm();
			if (this.bnInferenceAlgorithm == null) {
				// if the compiler is not initializing algorithm, I'll do it myself!
				this.bnInferenceAlgorithm = new JunctionTreeAlgorithm();
				((IBNInferenceAlgorithmHolder)this.prmCompiler).setBNInferenceAlgorithm(this.bnInferenceAlgorithm);
			}
		} else {
			// by the way, initialize the algorithm
			this.bnInferenceAlgorithm = new JunctionTreeAlgorithm();
		}
		
		// initialize IO
		// initialize plugin-aware IO with some attribute customization
        this.setIO(this.reloadPluginIO());
        
        // adding a listener to reload IO if plugin reload action is triggered
		UnBBayesPluginContextHolder.newInstance().addListener(new UnBBayesPluginContextHolder.OnReloadActionListener() {
			public void onReload(EventObject arg0) {
				setIO(reloadPluginIO());
			}
		});
	}
	
	/**
	 * This method uses {@link #getPRMIOExtensionPointID()} and
	 * {@link #getPRMModulePluginID()} in order to set up a new instance
	 * of {@link PluginAwareFileExtensionIODelegator} as PRM's default
	 * IO component.
	 * @return : the generated new IO component.
	 */
	protected BaseIO reloadPluginIO() {

		// instantiate the new IO
		PluginAwareFileExtensionIODelegator ioDelegator = PluginAwareFileExtensionIODelegator.newInstance(false);
		
		// customize the attributes
		ioDelegator.setCorePluginID(this.getPrmModulePluginID());
		ioDelegator.setExtensionPointID(this.getPrmIOExtensionPointID());
		
		// reload plugins
		ioDelegator.reloadPlugins();
		
		// If no plugin was loaded, add a default IO
		if (ioDelegator.getDelegators().isEmpty()) {
			ioDelegator.getDelegators().add(DefaultSQLPRMIO.getInstance());
		}
		
		return ioDelegator;
	}

	/**
	 * Default factory method
	 * @return
	 */
	public static PRMController newInstance(){
		return new PRMController();
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.prm.IPRMController#getDatabaseController()
	 */
	public IDatabaseController getDatabaseController() {
		return databaseController;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.prm.IPRMController#getPRMCompiler()
	 */
	public IPRMCompiler getPRMCompiler() {
		return this.prmCompiler;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.prm.IPRMController#setDatabaseController(unbbayes.controller.prm.IDatabaseController)
	 */
	public void setDatabaseController(IDatabaseController dbController) {
		this.databaseController = dbController;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.prm.IPRMController#setPRMCompiler(unbbayes.prs.prm.compiler.IPRMCompiler)
	 */
	public void setPRMCompiler(IPRMCompiler prmCompiler) {
		this.prmCompiler = prmCompiler;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.prm.IPRMController#compilePRM(unbbayes.prs.prm.IPRM, java.util.Collection)
	 */
	public Graph compilePRM(IPRM prm, Collection<IAttributeValue> query) {
		if (this.getPRMCompiler() != null) {
			return this.getPRMCompiler().compile(this.getDatabaseController(), prm, query);
		}
		throw new IllegalStateException(
				"The PRM compiler is not initialized. Use " 
				+ this.getClass().getName() + ".setPRMCOmpiler(" 
				+ IPRMCompiler.class.getName()
				+ ") to set it up.");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder#getBNInferenceAlgorithm()
	 */
	public IInferenceAlgorithm getBNInferenceAlgorithm() {
		return this.bnInferenceAlgorithm;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder#setBNInferenceAlgorithm(unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 */
	public void setBNInferenceAlgorithm(IInferenceAlgorithm inferenceAlgorithm) {
		this.bnInferenceAlgorithm = inferenceAlgorithm;
	}

	/**
	 * @return the defaultIO
	 */
	public BaseIO getIO() {
		return defaultIO;
	}

	/**
	 * @param io the defaultIO to set
	 */
	public void setIO(BaseIO io) {
		defaultIO = io;
	}

	/**
	 * @return the prmIOExtensionPointID
	 */
	public String getPrmIOExtensionPointID() {
		return prmIOExtensionPointID;
	}

	/**
	 * @param prmIOExtensionPointID the prmIOExtensionPointID to set
	 */
	public void setPrmIOExtensionPointID(String prmIOExtensionPointID) {
		this.prmIOExtensionPointID = prmIOExtensionPointID;
	}

	/**
	 * @return the prmModulePluginID
	 */
	public String getPrmModulePluginID() {
		return prmModulePluginID;
	}

	/**
	 * @param prmModulePluginID the prmModulePluginID to set
	 */
	public void setPrmModulePluginID(String prmModulePluginID) {
		this.prmModulePluginID = prmModulePluginID;
	}


	
	

}
