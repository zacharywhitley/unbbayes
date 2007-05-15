package br.com.digitaxi.controleacesso.menu.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.menu.Menu;
import br.com.digitaxi.controleacesso.menu.persistence.MenuDao;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;

public class PrepareUpdateMenuAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DynaActionForm userDynaForm = (DynaActionForm) form;
		String id = userDynaForm.getString("idMenu");
		
		MenuDao dao = new MenuDao();
		Menu menu = dao.findById(new Short(id).shortValue());
		request.setAttribute("menu",menu);

		Collection menus = dao.list();
		request.setAttribute("menus",menus);
		
		PerfilDao perfilDao = new PerfilDao();
		Collection col = perfilDao.list();
		
		request.setAttribute("perfis",col);
		request.setAttribute("perfil",new Short(menu.getPerfil().getIdPerfil()));
		if (menu.getMenuPai()!=null) {
			request.setAttribute("menupai",new Short(menu.getMenuPai().getIdMenu()));			
		}
		
		return mapping.findForward("success");
	}
}
