package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class Neuron implements Serializable{
  protected String key;

  public String getKey(){
    return key;
  }

  public abstract void prunning(String key);

  public abstract boolean getSignal();
}