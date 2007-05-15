package br.com.digitaxi.pessoa.pessoa.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;
import br.com.digitaxi.pessoa.pessoa.Pessoa;
import br.com.digitaxi.pessoa.pessoa.persistence.PessoaDao;

public class PrepareUpdatePessoaAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm userDynaForm = (DynaActionForm) form;
		String id = userDynaForm.getString("idPessoa");
		
		PessoaDao dao = new PessoaDao();
		Pessoa pessoa = dao.findById(new Short(id).shortValue());
		
		request.setAttribute("pessoa",pessoa);
		
		UsuarioDao usuarioDao = new UsuarioDao();
		Collection col = usuarioDao.listOrder();
		request.setAttribute("usuarios",col);

		return mapping.findForward("success");
	}
}
