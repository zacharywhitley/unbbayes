/*
 * Created on 29/05/2003
 *
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
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
package ubs.persistence.user;

import java.util.Collection;

import javax.ejb.EJBLocalObject;
import javax.ejb.FinderException;

/**
 * @author Rommel Carvalho
 *
 * This classe is responsible for 
 */
public interface LocalUser extends EJBLocalObject {

	public int getUserId();
	public String getName();
	public String getPhone();
	public String getEmail();
	public String getSex();
	public LocalGroup getGroup();
	public Collection getCases() throws FinderException;
	public Collection getEvidences() throws FinderException;

}
