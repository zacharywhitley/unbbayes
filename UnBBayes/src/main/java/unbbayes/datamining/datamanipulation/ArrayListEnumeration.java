/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.datamanipulation;

import java.util.Enumeration;

/**
 *  Class for enumerating the ArrayList's elements.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class ArrayListEnumeration implements Enumeration
{	/** The counter. */
   	private int counter;

   	/** The array */
   	private Object[] array;

    /** Special element. Skipped during enumeration. */
    private int specialElement;

    /**
     * 	Constructs an enumeration.
     *
     * 	@param arrayList Vector which is to be enumerated
     */
    public ArrayListEnumeration(Object[] array)
	{	counter = 0;
      	this.array = array;
      	specialElement = -1;
    }

    /**
     * Constructs an enumeration with a special element.
     * The special element is skipped during the enumeration.
     *
     * @param arrayList the vector which is to be enumerated
     * @param special the index of the special element
     */
    public ArrayListEnumeration(Object[] array, int special)
	{	this.array = array;
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
	{	if (counter < array.length)
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
	{	Object result = array[counter];

      	counter++;
		if (counter == specialElement)
		{	counter++;
      	}
      	return result;
    }
}
