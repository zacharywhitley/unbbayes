package br.com.digitaxi.controleacesso.menu.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.controleacesso.menu.persistence.MenuDao;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;

public class PrepareCreateMenuAction  extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		PerfilDao perfilDao = new PerfilDao();
		Collection col = perfilDao.listOrder();
		request.setAttribute("perfis",col);
		
		MenuDao dao = new MenuDao();
		Collection menus = dao.listOrder();
		request.setAttribute("menus",menus);

		return mapping.findForward("success");
	}
}
