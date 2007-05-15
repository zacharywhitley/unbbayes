package br.com.digitaxi.controleacesso.usuario.persistence;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;

import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class UsuarioDao {
	
	public int persist(Usuario transientInstance) {
		Session session = HibernateUtil.getSession();
		Integer id = (Integer) session.save(transientInstance);
		return id.intValue();
	}
	
	public void update(Usuario transientInstance) {
		Session session = HibernateUtil.getSession();
		session.saveOrUpdate(transientInstance);
	}
	
	public Usuario findById(int id) {
		Session session = HibernateUtil.getSession();
		return (Usuario) session.get(
			"br.com.digitaxi.controleacesso.usuario.Usuario", new Integer(id));
	}
	
	public List findByExample(Usuario instance) {
		Session session = HibernateUtil.getSession();
		return session.createCriteria(
			"br.com.digitaxi.controleacesso.usuario.Usuario").add(
				Example.create(instance)).list();
	}
	
	public Collection list() {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createQuery("from br.com.digitaxi.controleacesso.usuario.Usuario ").
			list();
		return result;
	}

	public Collection listOrder() {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createCriteria(Usuario.class)
			.addOrder(Order.asc("login"))
			.list();
		return result;
	}

	public Collection list(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createQuery("from br.com.digitaxi.controleacesso.usuario.Usuario ")
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createCriteria(Usuario.class)
			.addOrder(Order.asc("login"))
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults,String[] campos,String[] valores) {
		List result = null;
		Session s = HibernateUtil.getSession();
		Criteria c = s.createCriteria(Usuario.class);
		if (campos != null) {
			for (int i=0;i<campos.length;i++) {
				c.add(Expression.like(campos[i],valores[0],MatchMode.START));
			}			
		}
		result = c.addOrder(Order.asc("login"))
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(String[] campos,String[] valores) {
		List result = null;
		Session s = HibernateUtil.getSession();
		Criteria c = s.createCriteria(Usuario.class);
		if (campos != null) {
			for (int i=0;i<campos.length;i++) {
				c.add(Expression.like(campos[i],valores[0],MatchMode.START));
			}			
		}
		result = c.addOrder(Order.asc("login"))
			.list();
		return result;
	}

	public void delete(Usuario instance) {
		Session session = HibernateUtil.getSession();
		session.delete(instance);
	}
}
