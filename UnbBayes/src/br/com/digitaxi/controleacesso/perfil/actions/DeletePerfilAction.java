package br.com.digitaxi.controleacesso.perfil.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class DeletePerfilAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String[] ids = request.getParameterValues("selecao");
		
		PerfilDao dao = new PerfilDao();
		HibernateUtil.beginTransaction();		
		for (int i=0;i<ids.length;i++) {
			String id = ids[i];
			Perfil perfil = dao.findById(new Short(id).shortValue());
			dao.delete(perfil);
		}
		HibernateUtil.commitTransaction();		
		
		return mapping.findForward("success");
	}

}
