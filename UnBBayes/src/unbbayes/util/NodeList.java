/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.util;

import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.id.DecisionNode;

/**
 * Classe que representa um array dinâmico do tipo <code>Node</code>.
 *
 * @author Michael
 * @author Rommel
 */
public final class NodeList implements java.io.Serializable {
    public static final int DEFAULT_SIZE = 30;

    private Node data[];
    private int size;
    
    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.util.resources.UtilResources");

    public NodeList(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException(resource.getString("IllegalCapacityException") +
                                               initialCapacity);
        this.data = new Node[initialCapacity];
    }

    public NodeList() {
       this(DEFAULT_SIZE);
    }
    
    public Object clone() {
    	NodeList cloned = new NodeList();
    	Node [] clonedData = new Node[size];
    	for (int i = 0; i < clonedData.length; i++) {
    		if (data[i] instanceof ProbabilisticNode) {
    			clonedData[i] = (ProbabilisticNode)((ProbabilisticNode)data[i]).clone();
    		} else if (data[i] instanceof DecisionNode) {
    			clonedData[i] = (DecisionNode)((DecisionNode)data[i]).clone();
    		} else {
    			System.out.println("IDIDIDIDID");
    		}
    	}
    	//System.arraycopy(data, 0, clonedData, 0, size);
    	//cloned = SetToolkit.clone(this);
    	//Node [] clonedData = cloned.getData();
    	//System.out.println(data.toString());
    	//System.out.println(clonedData.toString());
    	//double[] marginais;
    	//for (int i = 0; i < size; i++) {
    		//marginais = new double[((TreeVariable)clonedData[i]).getMarginais().length];
    		//System.arraycopy(((TreeVariable)clonedData[i]).getMarginais(), 0, marginais, 0, marginais.length);
    		//for (int j = 0; j < ((TreeVariable)clonedData[i]).getMarginais().length; j++) {
    		//	double teste = ((TreeVariable)clonedData[i]).getMarginais()[j];
    		//	marginais[j] = teste;
    		//}
    		//marginais[0] = .33;
    		//((TreeVariable)clonedData[i]).setMarginais(marginais);
    		//System.out.println(((TreeVariable)data[i]).toString());
    		//System.out.println(((TreeVariable)clonedData[i]).toString());
    		//System.out.println(((TreeVariable)data[i]).getMarginais().toString());
    		//System.out.println(((TreeVariable)clonedData[i]).getMarginais().toString());
    		//System.out.println(((TreeVariable)data[i]).getMarginais()[0]);
    	//}
    	cloned.setData(clonedData);
    	cloned.setSize(size);
    	return cloned;
    }


    /**
     * Increases the capacity of this <tt>NodeList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public final void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            Node oldData[] = data;
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity) {
               newCapacity = minCapacity;
            }
            data = new Node[newCapacity];
            System.arraycopy(oldData, 0, data, 0, size);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public final int size() {
        return size;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     */
    public final Node get(int index) {
        return data[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     */
    public final Node set(int index, Node element) {
        Node oldValue = data[index];
        data[index] = element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt>
     */
    public final boolean add(Node newElement) {
        ensureCapacity(size + 1);
        data[size++] = newElement;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws    IndexOutOfBoundsException if index is out of range
     *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
     */
    public final void add(int index, Node element) {
        /*
        if (index > size || index < 0) {
           throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
        }
        */
        ensureCapacity(size+1);
        System.arraycopy(data, index, data, index + 1,
                 size - index);
        data[index] = element;
        size++;
    }
    
    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the
     * specified Collection's Iterator.  The behavior of this operation is
     * undefined if the specified Collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified Collection is this list, and this
     * list is nonempty.)
     *
     * @param c the elements to be inserted into this list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     */
    public final boolean addAll(NodeList c) {
		int numNew = c.size();
		ensureCapacity(size + numNew);
	
		for (int i = 0; i < numNew; i++)
		    data[size++] = c.data[i];
	
		return numNew != 0;
    }
    
    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public final void clear() {
		// Let gc do its work
		for (int i = 0; i < size; i++)
		    data[i] = null;
	
		size = 0;
    }
    
    public final boolean removeAll(NodeList c) {
		boolean modified = false;
		for (int i = size-1; i >= 0; i--) {
		    if (c.contains(data[i])) {
		    	remove(i);		    	
				modified = true;
		    }
		}
		return modified;
    }
    
    public final boolean containsAll(NodeList c) {    	
    	for (int i = 0; i < c.size; i++) {
		    if(! contains(c.data[i]))
				return false;	
    	}
		return true;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     */
    public final Node remove(int index) {
        Node oldValue = data[index];
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index+1, data, index,
                     numMoved);
        data[--size] = null;
        return oldValue;
    }
    
    
    /**
     * Removes a single instance of the specified element from this
     * collection, if it is presen. Returns <tt>true</tt> if the collection contained the
     * specified element (or equivalently, if the collection changed as a
     * result of the call).<p>
     *
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if the collection contained the specified
     *         element.
     */
   
    public final boolean remove(Object o) {
    	for (int i = 0; i < data.length; i++) {
			if (o.equals(data[i])) {
			    remove(i);
			    return true;
		    }
		}
		return false;
    }
    
    
    public final boolean retainAll(NodeList c) {
    	boolean modified = false;
    	for (int i = size-1; i >= 0; i--) {
    		if (! c.contains(data[i])) {
    			remove(i);
    			modified = true;
    		}
    	}
    	return modified;
    }


    
    
    /**
     * Searches for the first occurence of the given argument, testing 
     * for equality using the <tt>equals</tt> method. 
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    public final int indexOf(Object elem) {
        for (int i = 0; i < size; i++)
			if (elem.equals(data[i]))
			    return i;
		return -1;
    }
    
    
    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param elem element whose presence in this List is to be tested.
     */
    public final boolean contains(Object elem) {
		return indexOf(elem) >= 0;
    }
	/**
	 * Sets the data.
	 * @param data The data to set
	 */
	public final void setData(Node[] data) {
		this.data = data;
	}

	/**
	 * Sets the size.
	 * @param size The size to set
	 */
	public final void setSize(int size) {
		this.size = size;
	}

	/**
	 * Gets the data.
	 * @return Returns a Node[]
	 */
	public final Node[] getData() {
		return data;
	}

}