package br.com.digitaxi.controleacesso.perfil.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;

public class PrepareUpdatePerfilAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm userDynaForm = (DynaActionForm) form;
		String id = userDynaForm.getString("idPerfil");
		
		PerfilDao dao = new PerfilDao();
		Perfil perfil = dao.findById(new Short(id).shortValue());
		request.setAttribute("perfil",perfil);
		
		return mapping.findForward("success");
	}
}
