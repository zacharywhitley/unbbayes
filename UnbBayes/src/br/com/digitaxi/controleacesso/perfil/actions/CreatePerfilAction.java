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

public class CreatePerfilAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm userDynaForm = (DynaActionForm) form;
		String nome = userDynaForm.getString("nome");
		String descricao = userDynaForm.getString("descricao");
		
		Perfil perfil = new Perfil();
		perfil.setNome(nome);
		perfil.setDescricao(descricao);
		
		PerfilDao dao = new PerfilDao();
		HibernateUtil.beginTransaction();		
		dao.persist(perfil);
		HibernateUtil.commitTransaction();		
		
		return mapping.findForward("success");
	}

}
