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
	{	if (!att.isNumeric())//garante que o atributo seja numérico
		{	throw new IllegalArgumentException("Attribute not numeric");
		}
		int numInstances = inst.numInstances();// se o numero de instancias for 0 não há nada a fazer
		if (numInstances == 0)
		{	return;
		}
		if (numThresholds < 1)
			numThresholds = 1;
		try
		{	int position = att.getIndex();
		Attribute newAttribute = new Attribute(att.getAttributeName(),
				Attribute.NOMINAL,
				false,
				att.numValues(),
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
}