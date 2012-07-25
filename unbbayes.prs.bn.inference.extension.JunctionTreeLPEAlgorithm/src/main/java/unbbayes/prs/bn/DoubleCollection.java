
package unbbayes.prs.bn;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * This is a class which represents a dynamic array of Double, but with some optimizations
 * regarding implementations of {@link JunctionTreeAlgorithm}, compared to {@link java.util.List<Double>}
 * @author Shou Matsumoto
 * @see unbbayes.util.FloatCollection
 */
public class DoubleCollection implements java.io.Serializable {

	private static final long serialVersionUID = -5277642564323532851L;

	public static  int DEFAULT_SIZE = 0;
	
    public double data[];
    public int size;
    
    /** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.util.resources.UtilResources.class.getName(),
  			Locale.getDefault(),
  			DoubleCollection.class.getClassLoader());

    public DoubleCollection(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException(resource.getString("IllegalCapacityException") +
                                               initialCapacity);
        this.data = new double[initialCapacity];
    }

    public DoubleCollection() {
       this(DEFAULT_SIZE);
    }


    /**
     * Increases the capacity of this <tt>floatCollection</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public  void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
        	// some recent profiling indicates that for 200+ nodes, the time for running garbage collect here
        	// costs more than the overhead of calling System.arraycopy multiple times...
        	// hence, we should minimize allocating more than we need, instead of doing pre-allocation
            double oldData[] = data;
            int newCapacity = minCapacity;
            data = new double[newCapacity];
            System.arraycopy(oldData, 0, data, 0, size);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public  int size() {
        return size;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     */
    public double get(int index) {
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
    public  double set(int index, double element) {
        double oldValue = data[index];
        data[index] = element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt>
     */
    public  boolean add(double newElement) {
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
    public  void add(int index, double element) {
        ensureCapacity(size+1);
        System.arraycopy(data, index, data, index + 1,
                 size - index);
        data[index] = element;
        size++;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     */
    public  void remove(int index) {
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index+1, data, index,
                     numMoved);
        if (size > 0) {        
	        data[--size] = 0;
        }
    }
}