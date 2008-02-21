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
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;

public class CompactInterationController {
    
     
    private  CompactFileWindow frame;	
    
	public CompactInterationController(CompactFileWindow frame){
		this.frame = frame;
	}
	
	public void actionYes(NodeList variablesVector){
		frame.dispose();
		new CompactChooseWindow(variablesVector);				
	}
	
	public void actionNo(){
	    frame.dispose();			
	}

}
