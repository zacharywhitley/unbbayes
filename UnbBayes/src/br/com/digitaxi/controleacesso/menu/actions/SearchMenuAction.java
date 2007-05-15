package br.com.digitaxi.controleacesso.menu.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.menu.persistence.MenuDao;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;

public class SearchMenuAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DynaActionForm userDynaForm = (DynaActionForm) form;
		String nome = userDynaForm.getString("nomePesquisa");

		PerfilDao perfilDao = new PerfilDao();
		Collection perfis = perfilDao.list();
		
		request.setAttribute("perfis",perfis);

		MenuDao dao = new MenuDao();
		Collection col;
		String[] campos = {"nome"};
		String[] valores = {nome};
		col = dao.listOrder(0,30,campos,valores);			

		Collection menus = dao.list();
		request.setAttribute("col",col);
		request.setAttribute("menus",menus);
		
		return mapping.findForward("success");
	}
}
