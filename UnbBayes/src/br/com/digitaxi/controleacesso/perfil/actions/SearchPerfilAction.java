package br.com.digitaxi.controleacesso.perfil.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;

public class SearchPerfilAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DynaActionForm userDynaForm = (DynaActionForm) form;
		String nome = userDynaForm.getString("nomePesquisa");

		PerfilDao dao = new PerfilDao();
		Collection col;
		String[] campos = {"nome"};
		String[] valores = {nome};
		col = dao.listOrder(campos,valores);
		
		request.setAttribute("col",col);
		
		return mapping.findForward("success");
	}
}
