/*
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

package unbbayes;

import unbbayes.controller.MainController;

/**
 *  This class starts UnBBayes
 *
 *@author   Michael S. Onishi (mso@gmail.com)
 *@author 	Rommel N. Carvalho (rommel.carvalho@gmail.com)
 *@version    24 de Junho de 2001
 */

public class Main {
	
    /**
     *  Starts UnBBayes.
     */
    public static void main(String[] args) {
        new MainController();
    }
    
}
