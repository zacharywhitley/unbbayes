package unbbayes.controlador;

import java.awt.Cursor;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.util.ResourceBundle;

import javax.help.*;
import javax.swing.*;

//import unbbayes.datamining.controller.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.fronteira.*;

public class FileController
{   /** Uma instância deste objeto */
    private static FileController singleton;
    private InstanceSet inst;
    private File selectedFile;
    private ResourceBundle resource;
    private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));

    public File getCurrentDirectory()
    {   return fileChooser.getCurrentDirectory();
    }

    public void setCurrentDirectory(File file)
    {   fileChooser.setCurrentDirectory(file);
    }

    /** Construtor padrão. Só pode ser instanciado pelo método getInstance. */
    protected FileController()
    {   resource = ResourceBundle.getBundle("unbbayes.datamining.gui.naivebayes.resources.NaiveBayesResource");

        // set up the timer action
        /*activityMonitor = new Timer(500,new ActionListener()
        {   public void actionPerformed(ActionEvent event)
            {   int current = fileThread.getCurrent();

                // show progress
                progressDialog.setProgress(current);

                // check if task is completed or canceled
                if (!fileThread.isAlive()||current == fileThread.getTarget()|| progressDialog.isCanceled())
                {   activityMonitor.stop();
                    progressDialog.close();
                    fileThread.interrupt();
                }
            }
        });*/
    }

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

    public InstanceSet setBaseInstancesFromFile(File f,Component component) throws Exception
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
        //fileThread = new FileActivityThread(loader);
        //fileThread.start();

        // launch progress dialog
        //progressDialog = new ProgressMonitor(component,"Loading File "+f.getName(),null, 0, fileThread.getTarget());

        // start timer
        //activityMonitor.start();

        //Thread.yield();
        while (loader.getInstance())
        {}

        if (loader instanceof TxtLoader)
        {   ((TxtLoader)loader).checkNumericAttributes();
        }

        inst = loader.getInstances();
        return inst;
    }

    private Timer activityMonitor;
    private ProgressMonitor progressDialog;
//    private FileActivityThread fileThread;
}