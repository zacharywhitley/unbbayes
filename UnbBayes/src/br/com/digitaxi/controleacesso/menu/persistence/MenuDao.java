package br.com.digitaxi.controleacesso.menu.persistence;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;

import br.com.digitaxi.controleacesso.menu.Menu;
import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class MenuDao {

	public short persist(Menu transientInstance) {
		Session session = HibernateUtil.getSession();
		Short id = (Short) session.save(transientInstance);
		return id.shortValue();
	}

	public void update(Menu transientInstance) {
		Session session = HibernateUtil.getSession();
		session.saveOrUpdate(transientInstance);
	}

	public Menu findById(short id) {
		Session session = HibernateUtil.getSession();
		return (Menu) session.get(Menu.class, new Short(id));
	}

	public List findByExample(Menu instance) {
		Session session = HibernateUtil.getSession();
		return session.createCriteria(Menu.class).add(
				Example.create(instance)).list();
	}

	public Collection list() {
		List result = null;
		Session s = HibernateUtil.getSession();
		HibernateUtil.beginTransaction();
		result = s.createQuery("from Menu").
			list();
		return result;
	}

	public Collection list(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createQuery("from Menu")
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder() {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createCriteria(Menu.class)
			.addOrder(Order.asc("nome"))
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createCriteria(Menu.class)
			.addOrder(Order.asc("nome"))
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults,String[] campos,String[] valores) {
		List result = null;
		Session s = HibernateUtil.getSession();
		Criteria c = s.createCriteria(Menu.class);
		if (campos != null) {
			for (int i=0;i<campos.length;i++) {
				c.add(Expression.like(campos[i],valores[0],MatchMode.START));
			}
		}
		result = c.addOrder(Order.asc("nome"))
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public void delete(Menu instance) {
		Session session = HibernateUtil.getSession();
		session.delete(instance);
	}

	public List findByPerfil(Perfil perfil) {

		StringBuffer hql = new StringBuffer();
		hql.append("select menu ");
		hql.append("from br.com.digitaxi.controleacesso.menu.Menu menu ");
		hql.append("where menu.perfil.id = :idPerfil ");
		hql.append("and menu.menuPai is null ");
		hql.append("order by menu.prioridade, menu.idMenu ");

		Session session = HibernateUtil.getSession();
		Query query = session.createQuery(hql.toString());
		query.setShort("idPerfil", perfil.getIdPerfil());
		return query.list();
	}
	
	public List findChildren(Menu menu) {
		StringBuffer hql = new StringBuffer();
		hql.append("select menu ");
		hql.append("from br.com.digitaxi.controleacesso.menu.Menu menu ");
		hql.append("where menu.menuPai = :idMenuPai ");
		hql.append("order by menu.prioridade, menu.idMenu ");

		Session session = HibernateUtil.getSession();
		Query query = session.createQuery(hql.toString());
		query.setShort("idMenuPai", menu.getIdMenu());
		return query.list();
	}
}