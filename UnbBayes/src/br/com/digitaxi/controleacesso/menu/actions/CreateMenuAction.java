package br.com.digitaxi.controleacesso.menu.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.menu.Menu;
import br.com.digitaxi.controleacesso.menu.persistence.MenuDao;
import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class CreateMenuAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DynaActionForm userDynaForm = (DynaActionForm) form;
		String nome = userDynaForm.getString("nome");
		String descricao = userDynaForm.getString("descricao");
		String menupai = userDynaForm.getString("menupai");
		String idPerfil = userDynaForm.getString("perfil");
		String url = userDynaForm.getString("url");
		String icon = userDynaForm.getString("icon");
		String prioridade = userDynaForm.getString("prioridade");
		
		PerfilDao perfilDao = new PerfilDao();
		Perfil perfil = perfilDao.findById(new Short(idPerfil).shortValue());
		
		Menu menu = new Menu();
		menu.setNome(nome);
		menu.setDescricao(descricao);
		menu.setPerfil(perfil);
		menu.setUrl(url);
		menu.setIcon(icon);
		menu.setPrioridade(new Integer(prioridade).intValue());
		
		MenuDao dao = new MenuDao();
		if (menupai!=null && !menupai.equals("")) {
			Menu menuPai = dao.findById(new Short(menupai).shortValue());
			menu.setMenuPai(menuPai);			
		}

		HibernateUtil.beginTransaction();		
		dao.persist(menu);
		HibernateUtil.commitTransaction();		
		
		return mapping.findForward("success");
	}
}
