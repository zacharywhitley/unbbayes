package br.com.digitaxi.pessoa.pessoa.persistence;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;

import br.com.digitaxi.pessoa.pessoa.Pessoa;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class PessoaDao {
	
	public int persist(Pessoa transientInstance) {
		Session session = HibernateUtil.getSession();
		Integer id = (Integer) session.save(transientInstance);
		return id.intValue();
	}
	
	public void update(Pessoa transientInstance) {
		Session session = HibernateUtil.getSession();
		session.saveOrUpdate(transientInstance);
	}
	
	public Pessoa findById(int id) {
		Session session = HibernateUtil.getSession();
		return (Pessoa) session.get(
			"br.com.digitaxi.pessoa.pessoa.Pessoa", new Integer(id));
	}
	
	public List findByExample(Pessoa instance) {
		Session session = HibernateUtil.getSession();
		return session.createCriteria(
			"br.com.digitaxi.pessoa.pessoa.Pessoa").add(
				Example.create(instance)).list();
	}
	
	public Collection list() {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createQuery("from br.com.digitaxi.pessoa.pessoa.Pessoa ").
			list();
		return result;
	}

	public Collection list(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createQuery("from br.com.digitaxi.pessoa.pessoa.Pessoa ")
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults) {
		List result = null;
		Session s = HibernateUtil.getSession();
		result = s.createCriteria(Pessoa.class)
			.addOrder(Order.asc("nome"))
			.setFirstResult(first)
			.setMaxResults(maxResults)
			.list();
		return result;
	}

	public Collection listOrder(int first, int maxResults,String[] campos,String[] valores) {
		List result = null;
		Session s = HibernateUtil.getSession();
		Criteria c = s.createCriteria(Pessoa.class);
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
		Criteria c = s.createCriteria(Pessoa.class);
		if (campos != null) {
			for (int i=0;i<campos.length;i++) {
				c.add(Expression.like(campos[i],valores[0],MatchMode.START));
			}			
		}
		result = c.addOrder(Order.asc("nome"))
			.list();
		return result;
	}

	public void delete(Pessoa instance) {
		Session session = HibernateUtil.getSession();
		session.delete(instance);
	}
}
