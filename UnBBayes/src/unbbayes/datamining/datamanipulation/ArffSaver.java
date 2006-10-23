package unbbayes.datamining.datamanipulation;

import java.io.*;

/**
 *
 *	@author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
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
