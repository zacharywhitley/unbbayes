/**
 * 
 */
package unbbayes.prs.prm.builders;

import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.PRMClass;

/**
 * 
 * Default builder for {@link PRMClass}
 * @author Shou Matsumoto
 *
 */
public class PRMClassBuilder implements IPRMClassBuilder {

	private int counter = 0;
	
	private String name = "NewPRMEntity";
	
	/**
	 * Default constructor 
	 */
	public PRMClassBuilder() {}
	
	/**
	 * Default constructor initializing fields.
	 * @param name : the default name for new PRMClass
	 * @param counter : counter to be appended to name for new PRMClass
	 */
	public PRMClassBuilder(String name, int counter) {
		this.counter = counter;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.builders.IPRMClassBuilder#buildPRMClass(unbbayes.prs.prm.IPRM)
	 */
	public IPRMClass buildPRMClass(IPRM prm) {
		this.counter++;
		return PRMClass.newInstance(prm, this.getName() + this.getCounter());
	}
	

	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
