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
package unbbayes.datamining.gui.evaluation.resources;

import java.util.ListResourceBundle;

public class EvaluationResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}

	static final Object[][] contents = {
		// EvaluationMain
		{"title","Evaluation"},
		{"selectProgram","Select Program"},
		{"file","File"},
		{"help","Help"},
		{"helpTopics","Help Topics"},
		{"status","Status"},
		{"welcome","Welcome"},
		{"exit","Exit"},
		{"openModel","Open a model"},
		{"openModelDialog","Open Model ..."},
		{"error2","Error= "},
		{"errorDB","Error in data base: "},
		{"error","Error "},
		{"fileNotFound","File not found: "},
		{"errorOpen","Error opening file: "},
		{"modelOpened","Model opened successfully"},
		{"model","Model "},
		{"fileExtensionNotKnown","File extension not known"},
		{"numericAttributesException","Can't handle numeric attributes - Discretization needed"},
		{"fileMnemonic",new Character('F')},
		{"helpMnemonic",new Character('H')},
		{"helpTopicsMnemonic",new Character('E')},
		{"fileExitMnemonic",new Character('X')},
		{"openModelMnemonic",new Character('M')},
	};
}