package br.com.digitaxi.pessoa.pessoa.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;

public class PrepareCreatePessoaAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		UsuarioDao usuarioDao = new UsuarioDao();
		Collection col = usuarioDao.listOrder();
		request.setAttribute("usuarios",col);

		return mapping.findForward("success");
	}

}
