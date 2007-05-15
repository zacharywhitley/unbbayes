package br.com.digitaxi.controleacesso.autenticacao;

import java.util.List;

import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;
import br.com.digitaxi.util.security.CryptographyUtil;

public class AutenticacaoBO {

	public Usuario autenticaUsuario(String login, String senha) {

		Usuario usuario = new Usuario();
		usuario.setLogin(login);
		//usuario.setSenha(new String(CryptographyUtil.encrypt(senha)));

		UsuarioDao dao = new UsuarioDao();
		List usuarios = dao.findByExample(usuario);
		if (usuarios != null && !usuarios.isEmpty()) {
			
			Usuario usuarioAutenticado =
				(Usuario) usuarios.iterator().next();
			return usuarioAutenticado;
		}

		return null;
	}
}