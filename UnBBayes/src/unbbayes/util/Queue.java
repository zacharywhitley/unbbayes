package unbbayes.util;

/** Esta classe modela uma estrutura de dados Fila
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (26/03/2002)
 */
public class Queue extends DataStructure
{	/** Insere elemento no começo da Fila
		@param elem Objeto a ser inserido
	*/
	public void add(Object element)
	{	Register reg = new Register();
		reg.datum = element;
		reg.prox = null;
		if (end == null)
		{	end = reg;
			start = reg;
		}
		else
		{	end.prox = reg;
			end = end.prox;
		}
		size++;
	}
}