package br.com.digitaxi.controleacesso.menu.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.controleacesso.menu.persistence.MenuDao;
import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.util.security.AuthenticationConstants;

public class ExecuteMenuAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		HttpSession session = request.getSession();
		Perfil perfil = (Perfil) session.getAttribute(
				AuthenticationConstants.USER_PROFILE);
		
		MenuDao dao = new MenuDao();
		List menus = dao.findByPerfil(perfil);
		request.setAttribute("menus", menus);
		return mapping.findForward("painelmenu");
	}
}
