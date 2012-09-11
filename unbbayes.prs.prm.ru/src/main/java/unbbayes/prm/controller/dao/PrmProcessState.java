package unbbayes.prm.controller.dao;

/**
 * State of the prm process.
 *  
 * @author David Saldaña.
 */
public enum PrmProcessState {
	Partitioning,
	SelectorAttribute,
	ProbModel,
	Compile
}
