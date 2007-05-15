package br.com.digitaxi.controleacesso.usuario.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;

public class SearchUsuarioAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DynaActionForm userDynaForm = (DynaActionForm) form;
		String login = userDynaForm.getString("loginPesquisa");

		UsuarioDao dao = new UsuarioDao();
		Collection col;
		String[] campos = {"login"};
		String[] valores = {login};
		col = dao.listOrder(campos,valores);
		
		request.setAttribute("col",col);
		
		return mapping.findForward("success");
	}
}
