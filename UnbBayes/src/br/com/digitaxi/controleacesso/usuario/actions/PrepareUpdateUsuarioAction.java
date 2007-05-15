package br.com.digitaxi.controleacesso.usuario.actions;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;
import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;
import br.com.digitaxi.util.security.CryptographyUtil;

public class PrepareUpdateUsuarioAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm userDynaForm = (DynaActionForm) form;
		String id = userDynaForm.getString("idUsuario");
		
		UsuarioDao dao = new UsuarioDao();
		Usuario usuario = dao.findById(new Short(id).shortValue());
		String senha = usuario.getSenha();
		usuario.setSenha(CryptographyUtil.decrypt(senha.getBytes()));
		
		request.setAttribute("usuario",usuario);
		
		PerfilDao perfilDao = new PerfilDao();
		Collection col = perfilDao.listOrder();
		request.setAttribute("perfis",col);

		return mapping.findForward("success");
	}
}
