package unbbayes.datamining.datamanipulation.neuralmodel;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class CombinatorialNetwork {
  private Hashtable inputLayer;
  private Hashtable combinatorialLayer;
  private Hashtable outputLayer;

  public CombinatorialNetwork(Hashtable inputLayer, Hashtable combinatorialLayer, Hashtable outputLayer) {
    this.inputLayer = inputLayer;
    this.combinatorialLayer = combinatorialLayer;
    this.outputLayer = outputLayer;
  }

  public Hashtable getInputLayer(){
    return inputLayer;
  }

  public Hashtable getOutputLayer(){
    return outputLayer;
  }

  public Hashtable getCombinatorialLayer(){
    return combinatorialLayer;
  }
}