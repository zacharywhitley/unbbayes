package br.com.digitaxi.controleacesso.usuario.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;
import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;
import br.com.digitaxi.util.persistence.HibernateUtil;
import br.com.digitaxi.util.security.CryptographyUtil;

public class CreateUsuarioAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm userDynaForm = (DynaActionForm) form;
		String login = userDynaForm.getString("login");
		String senha = userDynaForm.getString("senha");
		String descricao = userDynaForm.getString("descricao");
		String idPerfil = userDynaForm.getString("perfil");
		
		Usuario usuario = new Usuario();
		usuario.setLogin(login);
		usuario.setSenha(new String(CryptographyUtil.encrypt(senha)));
		usuario.setDescricao(descricao);
		
		PerfilDao perfilDao = new PerfilDao();
		Perfil perfil = perfilDao.findById(new Short(idPerfil).shortValue());
		usuario.setPerfil(perfil);

		HibernateUtil.beginTransaction();		
		UsuarioDao usuarioDao = new UsuarioDao();
		usuarioDao.persist(usuario);
		HibernateUtil.commitTransaction();		

		return mapping.findForward("success");
	}

}
