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

public class DecisiontreeResource_pt extends ListResourceBundle 
{
	public Object[][] getContents() 
	{
		return contents;
	}
	
	static final Object[][] contents = 
	{
		// Decision Tree Main
		{"open","Abrir ..."},
		{"build","Construir Árvore de Decisão"},
		{"preferences","Preferências"},	
		{"file","Arquivo"},
		{"help","Ajuda"},
		{"about","Sobre ..."},
		{"openTooltip","Abre um arquivo"},
		{"buildTooltip","Constrói uma Árvore de decisão"},
		{"exit","Sair"},
		{"nullPointerException","Arquivo inválido: "},
		{"fileNotFoundException","Arquivo não encontrado: "},
		{"ioException1","Problema lendo "},
		{"ioException2"," como um arquivo arff."},
		{"result1","Processando arquivo:\t"},
		{"result2","Número de instâncias:\t"},
		{"result3","Número de atributos:\t"},
		{"result4","Atributos:\t"},
		{"result5","Classe:\t\t"},
		{"fileMnemonic",new Character('A')},
		{"openMnemonic",new Character('A')},
		{"helpMnemonic",new Character('U')},
		{"aboutMnemonic",new Character('B')},
		{"exitMnemonic",new Character('R')},
		{"buildMnemonic",new Character('C')},
		{"helpTopicsMnemonic",new Character('T')},
		{"learnMnemonic",new Character('P')},
		{"saveModelMnemonic",new Character('S')},
		{"openModelMnemonic",new Character('M')},
        {"openAccelerator", KeyStroke.getKeyStroke(65, java.awt.event.KeyEvent.CTRL_MASK, false)},
		{"buildAccelerator", KeyStroke.getKeyStroke(67, java.awt.event.KeyEvent.CTRL_MASK, false)},
		{"helpTopics","Tópicos de ajuda"},
		{"numericAttributesException","Este programa não manipula atributos numéricos - Discretização necessária"},
		{"saveModel","Salvar Modelo ..."},
		{"learn","Aprendizagem"},
		{"openModel","Abrir Modelo ..."},
		{"callHelpFile","Chama arquivo de ajuda"},
		{"openAModel","Abre um modelo"},
		{"saveAModel","Salva um modelo"},
		{"inference","Inferência"},
        {"attributes","Atributos"},
		{"error1","Erro= "},
		{"openFile","Abrir arquivo"},
		{"fileExtensionNotKnown"," Extensão de arquivo não conhecida."},
		{"fileOpenedSuccessfully","Arquivo aberto com sucesso"},
		{"exception","Exceção "},
		{"id3Learn","Aprendizagem ID3 feita com sucesso"},
		{"welcome","Bem Vindo"},
		{"openModel2","Abrir Modelo"},
		{"modelOpenedSuccessfully","Modelo aberto com sucesso"},
		{"errorWritingFile","Erro na escrita do arquivo "},
		//Induction Panel
		{"messages","Mensagens"},
		{"selectedNode","Nó selecionado: "},
		{"leaf","\tFolha alcançada: "},
		{"back","Voltar"},
		{"backMnemonic",new Character('V')},
		{"return","\nUsuário retorna um nível na Árvore\núltima opção foi "},
		//Decision Tree Options
		{"cancel","Cancelar"},
		{"gainRatio1","Taxa de ganho"},
		{"gainRatio2","Usar taxa de ganho no aprendizado"},
		{"prunning1","Poda"},
		{"prunning2","Podar a árvore"},
		{"verbosity1","Log"},
		{"verbosity2","Nível de log:"},
		{"confidenceLevel","Nível de confiança:"},
	};
}
