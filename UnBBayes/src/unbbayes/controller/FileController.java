package unbbayes.controller;

import java.awt.Cursor;
import java.awt.Component;
import java.io.*;
import java.util.ResourceBundle;

import javax.help.*;
import javax.swing.*;

import unbbayes.datamining.datamanipulation.*;

public class FileController
{   /** Uma instância deste objeto */
    private static FileController singleton;
    private File selectedFile;
    private ResourceBundle resource;
    private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    private Timer activityMonitor;
    private ProgressMonitor progressDialog;
    
    //--------------------------------------------------------------//

    public File getCurrentDirectory()
    {   return fileChooser.getCurrentDirectory();
    }
    
    //--------------------------------------------------------------//

    public void setCurrentDirectory(File file)
    {   fileChooser.setCurrentDirectory(file);
    }
    
    //--------------------------------------------------------------//

    /** Construtor padrão. Só pode ser instanciado pelo método getInstance. */
    protected FileController()
    {   resource = ResourceBundle.getBundle("unbbayes.datamining.gui.naivebayes.resources.NaiveBayesResource");        
    }
    
    //--------------------------------------------------------------//

    /** Retorna uma instância deste objeto. Se o objeto já estiver instanciado retorna o
        objeto atual, senão retorna uma nova instância do objeto.
        @return Um objeto Options
    */
    public static FileController getInstance()
    {   if (singleton == null)
        {   singleton = new FileController();
        }
        return singleton;
    }
    
    //--------------------------------------------------------------//

    public void openHelp(Component component) throws Exception
    {   component.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        String className = component.getClass().getName();
        HelpSet set = null;
        if (className.equals("unbbayes.datamining.gui.InvokerMain"))
        {   set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Data_Mining.hs"));
        }
        else if (className.equals("unbbayes.datamining.gui.decisiontree.DecisionTreeMain"))
        {   set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Decision_Tree.hs"));
        }
        else if (className.equals("unbbayes.datamining.gui.evaluation.EvaluationMain"))
        {   set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Evaluation.hs"));
        }
        else if (className.equals("unbbayes.datamining.gui.naivebayes.NaiveBayesMain"))
        {   set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Naive_Bayes.hs"));
        }
        else if (className.equals("unbbayes.datamining.gui.preprocessor.PreprocessorMain"))
        {   set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Preprocessor.hs"));
        }
        else
        {   component.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            throw new Exception("HelpSet not found "+this.getClass().getName());
        }
        JHelp help = new JHelp(set);
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(help);
        f.setSize(500,400);
        f.setVisible(true);
        component.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    //--------------------------------------------------------------//
    
    public InstanceSet getInstanceSet(File f,Component component) throws Exception
    {   Loader loader;
        String fileName = f.getName();
        if (fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
        {   loader = new ArffLoader(f);
        }
        else if (fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
        {   loader = new TxtLoader(f);
        }
        else
        {   throw new IOException(resource.getString("fileExtensionException"));
        }

        new CompactFileDialog(loader,component);
        
		//starts loading and shows a status screen
		ProgressDialog progressDialog = new ProgressDialog (f, loader);
		boolean successStatus = progressDialog.load();
		
		InstanceSet inst = loader.getInstances();
        
        if ((loader instanceof TxtLoader)&&(inst!=null))
        {   
        	((TxtLoader)loader).checkNumericAttributes();
        }

        if(successStatus)
        {
        	return inst;
        }
        else
        {
        	return null;	
        }
    }
    
	//--------------------------------------------------------------//

	  public InstanceSet getInstanceSet(File f) throws Exception
	  {   Loader loader;
		  String fileName = f.getName();
		  if (fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
		  {   loader = new ArffLoader(f);
		  }
		  else if (fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
		  {   loader = new TxtLoader(f);
		  }
		  else
		  {   throw new IOException(resource.getString("fileExtensionException"));
		  }
        
		  while (loader.getInstance())
		  {}

		  if (loader instanceof TxtLoader)
		  {   ((TxtLoader)loader).checkNumericAttributes();
		  }

		  return loader.getInstances();		  
	  }
}
