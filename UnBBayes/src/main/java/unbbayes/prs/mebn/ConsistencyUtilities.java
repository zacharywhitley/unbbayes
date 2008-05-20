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
package unbbayes.prs.mebn;

public class ConsistencyUtilities {

	public static boolean hasCycle(ResidentNode origin, ResidentNode destination){
		
		/* Caso trivial */
		/*
		if(origin == destination){
			return true; 
		}
		
		for (DomainResidentNode teste: origin.getResidentNodeFatherList()){
			if (hasCycle(teste, destination)){
				return true; 
			}
		}
		
		for(GenerativeInputNode teste: origin.getInputNodeFatherList()){
			if (hasCycle(teste, destination)){
				return true; 
			}
		}
		*/
		
		return false; 
	}

	/**
	 * Verifies if exists a cycle from the origin to destination. 
	 * 
	 * @param origin
	 * @param destination
	 * @return
	 */
	public static  boolean hasCycle(InputNode origin, ResidentNode destination){
		/*
		if(origin.getInputInstanceOf() != null){
			if(origin.getInputInstanceOf() instanceof ResidentNode){
				
				if (hasCycle((DomainResidentNode)(origin.getInputInstanceOf()), destination)){
					return true; 
				}
				
			}
		}
		*/
		return false; 
	}
	
}
