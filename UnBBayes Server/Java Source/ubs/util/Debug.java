/*
 * Created on 08/05/2003
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
package ubs.util;

/**
 * @author Rommel Carvalho
 *
 * This classe is responsible for helping debug. It makes it easier to print 
 * out debug statements. The Debug.print statements are printed to System.err
 * if debuggingOn = true.
 */
public final class Debug {

	public static final boolean debuggingOn = true;

	public static final void print(String msg) {

		if (debuggingOn) {
			System.err.println("Debug: " + msg);
		}
	}

	public static final void print(String msg, Object object) {

		if (debuggingOn) {
			System.err.println("Debug: " + msg);
			System.err.println("       " + object.getClass().getName());
		}
	}

}
