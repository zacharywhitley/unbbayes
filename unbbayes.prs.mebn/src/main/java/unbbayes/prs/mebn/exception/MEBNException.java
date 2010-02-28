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
package unbbayes.prs.mebn.exception;

public class MEBNException extends Exception{
    
	private static final long serialVersionUID = 6287201364470449002L;

	public MEBNException(String e){
    	super(e); 
    }

	public MEBNException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public MEBNException(){
		super(); 
	}
	
	public MEBNException(Exception e) {
		super(e);
	}

	public MEBNException(Throwable e) {
		super(e);
	}
	
}
