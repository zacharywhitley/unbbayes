/*
 * Created on 03/06/2003
 *
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
package ubs.persistence.model;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import ubs.util.Debug;

/**
 * @author Rommel Carvalho
 *
 * This classe is responsible for 
 */
public abstract class ModelBean implements EntityBean {

	private EntityContext context;

	// Access methods for persistent fields

	public abstract int getModelId();
	public abstract void setModelId(int id);

	public abstract String getName();
	public abstract void setName(String name);

	public abstract String getDefinition();
	public abstract void setDefinition(String definition);

	public abstract double getDescription();
	public abstract void setDescription(String description);

	// Access methods for relationship fields

	public abstract LocalDomain getDomain();
	public abstract void setDomain(LocalDomain domain);
	
	public abstract Collection getEvidences();
	public abstract void setEvidences(Collection evidences);

	// Select methods

	// TODO implent select methods if necessary

	// Business methods

	// TODO implent business methods if necessary	

	//	EntityBean  methods

	public String ejbCreate(
		int id,
		String name,
		String definition,
		String description)
		throws CreateException {

		Debug.print("ModelBean ejbCreate");
		setModelId(id);
		setName(name);
		setDefinition(definition);
		setDescription(description);
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbLoad()
	 */
	public void ejbLoad() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbRemove()
	 */
	public void ejbRemove()
		throws RemoveException, EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbStore()
	 */
	public void ejbStore() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
	 */
	public void setEntityContext(EntityContext arg0)
		throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#unsetEntityContext()
	 */
	public void unsetEntityContext() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

}
