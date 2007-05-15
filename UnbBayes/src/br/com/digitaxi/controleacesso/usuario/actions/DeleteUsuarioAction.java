package br.com.digitaxi.controleacesso.usuario.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class DeleteUsuarioAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String[] ids = request.getParameterValues("selecao");
		
		UsuarioDao dao = new UsuarioDao();
		HibernateUtil.beginTransaction();		
		for (int i=0;i<ids.length;i++) {
			String id = ids[i];
			Usuario usuario = dao.findById(new Short(id).shortValue());
			dao.delete(usuario);
		}
		HibernateUtil.commitTransaction();		
		
		return mapping.findForward("success");
	}

}
