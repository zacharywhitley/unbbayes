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
package unbbayes.datamining.gui.c45.resources;

import java.util.ListResourceBundle;

import javax.swing.KeyStroke;

public class DecisiontreeResource extends ListResourceBundle 
{
	public Object[][] getContents() 
	{
		return contents;
	}
	
	static final Object[][] contents = 
	{
		// Decision Tree Main
		{"open","Open ..."},
		{"build","Learn Decision Tree"},
		{"preferences","Preferences"},
		{"file","File"},
		{"help","Help"},
		{"about","About ..."},
		{"openTooltip","Open a file"},
		{"buildTooltip","Learn a decision tree"},
		{"exit","Exit"},
		{"nullPointerException","File invalid: "},
		{"fileNotFoundException","File not found: "},
		{"ioException1","Problem opening "},
		{"ioException2"," as a arff file."},
		{"result1","Processing file:\t"},
		{"result2","Instances number:\t"},
		{"result3","Attributes number:\t"},
		{"result4","Attributes:\t"},
		{"result5","Class:\t\t"},
		{"fileMnemonic",new Character('F')},
		{"openMnemonic",new Character('O')},
		{"helpMnemonic",new Character('H')},
		{"aboutMnemonic",new Character('A')},
		{"exitMnemonic",new Character('X')},
		{"buildMnemonic",new Character('D')},
		{"helpTopicsMnemonic",new Character('E')},
		{"learnMnemonic",new Character('L')},
		{"saveModelMnemonic",new Character('S')},
		{"openModelMnemonic",new Character('M')},
        {"openAccelerator", KeyStroke.getKeyStroke(79, java.awt.event.KeyEvent.CTRL_MASK, false)},
		{"buildAccelerator", KeyStroke.getKeyStroke(66, java.awt.event.KeyEvent.CTRL_MASK, false)},
		{"helpTopics","Help Topics"},
		{"numericAttributesException","Can't handle numeric attributes - Discretization needed"},
		{"saveModel","Save Model ..."},
		{"learn","Learning"},
		{"openModel","Open Model ..."},
		{"callHelpFile","Call help file"},
		{"openAModel","Open a model"},
		{"saveAModel","Save a model"},
		{"inference","Inference"},
        {"attributes","Attributes"},
		{"error1","Error= "},
		{"openFile","Open File"},
		{"fileExtensionNotKnown"," File extension not known."},
		{"fileOpenedSuccessfully","File opened successfully"},
		{"exception","Exception "},
		{"id3Learn","ID3 learning successful"},
		{"welcome","Welcome"},
		{"openModel2","Open Model"},
		{"modelOpenedSuccessfully","Model opened successfully"},
		{"errorWritingFile","Error writing file "},
		//Induction Panel
		{"messages","Messages"},
		{"selectedNode","Selected node: "},
		{"leaf","\tReached leaf: "},
		{"back","Back"},
		{"backMnemonic",new Character('B')},
		{"return","\nUser returns a level in the tree\nLast option was "},
		//Decision Tree Options
		{"cancel","Cancel"},
		{"gainRatio1","Gain Ratio"},
		{"gainRatio2","Use gain ratio in learning"},
		{"prunning1","Prunning"},
		{"prunning2","Prune the tree"},
		{"verbosity1","Verbosity"},
		{"verbosity2","Verbosity level:"},
		{"confidenceLevel","Confidence level:"},
	};
}
