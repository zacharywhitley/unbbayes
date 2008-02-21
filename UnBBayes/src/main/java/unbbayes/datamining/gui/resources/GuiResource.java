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
package unbbayes.datamining.gui.resources;

import java.util.ListResourceBundle;

public class GuiResource extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Invoker Main
	{"id3Classifier","ID3 Classifier"},
	{"naiveBayesClassifier","Naive Bayes Classifier"},
	{"evaluation","Evaluation"},
	{"batchEvaluation","Batch Evaluation"},
	{"lookAndFeel","Look and Feel"},
	{"window","Window"},
	{"view","View"},
	{"toolsbar","Tools Bar"},	
	{"help","Help"},
	{"helpTopics","Help Topics"},
	{"cascade","Cascade"},
	{"tile","Tile"},
	{"english","English"},
	{"portuguese","Portuguese"},
	{"globalOptions","Global Options"},
	{"preferences","Preferences..."},
	
	{"tbPreferences","Global Options Tools Bar"},
	{"tbView","View Tools Bar"},
	{"tbWindow","Window Tools Bar"},
	{"tbHelp","Help Tools Bar"},	

	
	{"id3Mnemonic",new Character('I')},
	{"naiveBayesMnemonic",new Character('N')},
	{"evaluationMnemonic",new Character('E')},
	{"batchEvaluationMnemonic",new Character('a')},
	{"languageMnemonic",new Character('L')},
	{"lafMnemonic",new Character('F')},
	{"windowMnemonic",new Character('W')},
	{"helpMnemonic",new Character('H')},
	{"helpTopicsMnemonic",new Character('T')},
	{"preferencesMnemonic",new Character('P')},
	{"globalOptionsMnemonic",new Character('G')},
	{"portugueseMnemonic",new Character('P')},
	{"englishMnemonic",new Character('E')},
	{"selectMnemonic",new Character('S')},
	{"preprocessorMnemonic",new Character('P')},
	{"cascadeMnemonic",new Character('C')},
	{"tileMnemonic",new Character('T')},
	{"viewMnemonic",new Character('V')},
	{"tbMenuMnemonic",new Character('B')},
	
	{"tbPreferencesMnemonic",new Character('G')},
	{"tbViewMnemonic",new Character('V')},
	{"tbWindowMnemonic",new Character('W')},
	{"tbHelpMnemonic",new Character('H')},
	
	{"selectProgram","Select Program"},
	{"instancesPreprocessor","Instances InitializePreprocessors"},
	{"unsupportedLookAndFeelException","LookAndFeel not supported: "},
	{"classNotFoundException","LookAndFeel class not found: "},
	{"instanciationException","Can't load LookAndFeel: "},
	{"illegalAccessException","LookAndFeel can't be used: "},
	// Global Options
	{"globalOptions","Global Options"},
	{"defaultLanguage","Default Language"},
	{"defaultLookAndFeel","Default Look and Feel"},
	{"maximumNumber","Maximum number of states allowed"},
	{"cancel","Cancel"},
	// Attribute Panel
	{"selectClass","Select Class ="},
	{"class","Class = "},
	{"attributes","Attributes = "},
	// AttributesTree
	{"clickToChange", "Click here to enter a value"},
	};
}