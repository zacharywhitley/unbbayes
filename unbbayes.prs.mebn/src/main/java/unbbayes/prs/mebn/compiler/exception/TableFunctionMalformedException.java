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
package unbbayes.prs.mebn.compiler.exception;

import java.util.ResourceBundle;

import unbbayes.prs.mebn.exception.MEBNException;

public class TableFunctionMalformedException extends MEBNException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4621101714330356143L;

	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.prs.mebn.compiler.resources.Resources.class.getName());

	
	public TableFunctionMalformedException() {
		super(resource.getString("UnexpectedTokenFound"));
	}
	
	public TableFunctionMalformedException(String msg) {
		super(msg + " : " + resource.getString("UnexpectedTokenFound"));
	}
	
	public TableFunctionMalformedException(Throwable t) {
		super(resource.getString("UnexpectedTokenFound"),t);
	}
	
	public TableFunctionMalformedException(String msg, Throwable t) {
		super(msg + " : " + resource.getString("UnexpectedTokenFound"),t);
	}

}
