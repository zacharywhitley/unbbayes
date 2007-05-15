package br.com.digitaxi.pessoa.pessoa.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.pessoa.pessoa.Pessoa;
import br.com.digitaxi.pessoa.pessoa.persistence.PessoaDao;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class DeletePessoaAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String[] ids = request.getParameterValues("selecao");
		
		PessoaDao dao = new PessoaDao();
		HibernateUtil.beginTransaction();		
		for (int i=0;i<ids.length;i++) {
			String id = ids[i];
			Pessoa pessoa = dao.findById(new Short(id).shortValue());
			dao.delete(pessoa);
		}
		HibernateUtil.commitTransaction();		
		
		return mapping.findForward("success");
	}

}
