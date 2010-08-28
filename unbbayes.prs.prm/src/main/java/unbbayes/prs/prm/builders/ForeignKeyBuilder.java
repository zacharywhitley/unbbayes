/**
 * 
 */
package unbbayes.prs.prm.builders;

import unbbayes.prs.prm.ForeignKey;
import unbbayes.prs.prm.IForeignKey;

/**
 * Builds an instance of {@link IForeignKey}
 * @author Shou Matsumoto
 *
 */
public class ForeignKeyBuilder implements IForeignKeyBuilder {

	private String name = "FK";
	private static int count = 0;	// TODO avoid static
	
	/**
	 * Default constructor
	 */
	public ForeignKeyBuilder() {}
	
	/**
	 * Constructor using fields
	 * @param name
	 * @param count
	 */
	public ForeignKeyBuilder(String name, int count) {
		this();
		this.name = name;
		this.count = count;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.builders.IForeignKeyBuilder#buildForeignKey()
	 */
	public IForeignKey buildForeignKey() {
		this.count++;
		IForeignKey ret = ForeignKey.newInstance();
		ret.setName(this.getName() + this.getCount());
		return ret;
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

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

}
