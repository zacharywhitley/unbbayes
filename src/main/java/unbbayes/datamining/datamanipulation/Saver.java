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
package unbbayes.datamining.datamanipulation;

import java.io.IOException;

import unbbayes.controller.IProgress;

/**
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (20/06/2003)
 */
public abstract class Saver implements IProgress{
	protected InstanceSet instanceSet;
	protected int numInstances = 0;

	protected abstract void writeHeader() throws IOException;

	public abstract boolean setInstance() throws IOException;

	public boolean next() {
		boolean result = false;
		
		try {
			result = setInstance();
		}
		catch(IOException ioe) {
			result = false;
		}
		return result;
	}

	public void cancel() {
	}

	public int maxCount() {
		return numInstances;
	}
}