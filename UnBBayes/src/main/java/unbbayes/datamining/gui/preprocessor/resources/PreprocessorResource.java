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
package unbbayes.datamining.gui.preprocessor.resources;

import java.util.ListResourceBundle;

public class PreprocessorResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// PreprocessorMain
		{"selectProgram","Select Program"},
		{"help","Help"},
	{"file","File"},
		{"helpTopics","Help Topics"},
		{"openFile","Open File"},
		{"saveFile","Save File"},
		{"saveTrainingFile","Save Training File"},
		{"saveTestFile","Save Test File"},
		{"status","Status"},
		{"welcome","Welcome"},
		{"open","Open..."},
		{"save","Save..."},
		{"fileTestTraining","Create Test and Training Sets..."},
		{"sample","Create Sample..."},
		{"exit","Exit"},
		{"preprocess","Preprocess"},
	{"errorException","Error= "},
	{"fileOpened","File opened successfully"},
	{"fileTestTrainingCreated","Test and training set created successfully"},
	{"sampleCreated","Sample created successfully"},
	{"preprocessorTitle","InitializePreprocessors - "},
	{"errorDB","Error in data base: "},
	{"fileNotFound","File not found: "},
	{"errorOpen","Error opening file: "},
	{"error","Error"},
	{"fileMnemonic",new Character('F')},
		{"helpMnemonic",new Character('H')},
		{"helpTopicsMnemonic",new Character('E')},
		{"openMnemonic",new Character('O')},
		{"saveMnemonic",new Character('S')},
		{"fileTestTrainingMnemonic",new Character('C')},
		{"sampleMnemonic",new Character('P')},
		{"fileExitMnemonic",new Character('X')},
		// PreprocessPanel
		{"continuousAttributes","Continuous attributes"},
	{"attributeInfo","Attribute info for base relation"},
	{"fileAttributes","Attributes in file"},
	{"file","File"},
	{"attributes","Attributes: "},
	{"instances","Instances: "},
	{"none","None"},
	{"relation","Relation: "},
	{"name","Name: "},
	{"distinct","Distinct: "},
	{"missing","Missing: "},
	{"type","Type: "},
	{"discretizeAttribute","Discretize attribute ..."},
	{"instancesEditor","Instances Editor from Selected Attributes"},
	{"selectedAttributes","Only selected attributes will be used by Instances Editor"},
	{"label","Label"},
	{"count","Count"},
	{"statistic","Statistic"},
	{"value","Value"},
	{"minimum","Minimum"},
	{"maximum","Maximum"},
	{"mean","Mean"},
	{"stdDev","StdDev"},
	{"nominal","Nominal"},
	{"numeric","Numeric"},
	{"noAttributeSelected","No attribute selected"},
		// AttributeSelectionPanel
		{"name2","Name"},
	{"selectedAttributes","Only selected attributes will be used by Instances Editor"},
		};
}