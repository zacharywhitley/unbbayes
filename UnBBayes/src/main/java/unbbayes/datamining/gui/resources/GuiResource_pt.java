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

public class GuiResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}
	static final Object[][] contents = {
	// Invoker Main
	{"id3Classifier","ID3 Classifier"},
	{"naiveBayesClassifier","Naive Bayes Classifier"},
	{"evaluation","Evaluation"},
	{"batchEvaluation","Avaliação em Lote"},
	{"language","Linguagem"},
	{"lookAndFeel","Look and Feel"},
	{"window","Janela"},
	{"view","Exibir"},
	{"toolsbar","Barras de Ferramentas"},	
	{"help","Ajuda"},
	{"helpTopics","Tópicos de ajuda"},
	{"cascade","Cascade"},
	{"tile","Tile"},
	{"english","Inglês"},
	{"portuguese","Português"},
	{"globalOptions","Opções globais"},
	{"preferences","Preferências..."},
	
	{"tbPreferences","Barra de Ferramentas de Opções Globais"},
	{"tbView","Barra de Ferramentas de Exibir"},
	{"tbWindow","Barra de Ferramentas de Janela"},
	{"tbHelp","Barra de Ferramentas de Ajuda"},	
	
	{"id3Mnemonic",new Character('I')},
	{"naiveBayesMnemonic",new Character('N')},
	{"evaluationMnemonic",new Character('A')},
	{"batchEvaluationMnemonic",new Character('o')},
	{"languageMnemonic",new Character('L')},
	{"lafMnemonic",new Character('F')},
	{"windowMnemonic",new Character('J')},
	{"helpMnemonic",new Character('A')},
	{"helpTopicsMnemonic",new Character('T')},
	{"preferencesMnemonic",new Character('P')},
	{"globalOptionsMnemonic",new Character('G')},
	{"portugueseMnemonic",new Character('P')},
	{"englishMnemonic",new Character('I')},
	{"selectMnemonic",new Character('S')},
	{"preprocessorMnemonic",new Character('P')},
	{"cascadeMnemonic",new Character('C')},
	{"tileMnemonic",new Character('T')},
	{"viewMnemonic",new Character('E')},
	{"tbMenuMnemonic",new Character('B')},
	
	{"tbPreferencesMnemonic",new Character('G')},
	{"tbViewMnemonic",new Character('E')},
	{"tbWindowMnemonic",new Character('J')},
	{"tbHelpMnemonic",new Character('A')},	
	
	{"selectProgram","Selecione Programa"},
	{"instancesPreprocessor","InitializePreprocessors"},
	{"unsupportedLookAndFeelException","Não suporta esse LookAndFeel: "},
	{"classNotFoundException","A classe do LookAndFeel não foi encontrada: "},
	{"instanciationException","Não foi possível carregar esse LookAndFeel: "},
	{"illegalAccessException","Esse LookAndFeel não pode ser usado: "},
	// Global Options
	{"defaultLanguage","Linguagem Default"},
	{"defaultLookAndFeel","Look and Feel Default"},
	{"maximumNumber","Número Máximo de estados permitidos"},
	{"cancel","Cancelar"},
	// Attribute Panel
	{"selectClass","Selecione Classe ="},
	{"class","Classe = "},
	{"attributes","Attributos = "},
	// AttributesTree
	{"clickToChange", "Clique aqui para entrar com um valor"},
	};
}