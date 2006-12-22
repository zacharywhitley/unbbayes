package unbbayes.controller;

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import unbbayes.datamining.datamanipulation.ArffSaver;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.Saver;
import unbbayes.datamining.datamanipulation.TxtSaver;
import unbbayes.datamining.gui.datamanipulation.AttributeTypeChooserController;

public class FileController
{	 /** Uma instância deste objeto */
	private static FileController singleton;
	private ResourceBundle resource;
	private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));

	//--------------------------------------------------------------//

	public File getCurrentDirectory()
	{	 return fileChooser.getCurrentDirectory();
	}

	//--------------------------------------------------------------//

	public void setCurrentDirectory(File file)
	{	 fileChooser.setCurrentDirectory(file);
	}

	//--------------------------------------------------------------//

	/** Construtor padrão. Só pode ser instanciado pelo método getInstance. */
	protected FileController()
	{	 resource = ResourceBundle.getBundle("unbbayes.datamining.gui.naivebayes.resources.NaiveBayesResource");
	}

	//--------------------------------------------------------------//

	/** Retorna uma instância deste objeto. Se o objeto já estiver instanciado retorna o
		objeto atual, senão retorna uma nova instância do objeto.
		@return Um objeto Options
	*/
	public static FileController getInstance()
	{	 if (singleton == null)
		{	 singleton = new FileController();
		}
		return singleton;
	}

	//--------------------------------------------------------------//

	public void openHelp(Component component) throws Exception
	{	 component.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		String className = component.getClass().getName();
		HelpSet set = null;
		if (className.equals("unbbayes.datamining.gui.InvokerMain"))
		{	 set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Data_Mining.hs"));
		}
		else if (className.equals("unbbayes.datamining.gui.id3.DecisionTreeMain"))
		{	 set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Decision_Tree.hs"));
		}
				else if (className.equals("unbbayes.datamining.gui.c45.DecisionTreeMain"))
				{	 set = new HelpSet(null, getClass().getResource("/help/C45Help/C45.hs"));
				}
				else if (className.equals("unbbayes.datamining.gui.evaluation.EvaluationMain"))
		{	 set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Evaluation.hs"));
		}
		else if (className.equals("unbbayes.datamining.gui.naivebayes.NaiveBayesMain"))
		{	 set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Naive_Bayes.hs"));
		}
		else if (className.equals("unbbayes.datamining.gui.preprocessor.PreprocessorMain"))
		{	 set = new HelpSet(null, getClass().getResource("/help/DataMiningHelp/Preprocessor.hs"));
		}
		else if (className.equals("unbbayes.datamining.gui.neuralmodel.NeuralModelMain"))
		{	 set = new HelpSet(null, getClass().getResource("/help/CNMHelp/cnm.hs"));
		}
				else if (className.equals("unbbayes.datamining.gui.neuralnetwork.NeuralNetworkMain"))
				{	 set = new HelpSet(null, getClass().getResource("/help/BpnHelp/BpnHelp.hs"));
				}
				else if (className.equals("unbbayes.gui.UnBBayesFrame"))
				{	 set = new HelpSet(null, getClass().getResource("/help/JUnBBayes.hs"));
				}
		else
		{
					component.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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

	public InstanceSet getInstanceSet(File file, Component component) throws Exception {
		/* Get the attributes' type information */
		AttributeTypeChooserController attributeTypeChooser;
		attributeTypeChooser = new AttributeTypeChooserController(file, component);
		Loader loader = attributeTypeChooser.getLoader();
		
		/* Check if the user cancelled the opening operation */
		if (loader == null) {
			return null;
		}
		
		/* Starts loading and shows a status screen */
		String fileName = file.getName();
		ProgressDialog progressDialog = new ProgressDialog (fileName, loader);
		boolean successStatus = progressDialog.load();

		InstanceSet inst = loader.getInstanceSet();

		if (successStatus) {
			return inst;
		} else {
			return null;
		}
	}

	//--------------------------------------------------------------//

	public void saveInstanceSet(File output, InstanceSet instanceSet,
			int[] selectedAttributes) throws IOException {
		Saver saver;
		String fileName = output.getName();
		boolean compacted;
		
		if (instanceSet.counterIndex == -1) {
			compacted = false;
		} else {
			compacted = true;
		}
		
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
			saver = new ArffSaver(output, instanceSet, selectedAttributes, compacted);
		} else if (fileName.regionMatches(true, fileName.length() - 4, ".txt", 0, 4)) {
			saver = new TxtSaver(output, instanceSet, selectedAttributes, compacted);
		} else {
			throw new IOException(resource.getString("fileExtensionException"));
		}

		//starts loading and shows a status screen
		ProgressDialog progressDialog = new ProgressDialog (output.getName(), saver);
		progressDialog.load();
	}

}
