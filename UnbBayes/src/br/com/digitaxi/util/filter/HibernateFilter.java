package br.com.digitaxi.util.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import br.com.digitaxi.util.persistence.HibernateUtil;

/**
 * Filtro para tratamento de transa&ccedil;&otilde;es
 * do framework Hibernate.
 * 
 * @author Gustavo Portella
 * @since jsdk5.0
 */
public class HibernateFilter implements Filter {

	/**
	 * Inicializa o filtro
	 */
	public void init(FilterConfig config) throws ServletException {

	}
	
	/**
	 * Executa o filtro
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// Propaga o processamento
		chain.doFilter(request, response);
		
		// Verifica se existe transacao aberta e commita
		if (HibernateUtil.isInTransaction()) {
			HibernateUtil.commitTransaction();
		}
		
		// Fecha a sessao
		HibernateUtil.closeSession();
	}
	
	/**
	 * Finaliza o filtro
	 */
	public void destroy() {

	}
}