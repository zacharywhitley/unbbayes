package unbbayes.datamining.discretize;

/**
 * Wrapps instance of {@link IDiscretization}
 * so that {@link #toString()}
 * will return {@link IDiscretization#getName()}
 * @author Shou Matsumoto
 */
public class DiscretizationWrapper {
	
	private IDiscretization wrapped;
	
	public DiscretizationWrapper(IDiscretization wrapped) {
		this.setWrapped(wrapped);
	}

	/**
	 * @return the wrapped {@link IDiscretization}
	 */
	public IDiscretization getWrapped() {
		return wrapped;
	}
	
	/**
	 * @param wrapped
	 * the wrapped {@link IDiscretization} to set
	 */
	public void setWrapped(IDiscretization wrapped) {
		this.wrapped = wrapped;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getWrapped().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return super.equals(obj) || getWrapped().equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getWrapped().getName();
	}
	
}
