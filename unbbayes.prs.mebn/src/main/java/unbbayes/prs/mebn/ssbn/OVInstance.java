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
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * Represents a ordinary variable instanciate for a entity 
 * 
 * @author Shou Matsumoto
 */
public class OVInstance {
	
	private OrdinaryVariable ov = null;
	private LiteralEntityInstance entity = null;
	
	private OVInstance() {
		super();
	}
	
	public static OVInstance getInstance(OrdinaryVariable ov , LiteralEntityInstance ei) {
		OVInstance ovi = new OVInstance();
		ovi.setOv(ov);
		ovi.setEntity(ei);
		return ovi;
	}
	
	/**
	 * @return the entity
	 */
	public LiteralEntityInstance getEntity() {
		return entity;
	}
	/**
	 * @param entity the entity to set
	 */
	private void setEntity(LiteralEntityInstance entity) {
		this.entity = entity;
	}
	/**
	 * @return the ov
	 */
	public OrdinaryVariable getOv() {
		return ov;
	}
	/**
	 * @param ov the ov to set
	 */
	private void setOv(OrdinaryVariable ov) {
		this.ov = ov;
	}
	
	/**
	 * Sample: 
	 * DistFromOwn((st,ST4)(tPrev,T0)) [F]  [id=10]P=true
	 * Name + OVInstance List + Finding + Id + Permanent
	 */
	public String toString(){
		String ret = "("; 
		
		if(this.getOv() != null){
			ret+= this.getOv().getName(); 
		}
		
		if(this.getEntity() != null){
			ret+="," + this.getEntity().getInstanceName(); 
		}
		
		ret+=")"; 
	
		return ret; 
	}

	/**
	 * Two OVInstances are equals if
	 * - its ov are of the same MFrag 
	 * - its ov have the same name
	 * - its entity instance have the same name
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof OVInstance) {
			OVInstance ovi = (OVInstance)arg0;
			if(ovi.getOv().getMFrag().equals(this.getOv().getMFrag())){
				if (ovi.getOv().getName().equals(this.getOv().getName()) ) {
					if (ovi.getEntity().getInstanceName().equals(this.getEntity().getInstanceName())) {
						return true;
					}
				}			
			}
		} 
		return false;
	}
	
	
	
}
