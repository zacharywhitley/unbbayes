package unbbayes.util;

/** Classe abstrata que contem as estruteras b�sicas para as estruturas de dados pedidas
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (26/03/2002)
*/

public abstract class DataStructure
{	/** Faz refer�ncia ao come�o de uma estrutura de dados */
	protected Register start = null;
	/** Faz refer�ncia ao fim de uma estrutura de dados */
	protected Register end = null;
	/** Faz refer�ncia ao tamanho de uma estrutura de dados */
	protected int size = 0;

	/** Insere um objeto em uma estrutura de dados
		@param elem Objeto a ser inserido
	*/
	public abstract void add(Object elem);

	/** Retira o come�o de uma estrutura de dados
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

	/** Retorna o n�mero de objetos na estrutura de dados
		@return N�mero de objetos
	*/
	public int size()
	{	return size;
	}
}