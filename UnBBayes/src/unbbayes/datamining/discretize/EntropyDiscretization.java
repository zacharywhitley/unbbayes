package unbbayes.datamining.discretize;

import unbbayes.datamining.datamanipulation.*;

/** Faz discretizacao por alcance*/
public class EntropyDiscretization implements IDiscretization
{	private int numThresholds;
	private InstanceSet inst;

	public EntropyDiscretization(InstanceSet inst)
	{	this.inst = new InstanceSet(inst);
	}

	public void discretizeAttribute(Attribute att)
	{	discretizeAttribute(att,10);
	}

	public void discretizeAttribute(Attribute att,int numThresholds)
	{	if (!att.isNumeric())//garante que o atributo seja numerico
		{	throw new IllegalArgumentException("Attribute not numeric");
		}
		int numInstances = inst.numInstances();// se o numero de instancias for 0 nao ha nada a fazer
		if (numInstances == 0)
		{	return;
		}
		if (numThresholds < 1)
			numThresholds = 1;
		try
		{	/*int position = att.getIndex();
			Attribute newAttribute = new Attribute(att.getAttributeName(),null,Attribute.NOMINAL,position);
			//pega os valores do atributo
			float[] values = new float[numInstances];
          	byte[] classes = new byte[numInstances];
			Enumeration enumInst = inst.enumerateInstances();
          	int i=0,j=0;
          	while (enumInst.hasMoreElements())
          	{   Instance instance = (Instance)enumInst.nextElement();
              	values[i] = Float.parseFloat(instance.stringValue(att));
				classes[i] = instance.classValue();
              	i++;
          	}
			insertionSortInc(values,classes);

			int numChanged = 0;
			for(i=0; i<(values.length - 1); i++)
				if (classes[i] != classes[i + 1])
					numChanged++;

			float[] changed = new float[numChanged];
			for(i=0; i<(values.length - 1); i++)
				if (classes[i] != classes[i + 1])
				{	changed[j] = (values[i] + values[i + 1])/2;
					j++;
				}

			for(i=0; i<(changed.length); i++)
				System.out.println(changed[i]);
			//divide o valor maximo pelo numero de thresholds e cria os valores do atributo
			int maxIndex = Utils.maxIndex(values);
			double ranges = (double)(values[maxIndex]/numThresholds);
            for (i=0; i<numThresholds; i++)
            {   DecimalFormat df = new DecimalFormat("0.0#");
                DecimalFormatSymbols dfs = new DecimalFormatSymbols();
				dfs.setDecimalSeparator('.');
				df.setDecimalFormatSymbols(dfs);
                newAttribute.addValue(df.format(i*ranges)+"to"+df.format((i + 1)*ranges));
			}
			//insere o novo atributo nominal no lugar do antigo
			inst.setAttributeAt(newAttribute,position);
			//seta os valores das instancias com o novo atributo
			for (i=0; i<numInstances; i++)
			{	byte newValue = (byte)Math.abs(values[i] / ranges);
				if (newValue == numThresholds)
					newValue--;
				inst.getInstance(i).setValue(position,newValue);
			}*/
			Id3Utils utils = new Id3Utils();
			System.out.println(utils.computeInfoGain(inst,att));
		}
		catch (Exception e)
		{	throw new IllegalArgumentException("Attribute not found in InstanceSet");
		}
	}

	public void autoDiscretize()
	{	autoDiscretize(10);
	}

	public void autoDiscretize(int numThresholds)
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

	private void insertionSortInc(float[] a,byte[] b)
	{	int i;
		float key;
		byte temp;
		for(int j=1; j<a.length; j++)
		{	key = a[j];
			temp = b[j];
			//Insert a[j] into the sorted sequence a[1 .. j-1]
			i = j - 1;
			while ((i > -1) && (a[i] > key))
			{	a[i + 1] = a[i];
				b[i + 1] = b[i];
				i--;
			}
			a[i + 1] = key;
			b[i + 1] = temp;
		}
	}
}
