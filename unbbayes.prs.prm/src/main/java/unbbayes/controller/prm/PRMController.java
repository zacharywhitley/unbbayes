/**
 * 
 */
package unbbayes.controller.prm;

import java.util.Collection;

import unbbayes.prs.Graph;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder;
import unbbayes.prs.prm.compiler.IPRMCompiler;
import unbbayes.prs.prm.compiler.PRMToBNCompiler;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This is basically a facade
 * @author Shou Matsumoto
 *
 */
public class PRMController implements IPRMController, IBNInferenceAlgorithmHolder {

	private IDatabaseController databaseController;
	private IPRMCompiler prmCompiler;
	
	private IInferenceAlgorithm bnInferenceAlgorithm;

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

	
	

}
