package unbbayes.prs.mebn.test;

import java.io.File;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

public class SSBNTest {
	
	public static void main(String[] args) throws Exception {
		// 0. Comentar o ConsistencyUtilities.hasCycle()
		// 1. Caregar a ontologia StarTrek27.ubf
		PrOwlIO io = new PrOwlIO(); 
		MultiEntityBayesianNetwork net = io.loadMebn(new File("examples/mebn/StarTrek28.owl"));
		
		System.out.println(net.getName());
		
		// 1.1 Possibilitar atribuição de estados (entidade) StarshipZone() -> Zone
		
		// 1.2 Tratar sempre harArgNum como nonnegativenumber
		
		
		
		// 2. Popular a base de conhecimento com os individuos
		
		
		// 3. Popular a base de conhecimento com as evidencias
		
		
		
		// 4. Iniciar algoritmo de SSBN - QUERY
		// 5. Procurar por !ST4 e !T0
	}

}
