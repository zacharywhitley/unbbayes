package unbbayes.datamining.datamanipulation;

import java.io.*;
import unbbayes.controller.IProgress;

/**
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (20/06/2003)
 */
public abstract class Saver implements IProgress{
  protected InstanceSet instances;
  protected int numInstances = 0;

  protected abstract void writeHeader() throws IOException;

  public abstract boolean setInstance() throws IOException;

  protected abstract boolean setInstanceFull() throws IOException;

  public boolean next()
  {
          boolean result = false;
          try
          {
                  result = setInstance();
          }
          catch(IOException ioe)
          {
                  result = false;
          }
          return result;
  }

  public void cancel()
  {
          //instances=null;
  }
  public int maxCount()
  {
          return numInstances;
  }


}