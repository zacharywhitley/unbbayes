package unbbayes.datamining.datamanipulation;

import java.io.*;

/**
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (20/06/2003)
 */
public class TxtSaver extends Saver{
  protected PrintWriter writer;
  private int numAttributes;
  private int counter = -1;
  private int[] selectedAttributes;
  private boolean counterAttribute;

  public TxtSaver(File output,InstanceSet instances, int[] selectedAttributes) throws IOException{
    this(output,instances,selectedAttributes,false);
  }

  public TxtSaver(File output,InstanceSet instances, int[] selectedAttributes,boolean counterAttribute) throws IOException{
    writer = new PrintWriter(new FileWriter(output),true);
    this.instances = instances;
    this.selectedAttributes = selectedAttributes;
    this.counterAttribute = counterAttribute;
    numInstances = instances.numInstances();
    numAttributes = selectedAttributes.length;
    writeHeader();
  }

  public TxtSaver(File output,InstanceSet instances) throws IOException{
    this(output,instances,false);
  }

  public TxtSaver(File output,InstanceSet instances,boolean counterAttribute) throws IOException{
    int[] selAtt = new int[instances.numAttributes()];
    for (int i=0;i<selAtt.length;i++)
    {
      selAtt[i] = i;
    }
    writer = new PrintWriter(new FileWriter(output),true);
    this.instances = instances;
    this.selectedAttributes = selAtt;
    this.counterAttribute = counterAttribute;
    numInstances = instances.numInstances();
    numAttributes = selectedAttributes.length;
    writeHeader();
  }

  protected void writeHeader() throws IOException
  {
    for (int i=0;i<numAttributes;i++)
    {
      writer.print(instances.getAttribute(selectedAttributes[i]).getAttributeName()+" ");
    }
    if (counterAttribute)
    {
      writer.print(instances.getCounterAttributeName());
    }
    writer.println();
  }

  protected boolean setInstanceFull() throws IOException
  {
    Instance instance = instances.getInstance(counter);
    for (int i=0;i<numAttributes;i++)
    {
      writer.print(instance.stringValue(selectedAttributes[i])+" ");
    }
    if (counterAttribute)
    {
      writer.print(instance.getWeight());
    }
    writer.println();
    return true;
  }

  public boolean setInstance() throws IOException
  {
    counter++;
    if (counter!=numInstances)
    {
      return setInstanceFull();
    }
    else
    {
      writer.flush();
      writer.close();
      return false;
    }
  }


}