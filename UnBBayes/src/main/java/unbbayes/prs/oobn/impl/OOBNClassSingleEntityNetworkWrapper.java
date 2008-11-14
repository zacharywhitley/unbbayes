/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.util.Set;

import unbbayes.prs.Network;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.exception.OOBNException;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNClassSingleEntityNetworkWrapper extends BasicOOBNClass {

	private SingleEntityNetwork wrapped = null;
	
	/**
	 * 
	 */
	protected OOBNClassSingleEntityNetworkWrapper(SingleEntityNetwork wrapped) {
		// TODO Auto-generated constructor stub
		super(wrapped.getName());
		this.wrapped = wrapped;
	}

	public static OOBNClassSingleEntityNetworkWrapper newInstance (SingleEntityNetwork wrapped) {
		return new OOBNClassSingleEntityNetworkWrapper(wrapped);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.BasicOOBNClass#getNetwork()
	 */
	@Override
	public Network getNetwork() {
		return this.getWrapped();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.BasicOOBNClass#setClassName(java.lang.String)
	 */
	@Override
	public void setClassName(String name) throws OOBNException {
		this.getWrapped().setName(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.BasicOOBNClass#toString()
	 */
	@Override
	public String toString() {
		return this.wrapped.toString();
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.BasicOOBNClass#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try{
			if (this.getWrapped() == null) {
				return (this == obj) || obj == null;
			}
			return (this == obj) || this.getWrapped().equals(obj);
		} catch (Exception e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.impl.BasicOOBNClass#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getWrapped().getName();
	}

	/**
	 * @return the wrapped
	 */
	public SingleEntityNetwork getWrapped() {
		return wrapped;
	}

	/**
	 * @param wrapped the wrapped to set
	 */
	public void setWrapped(SingleEntityNetwork wrapped) {
		this.wrapped = wrapped;
	}
	
	
	
	
	

}
