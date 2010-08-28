package unbbayes.prs.prm.builders;

import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.PRMClass;

public interface IPRMClassBuilder {

	/**
	 * Builds a new instance of {@link PRMClass} using 
	 * {@link #getName()} + {@link #getCounter()} as its name.
	 * @param prm : prm containing prm class
	 * @return
	 */
	public abstract IPRMClass buildPRMClass(IPRM prm);

}