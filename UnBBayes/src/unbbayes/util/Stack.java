package unbbayes.util;

/** Esta classe modela uma estrutura de dados Pilha
    @author Mario Henrique Paes Vieira
    Tem como atributos privados comecoPilha = guarda o topo da pilha
                                fimPilha = guarda o fim da pilha
                                tamanhoPilha = guarda o tamanho da pilha
*/

public class Stack extends DataStructure
{
/**	Coloca um novo valor no topo da Pilha */

	public void add(Object element)
	{	Register reg = new Register();
		reg.datum = element;
		reg.prox = start;
		start = reg;
		if (end == null)
			end = start;
		size++;
	}
}

