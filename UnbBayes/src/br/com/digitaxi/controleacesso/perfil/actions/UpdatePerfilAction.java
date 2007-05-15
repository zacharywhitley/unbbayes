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
import br.com.digitaxi.util.persistence.HibernateUtil;

public class UpdatePerfilAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm userDynaForm = (DynaActionForm) form;
		String id = userDynaForm.getString("idPerfil");
		String nome = userDynaForm.getString("nome");
		String descricao = userDynaForm.getString("descricao");
		
		PerfilDao dao = new PerfilDao();
		Perfil perfil = dao.findById(new Short(id).shortValue());
		perfil.setDescricao(descricao);
		perfil.setNome(nome);

		HibernateUtil.beginTransaction();		
		dao.update(perfil);
		HibernateUtil.commitTransaction();		
		
		return mapping.findForward("success");
	}
}
