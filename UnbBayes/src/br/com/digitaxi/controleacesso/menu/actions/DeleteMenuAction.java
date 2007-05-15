package br.com.digitaxi.controleacesso.menu.actions;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.com.digitaxi.controleacesso.menu.Menu;
import br.com.digitaxi.controleacesso.menu.persistence.MenuDao;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class DeleteMenuAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String[] ids = request.getParameterValues("selecao");
		
		MenuDao dao = new MenuDao();
		HibernateUtil.beginTransaction();		
		for (int i=0;i<ids.length;i++) {
			String id = ids[i];
			Menu menu = dao.findById(new Short(id).shortValue());
			if (menu!=null && menu.getMenuPai()==null) {
				Collection filhos = dao.findChildren(menu);
				Iterator it = filhos.iterator();
				while (it.hasNext()) {
					Menu filho = (Menu)it.next();
					dao.delete(filho);
				}
				dao.delete(menu);
			} else if (menu!=null) {
				dao.delete(menu);
			}
		}
		HibernateUtil.commitTransaction();		
		
		return mapping.findForward("success");
	}
}
