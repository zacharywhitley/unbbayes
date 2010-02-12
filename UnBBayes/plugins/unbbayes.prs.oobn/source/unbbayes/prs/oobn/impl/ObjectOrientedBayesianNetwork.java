/**
 * 
 */
package unbbayes.prs.oobn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public class ObjectOrientedBayesianNetwork extends ProbabilisticNetwork implements
		IObjectOrientedBayesianNetwork {

	private NonDuplicatedArrayList<IOOBNClass> classesList = null; 

	protected ObjectOrientedBayesianNetwork() {
		super("OOBN");
	}

	/**
	 * @param name
	 */
	protected ObjectOrientedBayesianNetwork(String name) {
		super(name);
		this.setOOBNClassList(new NonDuplicatedArrayList<IOOBNClass>());
	}
	
	/**
	 * Generates a new instance of {@link ObjectOrientedBayesianNetwork}
	 * @param name: name/title of the oobn
	 * @return a new instance of {@link ObjectOrientedBayesianNetwork}
	 */
	public static ObjectOrientedBayesianNetwork newInstance(String name) {
		return new ObjectOrientedBayesianNetwork(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IObjectOrientedBayesianNetwork#getOOBNClassCount()
	 */
	public Integer getOOBNClassCount() {
		return this.getOOBNClassList().size();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IObjectOrientedBayesianNetwork#getOOBNOOBNClassList()
	 */
	public List<IOOBNClass> getOOBNClassList() {
		return this.classesList;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IObjectOrientedBayesianNetwork#getTitle()
	 */
	public String getTitle() {
		return super.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IObjectOrientedBayesianNetwork#setOOBNClassList(java.util.List)
	 */
	public void setOOBNClassList(List<IOOBNClass> oobnClasses) {
		this.classesList = new NonDuplicatedArrayList<IOOBNClass>(oobnClasses);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IObjectOrientedBayesianNetwork#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		super.setName(title);
	}
	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.IObjectOrientedBayesianNetwork#getSingleEntityNetwork()
	 */
	public SingleEntityNetwork getSingleEntityNetwork() {
		// TODO Auto-generated method stub
		return this;
	}






	private class NonDuplicatedArrayList<T> extends ArrayList<T>{
		static final long serialVersionUID = 0xA0L;
		
		NonDuplicatedArrayList() {
			super();
		}
		
		NonDuplicatedArrayList(Collection<T> c) {
			super();
			this.addAll(c);
		}
		
		/* (non-Javadoc)
		 * @see java.util.ArrayList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, T element) {
			if (!this.contains(element)) {
				super.add(index, element);
			} else {
				throw new IllegalArgumentException("[DUP]" + element.toString());
			}			
		}

		/* (non-Javadoc)
		 * @see java.util.ArrayList#add(java.lang.Object)
		 */
		@Override
		public boolean add(T e) {
			if (!this.contains(e)) {
				return super.add(e);
			} else {
				throw new IllegalArgumentException("[DUP]" + e.toString());
			}
		}

		/* (non-Javadoc)
		 * @see java.util.ArrayList#addAll(java.util.Collection)
		 */
		@Override
		public boolean addAll(Collection<? extends T> c) {
			if (c == null) {
				return false;
			}
			if (c.isEmpty()) {
				return false;
			}
			if (!this.containsAll(c)) {
				return super.addAll(c);
			} else {
				throw new IllegalArgumentException("[DUP]" + c.toString());
			}			
		}

		/* (non-Javadoc)
		 * @see java.util.ArrayList#addAll(int, java.util.Collection)
		 */
		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			if (c == null) {
				return false;
			}
			if (c.isEmpty()) {
				return false;
			}
			if (!this.containsAll(c)) {
				return super.addAll(index, c);
			} else {
				throw new IllegalArgumentException("[DUP]" + c.toString());
			}	
			
		}
		
	}

	
	
}
