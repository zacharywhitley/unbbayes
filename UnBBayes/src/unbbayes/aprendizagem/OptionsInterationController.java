/*
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
package unbbayes.aprendizagem;

public class OptionsInterationController {
	
	private TVariavel variable;
	private OptionsWindow frame;
	
	public OptionsInterationController(OptionsWindow frame, TVariavel variable){
		this.frame = frame;
		this.variable = variable;		
	}
	
	public void applyEvent(String text){		
		if(! text.equals("")){
            try{
                int max = Integer.parseInt(text);
                variable.setNumeroMaximoPais(max);
                frame.dispose();
            }catch (NumberFormatException e){
                System.out.println("Digite um inteiro valido");
            }
        }		
	}
	
	public void cancelEvent(){
		frame.dispose();				
	}

}
