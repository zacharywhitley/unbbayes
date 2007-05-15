package br.com.digitaxi.pessoa.pessoa.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;
import br.com.digitaxi.pessoa.pessoa.Pessoa;
import br.com.digitaxi.pessoa.pessoa.persistence.PessoaDao;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class CreatePessoaAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DynaActionForm userDynaForm = (DynaActionForm) form;
		String nome = userDynaForm.getString("nome");
		String cpf = userDynaForm.getString("cpf");
		String idUsuario = userDynaForm.getString("usuario");
		String telefoneRes = userDynaForm.getString("telefoneRes");
		String celular = userDynaForm.getString("celular");
		String telefoneCom = userDynaForm.getString("telefoneCom");
		String email = userDynaForm.getString("email");
		String descricao = userDynaForm.getString("descricao");
		
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		pessoa.setCpf(cpf);
		pessoa.setTelefoneRes(new Integer(telefoneRes));
		pessoa.setTelefoneCom(new Integer(telefoneCom));
		pessoa.setCelular(new Integer(celular));
		pessoa.setEmail(email);
		pessoa.setDescricao(descricao);
		
		UsuarioDao usuarioDao = new UsuarioDao();
		Usuario usuario = usuarioDao.findById(new Short(idUsuario).shortValue());
		pessoa.setUsuario(usuario);

		HibernateUtil.beginTransaction();		
		PessoaDao pessoaDao = new PessoaDao();
		pessoaDao.persist(pessoa);
		HibernateUtil.commitTransaction();		

		return mapping.findForward("success");
	}

}
