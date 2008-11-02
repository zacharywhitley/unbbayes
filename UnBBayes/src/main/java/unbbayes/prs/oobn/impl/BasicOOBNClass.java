/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.util.Set;

import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.exception.OOBNException;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class BasicOOBNClass extends SingleEntityNetwork implements IOOBNClass {

	/**
	 * @param name
	 */
	private BasicOOBNClass(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor method for BasicOOBNClass,
	 * a simplified implementation of IOOBNClass
	 * @param name: name/title of the oobn class
	 * @return a new instance of a oobn class
	 */
	public static BasicOOBNClass newInstance(String name) {
		return new BasicOOBNClass(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#getAllNodes()
	 */
	public Set<IOOBNNode> getAllNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			return super.equals(obj) || this.getName().equals(((BasicOOBNClass)obj).getName());
		} catch (Exception e) {
			// if conversion is throwing an exception, we assume they are not "compatible",
			// so, they are not "equal"
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Network#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#getClassName()
	 */
	public String getClassName() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IOOBNClass#setClassName(java.lang.String)
	 */
	public void setClassName(String name) throws OOBNException {
		// TODO implement name consistency check
		Debug.println(this.getClass(), "Name consistency check is not implemented yet.");
		if (name.contains("!")) {
			throw new OOBNException();
		}
		this.setName(name);
	}

	
	
	
	
	
	
	
	
	

}
