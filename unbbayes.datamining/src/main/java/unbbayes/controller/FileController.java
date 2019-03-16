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
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;

import unbbayes.datamining.datamanipulation.ArffSaver;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.Saver;
import unbbayes.datamining.datamanipulation.TxtSaver;
import unbbayes.datamining.gui.datamanipulation.AttributeTypeChooserController;

/**
 * File Controller for some files of UnBMiner (arff and txt)
 */
public class FileController
{	 /** A instance of this object */
	private static FileController singleton;
	private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));


	private ResourceBundle resourceNaiveBayesian = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.datamining.gui.naivebayes.resources.NaiveBayesResource.class.getName(),
			java.util.Locale.getDefault(), this.getClass().getClassLoader());
	
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
	protected FileController(){	 
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
		
		boolean compacted = instanceSet.counterIndex != -1;
		this.saveInstanceSet(output, instanceSet, selectedAttributes, compacted);
		
	}
	
	public void saveInstanceSet(File output, InstanceSet instanceSet,
			int[] selectedAttributes, boolean isCompactTextFormat) throws IOException {
		Saver saver;
		String fileName = output.getName();
		
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
			saver = new ArffSaver(output, instanceSet, selectedAttributes, isCompactTextFormat);
		} else if (fileName.regionMatches(true, fileName.length() - 4, ".txt", 0, 4)) {
			saver = new TxtSaver(output, instanceSet, selectedAttributes, isCompactTextFormat);
		} else {
			throw new IOException(resourceNaiveBayesian.getString("fileExtensionException"));
		}

		//starts loading and shows a status screen
		ProgressDialog progressDialog = new ProgressDialog (output.getName(), saver);
		progressDialog.load();
	}

}
