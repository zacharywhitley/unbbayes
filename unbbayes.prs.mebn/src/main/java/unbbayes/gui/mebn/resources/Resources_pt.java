/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.gui.mebn.resources;

import java.util.ArrayList;

import unbbayes.gui.resources.GuiResources;
import unbbayes.gui.resources.GuiResources_pt;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.gui.mebn package. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 02/13/2010
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 06/18/2011 - (feature:3317031) Added turnToMTheoryViewModeToolTip
 * 
 * TODO gradually move mebn-specific resources from {@link GuiResources} to here.
 */

public class Resources_pt extends GuiResources_pt {
 
    /** 
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		for (Object[] objects : super.getContents()) {
			list.add(objects);
		}
		for (Object[] objects2 : this.contents) {
			list.add(objects2);
		}
		return list.toArray(new Object[0][0]);
	}
 
	/**
	 * The particular resources for this class
	 */
	static final Object[][] contents =
	{	


		{"defaultMEBNEditor" , "MTheory"},
		{"defaultMEBNEditorTip" , "Editor padrão de MTheory"},
		{"MEBNModuleName" , "Rede Bayesiana Multi-Entidade"},
		{"newMebnToolTip", "Nova MEBN"}, 
        {"newMEBN","Nova MEBN"},
        {"newMEBNMn","E"},

		/* Exceptions MEBN */
		{"withoutMFrag", "No existe nenhuma MFrag"},
		
		// Entity warnings
		{"warning","Aviso"},
		{"selectEntityFirst","Selecione uma entidade para a qual se deseja adicionar uma nova entidade"},
		{"removingEntityWarning","Remover esta entidade tamb�m remover� todos os seus descendentes. Deseja Continuar?"},
		{"removeRootWarning","Root node cannot be removed!"},
		
		//Menus MEBN
		{"menuDelete", "Delete"}, 
		{"menuAddContext", "Adic. Contexto"}, 
		{"menuAddInput", "Adic. Input"},
		{"menuAddResident", "Adic. Residente"}, 
		{"menuAddDomainMFrag", "Adic. MFrag"}, 
		
		// option dialog
		{"openMEBNOptions", "Abrir Opções para MEBN"}, 
		{"mebnOptionTitle", "Opções para MEBN"}, 
		{"kbTab", "Base de Conhecimento"}, 
		{"kbParameters", "Parâmetros para a base de conhecimento"}, 
		{"availableKB", "Bases de conhecimento disponíveis"},  
		{"defaultKB", "Padrão"},  
		{"availableSSBN", "Algoritmos de geração de SSBN"}, 
		{"ssbnParameters", "Parâmetros para geração de SSBN"}, 
		{"ssbnTab", "Algoritmos de SSBN"},
		{"defaultSSBN", "Algoritmo Padrão"},  
		
		// SSBN option panel
		{"initializationCheckBoxLabel" , " Executar fase de initialização "},
		{"buildCheckBoxLabel" , " Executar fase de geração "},
		{"pruneCheckBoxLabel" , " Executar fase de podagem "},
		{"cptGenerationCheckBoxLabel" , " Executar fase de geração de CPT "},
		{"pruneConfigurationBorderTitle" , " Opções de podagem "},
		{"userInteractionCheckBoxLabel" , " Ativar modo interativo "},
		{"barrenNodePrunerCheckBoxLabel" , " Podar nós improdutivos "},
		{"dseparatedNodePrunerCheckBoxLabel" , " Podar nós d-separados do(s) nó(s) principal(is) "},
		{"mainPanelBorderTitle" , "Selecione as funcionalidades a habilitar"},
		{"recursiveLimitBorderTitle" , "Limite de profundidade na recursividade"},
		
		/* PLM file manager */
		/* TODO transfer it to IO package? */
		{"FileSaveOK" , "Base de conhecimento armazenado com sucesso"},
		{"FileLoadOK" , "Base de conhecimento carregado com sucesso"},
		{"NoSSBN" , "Não há SSBN gerada anteriormente! Modo não disponivel."},
		{"KBClean" , "Base de conhecimento limpa com sucesso"},
		{"NotImplemented" , "Funcionalidade ainda não implementada"}, 
		{"loadedWithErrors" , "Arquivo carregado, mas pode haver falhas"},
		
		// MTheory view mode
		{"turnToMTheoryViewModeToolTip" , "Visualizar MTheory"},
		{"showTitleBorder" , "Mostrar Borda do Título"},
		{"showBodyBorder" , "Mostrar Borda do Principal"},
		{"showRoundBorder" , "Mostrar Borda Arredondada"},
		
		
		// miscellaneous messages
		{"selectOnlyOneEntry" , "Por favor, selecione somente um único elemento."},
		
		{"enableSoftEvidence" , "Habilitar evidência não exata"},
		{"softEvidence" , "Evidência probabilística (regra de Jeffrey)"},
		{"likelihoodEvidence" , "Evidência por verossimilhança (likelihood evidence)"},
		{"csvSoftEvidence" , "Insira os valores da evidência (números reais separados por vírgula para probabilidades/verossimilhança)"},
		{"nonNegativeError" , "Somente números não negativos são permitidos"},
		
	};
}