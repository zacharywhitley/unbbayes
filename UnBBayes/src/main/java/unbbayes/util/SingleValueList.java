/**
 * 
 */
package unbbayes.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This is simply a list containing multiple copies of the same element.
 * It is fundamentally equivalent to using {@link List#add(Object)} multiple times with the same argument.
 * @author Shou Matsumoto
 */
public class SingleValueList<E> implements List<E> {
	
	
	private E value = null;
	private int size = 0;

	/**
	 * Default constructor should not be visible.
	 * @see #SingleValueList(Object, int)
	 */
	@SuppressWarnings("unused")
	private SingleValueList() {}
	
	/**
	 * Constructor initializing fields.
	 * @param value
	 * @param size
	 */
	public SingleValueList(E value, int size) {
//		this();
		this.value = value;
		if (size < 0) {
			 throw new IndexOutOfBoundsException("Size: "+size);
		}
		this.size = size;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#size()
	 */
	public int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return size > 0 && value.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#iterator()
	 */
	public Iterator<E> iterator() {
		return new SingleValueListIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		Object[] ret =  new Object[size];
		Arrays.fill(ret, value);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		return (T[]) this.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(E e) {
		throw new UnsupportedOperationException("This is an immutable list containing only " + size + " copies of " + value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		if (this.contains(o)) {
			this.clear();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		boolean ret = true;
		for (Object object : c) {
			ret = ret && this.contains(object);
			if (!ret) {
				break;
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("This is an immutable list containing only " + size + " copies of " + value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException("This is an immutable list containing only " + size + " copies of " + value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		if (this.isEmpty()) {
			return false;
		}
		if (c.contains(value)) {
			this.clear();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		if (this.isEmpty()) {
			return false;	//there is nothing to do
		}
		if (!c.contains(value)) {
			this.clear();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#clear()
	 */
	public void clear() {
		value = null;
		size = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
	public E get(int index) {
		if (index >= size || index < 0) {
		    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public E set(int index, E element) {
		if (index >= size || index < 0) {
		    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
		}
		E oldValue = value;
		value = element;
		return oldValue;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, E element) {
		throw new UnsupportedOperationException("This is an immutable list containing only " + size + " copies of " + value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	public E remove(int index) {
		if (index >= size || index < 0) {
		    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
		}
		E oldValue = value;
		size--;
		if (size <= 0) {
			this.clear();
		}
		return oldValue;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		if (this.contains(o)) {
			return 0;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		if (this.contains(o)) {
			return size-1;
		}
		return -1;
	}

	/**
	 * This is just an iterator returned by {@link SingleValueList#iterator()},
	 * {@link SingleValueList#listIterator()}, and {@link SingleValueList#listIterator(int)}.
	 * It wrapps {@link SingleValueList}.
	 * @author Shou Matsumoto
	 */
	public class SingleValueListIterator implements ListIterator<E> {
		private int index = 0;
		
		public SingleValueListIterator() { this(0); }
		public SingleValueListIterator(int index) { this.index = index; }

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#hasNext()
		 */
		public boolean hasNext() {
			return index < size;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#next()
		 */
		public E next() {
			index++;
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#hasPrevious()
		 */
		public boolean hasPrevious() {
			return index > 0;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#previous()
		 */
		public E previous() {
			index--;
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#nextIndex()
		 */
		public int nextIndex() {
			return index;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#previousIndex()
		 */
		public int previousIndex() {
			return index;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#remove()
		 */
		public void remove() {
			SingleValueList.this.remove(index--);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		public void set(E e) {
			SingleValueList.this.set(0,e);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		public void add(E e) {
			SingleValueList.this.add(index,e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<E> listIterator() {
		return new SingleValueListIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<E> listIterator(int index) {
		return new SingleValueListIterator(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	public List<E> subList(int fromIndex, int toIndex) {
		if (fromIndex >= size || fromIndex < 0
				|| toIndex > size || toIndex < 0
				|| fromIndex > toIndex) {
		    throw new IndexOutOfBoundsException("From: "+fromIndex+ ", To: " + toIndex + ", Size: "+size);
		}
		return new SingleValueList<E>(value, toIndex-fromIndex);
	}

}
