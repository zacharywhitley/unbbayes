package br.com.digitaxi.controleacesso.autenticacao.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.autenticacao.AutenticacaoBO;
import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.util.security.AuthenticationConstants;

public class ExecuteAutenticacaoAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm dynaForm = (DynaActionForm) form;
		String login = (String) dynaForm.get("login");
		String senha = (String) dynaForm.get("senha");
		
		AutenticacaoBO bo = new AutenticacaoBO();
		Usuario usuario = bo.autenticaUsuario(login, senha);
		if (usuario != null) {

			HttpSession session = request.getSession(true);
			session.setAttribute(AuthenticationConstants.USER_KEY,
					usuario);
			session.setAttribute(AuthenticationConstants.USER_PROFILE,
					usuario.getPerfil());
			return mapping.findForward("principal");

		} else {

			ActionMessages messages = new ActionMessages();
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				"controleacesso.autenticacao.FormularioAutenticacao.erro"));
			saveMessages(request, messages);
		}
		return mapping.findForward("formulario");
	}

}