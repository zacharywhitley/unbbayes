package br.com.digitaxi.pessoa.uf.persistence;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Example;

import br.com.digitaxi.util.persistence.HibernateUtil;
import br.com.digitaxi.pessoa.uf.Uf;

public class UfDao {
	
	public short persist(Uf transientInstance) {
		Session session = HibernateUtil.getSession();
		Short id = (Short) session.save(transientInstance);
		return id.shortValue();
	}
	
	public Uf findById(short id) {
		Session session = HibernateUtil.getSession();
		return (Uf) session.get(
			"br.com.digitaxi.pessoa.uf.Uf", new Short(id));
	}
	
	public List findByExample(Uf instance) {
		Session session = HibernateUtil.getSession();
		return session.createCriteria(
			"br.com.digitaxi.pessoa.uf.Uf").add(
				Example.create(instance)).list();
	}
	
	public void delete(Uf instance) {
		Session session = HibernateUtil.getSession();
		session.delete(instance);
	}
}
