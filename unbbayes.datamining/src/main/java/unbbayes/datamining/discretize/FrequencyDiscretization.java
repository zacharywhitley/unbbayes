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

/** faz discretizacao por frequencia */
public class FrequencyDiscretization implements IDiscretization
{	private int numThresholds;
	private InstanceSet inst;

	public FrequencyDiscretization(InstanceSet inst)
	{	this.inst = new InstanceSet(inst);
	}

	public void discretizeAttribute(Attribute att) throws Exception
	{	discretizeAttribute(att,10);
	}

	public void discretizeAttribute(Attribute att,int numThresholds) throws Exception
	{	if (!att.isNumeric())// garante que o atributo seja numerico
		{	throw new IllegalArgumentException("Attribute not numeric");
		}
		int numInstances = inst.numInstances();//se o numero de instancias for 0 nao ha nada a fazer
		if (numInstances == 0)
		{	return;
		}
		if (numThresholds < 1)
			numThresholds = 1;
		try
		{	int position = att.getIndex();//cria um novo atributo nominal
			Attribute newAttribute = new Attribute(att.getAttributeName(),
					Attribute.NOMINAL,
					true,
					numThresholds,
					position
					);
			// encontra todos os valores do atributo antigo
			float[] values = new float[numInstances];
          	Enumeration enumInst = inst.enumerateInstances();
          	int i=0,j=0;
          	while (enumInst.hasMoreElements())
          	{   Instance instance = (Instance)enumInst.nextElement();
              	values[i] = instance.getValue(att);
				i++;
          	}
			// soma todos os valores
			float sum = Utils.sum(values);
            // divide pelo numero de thresholds
			float result = sum / numThresholds;
			float[] values2 = new float[numInstances];
			System.arraycopy(values,0,values2,0,values.length);
			// retorna vetor com  os valores ordenados e soma iguais
			float[] changedValues = Utils.getDistribution(values);
            if (changedValues.length < numThresholds)
				throw new Exception("N�mero de thresholds maior que a frequencia dos valores");
			// ordena os valores e retorna vetor com as frequencias
			float[] freq = Utils.getFrequency(values);
			float[] breakPoint = new float[numThresholds];

			//cria os labels
			int inicialValue = 0;
            int actualValue = 0;
            for (i=0; i<numThresholds; i++)
            {   if (changedValues[actualValue] < result)
				{	double result2 = result;
                	while (result2 >= 0)
                	{   result2 -= changedValues[actualValue];
                    	if (result2 < 0)
                    	{   result2 += changedValues[actualValue];
                        	break;
                    	}
                    	actualValue++;
                	}
				}
				else
				{	actualValue++;
				}
				DecimalFormat df = new DecimalFormat("0.0#");
				DecimalFormatSymbols dfs = new DecimalFormatSymbols();
				dfs.setDecimalSeparator('.');
				df.setDecimalFormatSymbols(dfs);
                if (i != (numThresholds-1))
                {   breakPoint[i] = (changedValues[actualValue]/freq[actualValue]);
					newAttribute.addValue(df.format(changedValues[inicialValue]/freq[inicialValue])+"to"+df.format(breakPoint[i]));
                }
                else
                {	breakPoint[i] = values[(values.length - 1)];
					newAttribute.addValue(df.format(changedValues[inicialValue]/freq[inicialValue])+"to"+df.format(breakPoint[i]));
                }
                inicialValue = actualValue;
			}
			//insere o atributo
			inst.setAttributeAt(newAttribute,position);
			// insere os novos valores
			for (i=0; i<numInstances; i++)
			{	byte newValue = (byte)0;
				for (j=0; j<breakPoint.length; j++)
					if (values2[i] <= breakPoint[j])
					{	newValue = (byte)j;
						break;
					}
				inst.getInstance(i).setValue(position,newValue);
			}
		}
		catch (Exception e)
		{	throw new IllegalArgumentException(e.getMessage());
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
}