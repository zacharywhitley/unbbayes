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
package unbbayes.prs.mebn;

import java.io.File;

import unbbayes.controller.MEBNController;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.util.Debug;

public class SSBNTest {
	
	public static void main(String[] args) throws Exception {
		// 0. Comentar o ConsistencyUtilities.hasCycle()
		// 0.1. Acrescentar um mecanismo que indique uma RV cujo valor é funcional/exclusiva.
		// 1. Caregar a ontologia StarTrek.ubf
		PrOwlIO io = new PrOwlIO(); 
		MultiEntityBayesianNetwork net = io.loadMebn(new File("examples/mebn/StarTrek.owl"));
		
		System.out.println(net.getName());
		
		// 1.1 Possibilitar atribuição de estados (entidade) StarshipZone() -> Zone
		
		// 1.2 Tratar sempre harArgNum como nonnegativenumber
		
		// Alterar de categorico para objectentity -> zone, timestep, starship
		
		// Atribuir objectentity, mas salvar como objectentity_Label
		
		// Verificar os erros ao carregar o prowl (setas voando)
		
		// Fazer bateria de teste na GUI criando um novo startrekXXX.ubf (encontrar e corrigir erros)
		
		
		// 2. Popular a base de conhecimento com os individuos
		
		
		// 3. Popular a base de conhecimento com as evidencias
		long tIni = System.currentTimeMillis();
		PowerLoomKB kb = PowerLoomKB.getNewInstanceKB(); 
		//kb.loadDefinitionsFile("examples/mebn/starshipfull.plm");
		long tFim = System.currentTimeMillis();
		
		System.out.println("Carregou a kb em: " + (tFim - tIni)/1000f + " segundos" );
		
		// 4. Iniciar algoritmo de SSBN - QUERY
		
		// 4.1 Pesquisar como fazer para retornar false para ask starship t0
		
		// STEP 1 - fazer esse passo depois do 3, pq nao sei os tipos de st4 e t0
		tIni = System.currentTimeMillis();
		//String teste = kb.executeCommand("(ask (Starship_label !ST4))"); 
		
//		if (!teste.equalsIgnoreCase("TRUE")) {
//			System.out.println("Não achou !st4");
//		}
//		tFim = System.currentTimeMillis();
		
		System.out.println("(ask (Starship_label !ST4)) em: " + (tFim - tIni)/1000f + " segundos" );
//		
//		teste = kb.executeCommand("(ask (Starship_label !ST4))"); 
//		
//		if (!teste.equalsIgnoreCase("TRUE")) {
//			System.out.println("Não achou !st4");
//		}
//		
//		// STEP 2
//		teste = kb.executeCommand("(RETRIEVE (HarmPotential !ST4 !T0 ?x) )"); 
//		if (!teste.equalsIgnoreCase("No solution.")) {
//			System.out.println("Solução econtrada é: " + teste);
//			return;
//		}
//		
		// STEP 3
		// criar um método q retorne a MFrag de HarmPotential (domain) 
		// e verificar se os parametros passados batem com o tipo
		
		// STEP 4
		// criar um metodo para recuperar apenas os nos de contexto q se referem a t e st
		// avaliar cada no de conexto assumindo st4 e t0
		
		
		
	}

}
