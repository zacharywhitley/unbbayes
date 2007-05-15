package br.com.digitaxi.controleacesso.menu.junit;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;
import br.com.digitaxi.util.persistence.HibernateUtil;
import br.com.digitaxi.controleacesso.menu.Menu;
import br.com.digitaxi.controleacesso.menu.persistence.MenuDao;

public class TesteMenu extends TestCase {

	//private static Menu instance = new Menu();
	private static MenuDao dao = new MenuDao();
	//private static short id = 0;
	
	public TesteMenu(){
		
	}
	protected void setUp() {}
	protected void tearDown() {}
	
	public void test0BeginTransaction(){
		HibernateUtil.beginTransaction();
	}
	
	/*public void test1Cadastro(){
		instance.setNome("Distrito Federal");
		id = dao.persist(instance);
		assertTrue(id != 0);
	}
	
	public void test2Consulta(){
		Menu ufCadastrada = dao.findById(id);
		boolean flag = ufCadastrada != null &&
			instance.getNome().equals(ufCadastrada.getNome());
		assertTrue(flag);
	}
	
	public void test3Deleta(){
		dao.delete(instance);
	}*/
	
	public void test4Listagem(){
		Collection col = dao.list();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Menu p = (Menu)it.next();
			System.out.println(p.getDescricao());
			System.out.println(p.getNome());			
		}
		if (col.size()!=0) {
			assertTrue(true);			
		} else {
			assertTrue(false);
		}
	}
	
	public void test5ListagemParcial(){
		Collection col = dao.list(0,3);
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Menu p = (Menu)it.next();
			System.out.println(p.getDescricao());
			System.out.println(p.getNome());
		}		
		if (col.size()!=0) {
			assertTrue(true);			
		} else {
			assertTrue(false);
		}
	}
	
	public void test6MenusFilhos(){
		Menu menu = dao.findById((short)5);
		Collection col = dao.findChildren(menu);
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Menu p = (Menu)it.next();
			System.out.println(p.getDescricao());
			System.out.println(p.getNome());
		}		
		if (col.size()!=0) {
			assertTrue(true);			
		} else {
			assertTrue(false);
		}
	}
	
	public void test99CommitTransaction(){
		HibernateUtil.commitTransaction();
	}
}