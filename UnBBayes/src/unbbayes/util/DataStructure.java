package unbbayes.util;

/** Classe abstrata que contem as estruteras básicas para as estruturas de dados pedidas
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (26/03/2002)
*/

public abstract class DataStructure
{	/** Faz referência ao começo de uma estrutura de dados */
	protected Register start = null;
	/** Faz referência ao fim de uma estrutura de dados */
	protected Register end = null;
	/** Faz referência ao tamanho de uma estrutura de dados */
	protected int size = 0;

	/** Insere um objeto em uma estrutura de dados
		@param elem Objeto a ser inserido
	*/
	public abstract void add(Object elem);

	/** Retira o começo de uma estrutura de dados
		@return Objeto removido
	*/
	public Object remove()
	{	if (start == null)
			return null;
		Object value = start.datum;
		start = start.prox;
		if (start == null)
			end = null;
		size--;
		return value;
	}

	/** Lista os dados de uma estrutura de dados
		@return Um array com os objetos de uma estrutura de dados
	*/
	public Object[] getData()
	{	Object[] data = new Object[size];
		Register pointer = start;
		for (int i = 0; pointer != null; pointer = pointer.prox)
		{	data[i] = pointer.datum;
			i++;
		}
		return data;
	}

	/** Retorna o número de objetos na estrutura de dados
		@return Número de objetos
	*/
	public int size()
	{	return size;
	}
}