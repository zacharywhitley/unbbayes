/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.controller.mebn;

import javax.swing.ImageIcon;

/**
 * Created this MEBN specific icon controller in order to allow the addition 
 * of new icons without having to change UnBBayes core.
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 - (feature:3317031) First version adding MTheory view icon
 *
 */
public class IconController extends unbbayes.controller.IconController {
	
	private static final long serialVersionUID = -8636074503907649076L;

	private static IconController singleton;
	
	protected ImageIcon mTheoryViewIcon; 
	
	public static IconController getInstance() {
		if (singleton == null) {
			singleton = new IconController();
		}
		return singleton;
	}
	
	public ImageIcon getMTheoryViewIcon() {
		if (mTheoryViewIcon != null) {
			return mTheoryViewIcon;
		} else {
			mTheoryViewIcon = new ImageIcon(getClass().getResource(
					"/icons/mtheory-view.gif"));
			return mTheoryViewIcon;
		}
	}	

}
