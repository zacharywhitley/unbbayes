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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 *	@author Mario Henrique Paes Vieira (mariohpv@bol.com.br)
 *	@version $1.0 $ (20/06/2003)
 */
public class ArffSaver extends Saver{
	protected PrintWriter writer;
	private int numAttributes;
	private int counter = -1;
	private int[] selectedAttributes;
	private boolean counterAttribute;

	public ArffSaver(File output, InstanceSet instanceSet,
			int[] selectedAttributes) throws IOException{
		this(output, instanceSet, selectedAttributes,false);
	}

	public ArffSaver(File output, InstanceSet instanceSet,
			int[] selectedAttributes, boolean counterAttribute)
			throws IOException{
		writer = new PrintWriter(new FileWriter(output), true);
		this.selectedAttributes = selectedAttributes;
		this.instanceSet = instanceSet;
		this.counterAttribute = counterAttribute;
		numInstances = instanceSet.numInstances();
		numAttributes = selectedAttributes.length;
		writeHeader();
	}

	public ArffSaver(File output, InstanceSet instanceSet) throws IOException{
		this(output, instanceSet, false);
	}

	public ArffSaver(File output, InstanceSet instanceSet,
			boolean counterAttribute) throws IOException{
		int numSelAtt = instanceSet.numAttributes();
		int[] selAtt = new int[numSelAtt];
		
		for (int i = 0; i < numSelAtt; i++) {
			selAtt[i] = i;
		}
		writer = new PrintWriter(new FileWriter(output), true);
		this.instanceSet = instanceSet;
		this.selectedAttributes = selAtt;
		this.counterAttribute = counterAttribute;
		numInstances = instanceSet.numInstances();
		numAttributes = selectedAttributes.length;
		writeHeader();
	}

	protected void writeHeader() throws IOException	{
		writer.print("@relation " + instanceSet.getRelationName() + "\n\n");
		for (int i = 0; i < numAttributes; i++) {
			writer.print(instanceSet.getAttribute(selectedAttributes[i]) + "\n");
		}
		if (counterAttribute) {
			writer.print("@attribute "+instanceSet.getCounterAttributeName() +
					" numeric\n");
		}
		writer.print("\n@data\n");
	}

	public boolean setInstance() throws IOException	{
		counter++;
		if (counter != numInstances) {
			Instance instance = instanceSet.getInstance(counter);
			for (int i = 0; i < numAttributes; i++) {
				if (i > 0) writer.print(",");
				writer.print(instance.stringValue(selectedAttributes[i]));
			}
			if (counterAttribute) {
				writer.print("," + instance.getWeight());
			}
			writer.println();
			return true;
		} else {
			writer.flush();
			writer.close();
			return false;
		}
	}

}
