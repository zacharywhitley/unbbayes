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
package unbbayes.datamining.gui.naivebayes.resources;

import java.util.ListResourceBundle;

public class NaiveBayesResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Naive Bayes Main
	{"numericOption","Numeric attributes options:"},
	{"selectDB","Select data base:"},
	{"selectClass","Select class:"},
	{"cancel","Cancel"},
	{"help","Help"},
	{"select","Browse ..."},
	{"method","Discretization method:"},
	{"num","Number of discretized states:"},
	{"frequency","Frequency"},
	{"range","Range"},
	{"errorDB","Error in data base: "},
	{"error","Error "},
	{"fileNotFound","File not found: "},
	{"errorOpen","Error opening file: "},
	{"selectMnemonic",new Character('B')},
	{"cancelMnemonic",new Character('C')},
	{"helpMnemonic",new Character('H')},
	{"helpTopicsMnemonic",new Character('E')},
	{"fileMnemonic",new Character('F')},
	{"openMnemonic",new Character('O')},
	{"exitMnemonic",new Character('X')},
	{"learningMnemonic",new Character('L')},
	{"learnNaiveBayesMnemonic",new Character('N')},
	{"saveNetworkMnemonic",new Character('S')},
	{"fileMenu","File"},
	{"openMenu","Open ..."},
	{"exit","Exit"},
	{"helpTopicsMenu","Help Topics"},
	{"learningMenu","Learning"},
	{"learnNaiveBayes","Learn Naive Bayes "},
	{"saveNetworkMenu","Save Network ..."},
	{"openFileTooltip","Open a file"},
	{"saveFileTooltip","Save a file"},
	{"learnDataTooltip","Learn data"},
	{"helpFileTooltip","Call help file"},
	{"welcome","Welcome"},
	{"attributes2","Attributes"},
	{"inference","Inference"},
	{"fileExtensionException","File extension not known."},
	{"numericAttributesException","Can't handle numeric attributes - Discretization needed"},
	{"openFile","File opened successfully"},
	{"error2","Error= "},
	{"learnSuccessful","Naive Bayes learning successful"},
	{"exception","Exception "},
	{"errorWritingFileException","Error writing file "},
        {"saveModel","Model saved successfully"},
	};
}