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

public class EvaluationResource_pt extends ListResourceBundle {

	public Object[][] getContents() {
		return contents;
	}

	static final Object[][] contents = {
		// EvaluationMain
		{"title","Avaliação"},
		{"selectProgram","Selecione Programa"},
		{"file","Arquivo"},
		{"help","Ajuda"},
		{"helpTopics","Tópicos de ajuda"},
		{"status","Status"},
		{"welcome","Bem vindo"},
		{"exit","Sair"},
		{"openModel","Abrir um modelo"},
		{"openModelDialog","Abrir Modelo ..."},
		{"error2","Erro= "},
		{"errorDB","Erro na base de dados: "},
		{"error","Erro "},
		{"fileNotFound","Arquivo não encontrado: "},
		{"errorOpen","Erro ao abrir arquivo: "},
		{"modelOpened","Modelo aberto com sucesso"},
		{"model","Modelo "},
		{"fileExtensionNotKnown","Extensão de arquivo não conhecida"},
		{"numericAttributesException","Este programa não manipula atributos numéricos - Discretização necessária"},
		{"fileMnemonic",new Character('A')},
		{"helpMnemonic",new Character('U')},
		{"helpTopicsMnemonic",new Character('T')},
		{"fileExitMnemonic",new Character('R')},
		{"openModelMnemonic",new Character('M')},
	};
}