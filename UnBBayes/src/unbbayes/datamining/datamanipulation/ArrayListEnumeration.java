package unbbayes.datamining.datamanipulation;

import java.util.*;

/**
 *  Class for enumerating the ArrayList's elements.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class ArrayListEnumeration implements Enumeration
{	/** The counter. */
   	private int counter;

   	/** The arrayList. */
   	private ArrayList arrayList;

    /** Special element. Skipped during enumeration. */
    private int specialElement;

    /**
     * 	Constructs an enumeration.
     *
     * 	@param arrayList Vector which is to be enumerated
     */
    public ArrayListEnumeration(ArrayList arrayList)
	{	counter = 0;
      	this.arrayList = arrayList;
      	specialElement = -1;
    }

    /**
     * Constructs an enumeration with a special element.
     * The special element is skipped during the enumeration.
     *
     * @param arrayList the vector which is to be enumerated
     * @param special the index of the special element
     */
    public ArrayListEnumeration(ArrayList arrayList, int special)
	{	this.arrayList = arrayList;
      	specialElement = special;
      	if (special == 0)
		{	counter = 1;
      	}
		else
		{	counter = 0;
      	}
    }


    /**
     * Tests if there are any more elements to enumerate.
     *
     * @return true If there are some elements left
     */
    public final boolean hasMoreElements()
	{	if (counter < arrayList.size())
		{	return true;
      	}
      	return false;
    }

    /**
     * Returns the next element.
     *
     * @return the next element to be enumerated
     */
    public final Object nextElement()
	{	Object result = arrayList.get(counter);

      	counter++;
      	if (counter == specialElement)
		{	counter++;
      	}
      	return result;
    }
}