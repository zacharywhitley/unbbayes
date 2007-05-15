package br.com.digitaxi.util.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * Classe utilit&aacute;ria para cria&ccedil;&atilde;o
 * de sess&otilde;es do Hibernate.
 * 
 * @author Gustavo Portella
 * @since jsdk5.0
 */
public final class HibernateUtil {
	
	/**
	 * Objeto para log utilizando o framework log4j.
	 */
	private static final Log logger = LogFactory
			.getLog(HibernateUtil.class);
	
	/**
	 * Objeto ThreadLocal para a sess&atilde;o do Hibernate.
	 */
	private static final ThreadLocal sessionThread =
			new ThreadLocal();
	
	/**
	 * Objeto ThreadLocal para a transa&ccedil;&atilde;o do Hibernate.
	 */
	private static final ThreadLocal transactionThread =
		new ThreadLocal();
	
	/**
	 * Objeto SessionFactory do Hibernate.
	 */
    private static SessionFactory sessionFactory;
    
    /**
     * Bloco est&aacute;tico para inicializa&ccedil;&atilde;o
     * do objeto SessionFactory do Hibernate.
     */
    static {
    	try {
    		sessionFactory = new Configuration()
    			.configure().buildSessionFactory();
    	} catch (Exception ex) {
    		logger.error(ex);
    	}
    }
    
    /**
     * Construtor privado para n&atilde;o permitir
     * instancia&ccedil;&atilde;o de objetos.
     */
    private HibernateUtil() {}
    
    /**
     * M&eacute;todo para obten&ccedil;&atilde;o
     * da sess&atilde;o corrente.
     * 
     * @return sess&atilde;o corrente
     */
    public static Session getSession() {

    	Session session = (Session) sessionThread.get();
		if (session == null) {
			session = sessionFactory.openSession();
			sessionThread.set(session);
		}
		return session;
	}
    
    
    /**
     * M&eacute;todo para fechar a
     * sess&atilde;o corrente.
     */
    public static void closeSession() {

    	Session session = (Session) sessionThread.get();
    	if (session != null) {
    		session.close();
    		sessionThread.set(null); 
    	}
    }
    
    /**
     * Inicializa uma transa&ccedil;&atilde;o a partir da sess&atilde;o
     * corrente.
     */
    public static void beginTransaction() {

    	if (!isInTransaction()) {
	    	Session session = getSession();
	    	Transaction transaction = session.beginTransaction();
	    	transactionThread.set(transaction);
    	}
    }
    
    /**
     * Executa commit da transa&ccedil;&atilde;o corrente.
     */
    public static void commitTransaction() {

    	if (isInTransaction()) {
	    	Transaction transaction = (Transaction) transactionThread.get(); 
    		transaction.commit();
    		transactionThread.set(null);
    	}
    }
    
    /**
     * Executa rollback da transa&ccedil;&atilde;o corrente.
     */
    public static void rollbackTransaction() {

    	if (isInTransaction()) {
	    	Transaction transaction = (Transaction) transactionThread.get(); 
    		transaction.rollback();
    		transactionThread.set(null);
    	}
    }
    
    /**
     * Verifica se existe transa&ccedil;&atilde;o aberta.
     * @return true, caso exista, false, caso contr&aacute;rio
     */
    public static boolean isInTransaction() {

    	Transaction transaction = (Transaction) transactionThread.get();
    	return (transaction != null && transaction.isActive() &&
    		!transaction.wasCommitted() && !transaction.wasRolledBack());
    }
    
	/**
	 * M&eacute;todo para clonar o objeto lan&ccedil;a uma
	 * exce&ccedil;&atilde;o do tipo CloneNotSupportedException
	 * para previnir m&uacute;ltiplas inst&acirc;ncias.
	 * 
	 * @return Object clone
	 * @throws CloneNotSupportedException
	 * 		para previnir m&uacute;ltiplas inst&acirc;ncias
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}