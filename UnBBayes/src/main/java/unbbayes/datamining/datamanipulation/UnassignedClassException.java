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

/**
 * <code>UnassignedClassException</code> is used when
 * a method requires access to the Attribute designated as 
 * the class attribute in a set of Instances, but the Instances does not
 * have any class attribute assigned (such as by setClassIndex()).
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class UnassignedClassException extends RuntimeException 
{ 
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
	/**
   * Creates a new <code>UnassignedClassException</code> instance
   * with no detail message.
   */
  public UnassignedClassException() 
  { super(); 
  }

  /**
   * Creates a new <code>UnassignedClassException</code> instance
   * with a specified message.
   *
   * @param message a <code>String</code> containing the message.
   */
  public UnassignedClassException(String message) 
  { super(message); 
  }
}