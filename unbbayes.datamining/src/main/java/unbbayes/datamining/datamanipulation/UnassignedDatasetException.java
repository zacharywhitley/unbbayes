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
 * <code>UnassignedDatasetException</code> is used when
 * a method of an Instance is called that requires access to
 * the Instance structure, but that the Instance does not contain
 * a reference to any Instances (as set by Instance.setDataset(), or when
 * an Instance is added to a set of Instances)).
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class UnassignedDatasetException extends RuntimeException 
{ 
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;			
	
  /**
   * Creates a new <code>UnassignedDatasetException</code> instance
   * with no detail message.
   */
  public UnassignedDatasetException() 
  { super(); 
  }

  /**
   * Creates a new <code>UnassignedDatasetException</code> instance
   * with a specified message.
   *
   * @param message a <code>String</code> containing the message.
   */
  public UnassignedDatasetException(String message) 
  { super(message); 
  }
}