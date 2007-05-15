package br.com.digitaxi.controleacesso.perfil.persistence;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;

import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class PerfilDao {

	public short persist(Perfil transientInstance) {
		Session session = HibernateUtil.getSession();
		Short id = (Short) session.save(transientInstance);
		return id.shortValue();
	}
	
	public void update(Perfil transientInstance) {
		Session session = HibernateUtil.getSession();
		session.saveOrUpdate(transientInstance);
	}
	
	public Perfil findById(short id) {
		Session session = HibernateUtil.getSession();
		return (Perfil) session.get(
			"br.com.digitaxi.controleacesso.perfil.Perfil", new Short(id));
	}
	
	public List findByExample(Perfil instance) {
		Session session = HibernateUtil.getSession();
		return session.createCriteria(
			"br.com.digitaxi.controleacesso.perfil.Perfil").add(
				Example.create(instance)).list();
	}
	
	public Collection list() {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createQuery("from br.com.digitaxi.controleacesso.perfil.Perfil ").
			list();
		return result;
	}

	public Collection listOrder() {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createCriteria(Perfil.class)
			.addOrder(Order.asc("nome"))
			.list();
		return result;
	}

	public Collection list(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createQuery("from br.com.digitaxi.controleacesso.perfil.Perfil ")
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createCriteria(Perfil.class)
			.addOrder(Order.asc("nome"))
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults,String[] campos,String[] valores) {
		List result = null;
		Session s = HibernateUtil.getSession();
		Criteria c = s.createCriteria(Perfil.class);
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

	public Collection listOrder(String[] campos,String[] valores) {
		List result = null;
		Session s = HibernateUtil.getSession();
		Criteria c = s.createCriteria(Perfil.class);
		if (campos != null) {
			for (int i=0;i<campos.length;i++) {
				c.add(Expression.like(campos[i],valores[0],MatchMode.START));
			}			
		}
		result = c.addOrder(Order.asc("nome"))
			.list();
		return result;
	}

	public void delete(Perfil instance) {
		Session session = HibernateUtil.getSession();
		session.delete(instance);
	}
}