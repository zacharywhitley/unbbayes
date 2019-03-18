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
package unbbayes.datamining.discretize;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Enumeration;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

/** Faz discretizacao por alcance*/
public class RangeDiscretization implements IDiscretization
{	private int numThresholds;
	private InstanceSet inst;

	public RangeDiscretization(InstanceSet inst)
	{	this.inst = new InstanceSet(inst);
	}

	public void discretizeAttribute(Attribute att) throws Exception
	{	discretizeAttribute(att,10);
	}

	public void discretizeAttribute(Attribute att,int numThresholds) throws Exception
	{	if (!att.isNumeric())//garante que o atributo seja num�rico
		{	throw new IllegalArgumentException("Attribute not numeric");
		}
		int numInstances = inst.numInstances();// se o numero de instancias for 0 n�o h� nada a fazer
		if (numInstances == 0)
		{	return;
		}
		if (numThresholds < 1)
			numThresholds = 1;
		try
		{	int position = att.getIndex();
		Attribute newAttribute = new Attribute(att.getAttributeName(),
				Attribute.NOMINAL,
				true,
				numThresholds,
				position
				);
			//pega os valores do atributo
			float[] values = new float[numInstances];
          	Enumeration enumInst = inst.enumerateInstances();
          	int i=0;
          	while (enumInst.hasMoreElements())
          	{   Instance instance = (Instance)enumInst.nextElement();
              	values[i] = instance.getValue(att);
              	i++;
          	}
			//divide o valor maximo pelo numero de thresholds e cria os valores do atributo
			int maxIndex = Utils.maxIndex(values);
			double min = Utils.min(values);
			double ranges = (double)((values[maxIndex]-min)/numThresholds);
            for (i=0; i<numThresholds; i++)
            {   DecimalFormat df = new DecimalFormat("0.0#");
                DecimalFormatSymbols dfs = new DecimalFormatSymbols();
				dfs.setDecimalSeparator('.');
				df.setDecimalFormatSymbols(dfs);
                newAttribute.addValue(df.format((i*ranges)+min)+"to"+df.format(((i + 1)*ranges)+min));
			}
			//insere o novo atributo nominal no lugar do antigo
			inst.setAttributeAt(newAttribute,position);
			//seta os valores das instancias com o novo atributo
			for (i=0; i<numInstances; i++)
			{	byte newValue = (byte)Math.abs((values[i]-min) / ranges);
				if (newValue == numThresholds)
					newValue--;
				inst.getInstance(i).setValue(position,newValue);
			}
		}
		catch (Exception e)
		{	throw new IllegalArgumentException("Attribute not found in InstanceSet "+e.getMessage());
		}
	}

	public void autoDiscretize() throws Exception
	{	autoDiscretize(10);
	}

	public void autoDiscretize(int numThresholds) throws Exception
	{	int numAttributes = inst.numAttributes();

		for (int i=0; i<numAttributes; i++)
		{	Attribute att = inst.getAttribute(i);
			if (att.isNumeric())
			{	discretizeAttribute(att,numThresholds);
			}
		}
	}

	public InstanceSet getInstances()
	{	return inst;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretization#getName()
	 */
	public String getName() {
		return "Range";
	}
}