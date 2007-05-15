package br.com.digitaxi.controleacesso.usuario.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;

public class PrepareCreateUsuarioAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		PerfilDao perfilDao = new PerfilDao();
		Collection col = perfilDao.listOrder();
		request.setAttribute("perfis",col);

		return mapping.findForward("success");
	}

}
