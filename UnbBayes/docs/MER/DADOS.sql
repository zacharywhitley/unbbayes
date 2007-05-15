insert into perfil (id_perfil,nome,descricao)
values (nextval('seq_perfil'),'Administrador','Administradores do Sistema');

insert into usuario (id_usuario,id_perfil,"login",senha,descricao)
values (nextval('seq_usuario'),1,'gportella','e99a18c428cb38d5f260853678922e03','Gustavo Portella');

insert into menu (id_menu,id_menu_pai,id_perfil,nome,descricao,url,icon)
values (nextval('seq_menu'),null,1,'Cadastros','Cadastros',null,null);
insert into menu (id_menu,id_menu_pai,id_perfil,nome,descricao,url,icon)
values (nextval('seq_menu'),1,1,'Usuário','Cadastro de Usuário','/Usuario/PrepareSearchUsuario.do','/images/icons/icon_usuario.gif');
insert into menu (id_menu,id_menu_pai,id_perfil,nome,descricao,url,icon)
values (nextval('seq_menu'),1,1,'Perfil','Cadastro de Perfil de Usuário','/Perfil/PrepareSelecionarPerfil.do','/images/icons/icon_perfil.gif');
insert into menu (id_menu,id_menu_pai,id_perfil,nome,descricao,url,icon)
values (nextval('seq_menu'),null,1,'Relatórios','Relatórios',null,null);
