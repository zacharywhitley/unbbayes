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
package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A object entity instance ordereable is a entity instance that 
 * have a predecessor. Are instances of Object Entities objects where
 * the isOrdereable property is true; 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ObjectEntityInstanceOrdereable extends ObjectEntityInstance{

	private ObjectEntityInstanceOrdereable prev; 
	private ObjectEntityInstanceOrdereable proc; 
	
	protected ObjectEntityInstanceOrdereable(String name, ObjectEntity instanceOf){
		super(name, instanceOf); 
	}

	public ObjectEntityInstanceOrdereable getPrev() {
		return prev;
	}

	public void setPrev(ObjectEntityInstanceOrdereable prev) {
		this.prev = prev;
	}

	public ObjectEntityInstanceOrdereable getProc() {
		return proc;
	}

	public void setProc(ObjectEntityInstanceOrdereable proc) {
		this.proc = proc;
	}
	
	public boolean equals(Object obj){ 
		if(obj instanceof ObjectEntityInstanceOrdereable){
			return this.getName().equals(((ObjectEntityInstanceOrdereable)obj).getName()); 
		}else{
			return false; 
		}
	}
	
	/*-------------------------------------------------------------------------*/
	/* Methods for managment the ordereable list of entities                   */
	/*-------------------------------------------------------------------------*/

	public static void upEntityInstance(ObjectEntityInstanceOrdereable entity) {
		
		// A B C D -> A C B D (entity is the "c" term)
		if(entity.getPrev()!=null){
			ObjectEntityInstanceOrdereable c = entity; 
			ObjectEntityInstanceOrdereable b = entity.getPrev();
			ObjectEntityInstanceOrdereable d = entity.getProc(); 
			
			c.setProc(b);
			
			if(b != null){
				if(b.getPrev() != null){
			       b.getPrev().setProc(c); //proc(a) = c
				}
			   c.setPrev(b.getPrev()); //prev(c) = a
			   b.setPrev(c);
			   b.setProc(d);
			}
			
			if(d != null){
				d.setPrev(b); //prev(d) = b
			}
		}
	}
	
	public static void downEntityInstance(ObjectEntityInstanceOrdereable entity) {
		// A B C D -> A C B D (entity is the "b" term)
		
			ObjectEntityInstanceOrdereable b = entity; 
			ObjectEntityInstanceOrdereable a = entity.getPrev();
			ObjectEntityInstanceOrdereable c = entity.getProc(); 
			
			if(c != null){
				if(a != null){
					a.setProc(c);
				}
				
				if(c!= null){
					c.setPrev(a);
					b.setProc(c.getProc());
					if(c.getProc() != null){
						c.getProc().setPrev(b);
					}
					c.setProc(b);
				}
				
				b.setPrev(c);
			}
	}
	
	public static void removeEntityInstanceOrdereableReferences(ObjectEntityInstanceOrdereable entity) {
		
		if (entity.getPrev() != null){
			entity.getPrev().setProc(entity.getProc());
			if(entity.getProc() != null){
				entity.getProc().setPrev(entity.getPrev());
			}
		}else{
			if(entity.getProc() != null){
				entity.getProc().setPrev(null);
			}
		}
		
	}
	
	public static List<ObjectEntityInstanceOrdereable> ordererList(Collection<ObjectEntityInstanceOrdereable> originalCollection){
		
		ArrayList<ObjectEntityInstanceOrdereable> finalList = new ArrayList<ObjectEntityInstanceOrdereable>(); 
		
		ObjectEntityInstanceOrdereable prev = null; 
		
		for(int i = 0; i < originalCollection.size(); i++){
			for(ObjectEntityInstanceOrdereable instance: originalCollection){
				if(instance.getPrev() == prev){
					finalList.add(instance);
					prev = instance; 
					break; 
				}
			}
		}
		
		return finalList; 
	}
	
}
