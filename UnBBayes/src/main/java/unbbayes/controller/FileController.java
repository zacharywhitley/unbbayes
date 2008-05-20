/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.controller;

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.help.HelpSet;
// javax.help.JHelp appears to have a compatibility problem with java 6, so, it is removed for now
// TODO find another choice replacing javax.help.JHelp
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
{	 /** A instance of this object */
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

	/** 
	 * default constructor. S may be instanciated by getInstance method 
	 */
	protected FileController()
	{	 resource = ResourceBundle.getBundle("unbbayes.datamining.gui.naivebayes.resources.NaiveBayesResource");
	}

	//--------------------------------------------------------------//

	/**
	 * Returns an instance of this object. If the object is already instanciated once, it
	 * returns the current object, otherwise it returns a new object.
	 * @return Options object
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
		// javax.help.JHelp appears to have a compatibility problem with java 6, so, it is removed for now
		// TODO find another choice replacing javax.help.JHelp
		
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
