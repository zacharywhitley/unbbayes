package br.com.digitaxi.pessoa.operador;

import br.com.digitaxi.empresa.empresataxi.EmpresaTaxi;
import br.com.digitaxi.pessoa.pessoa.Pessoa;

// Generated 23/03/2007 15:18:07 by Hibernate Tools 3.2.0.beta8

/**
 * Operador generated by hbm2java
 */
public class Operador implements java.io.Serializable {

	// Fields    

	private int idOperador;

	private EmpresaTaxi empresaTaxi;

	private Pessoa pessoa;

	// Constructors

	/** default constructor */
	public Operador() {
	}

	/** full constructor */
	public Operador(int idOperador, EmpresaTaxi empresaTaxi, Pessoa pessoa) {
		this.idOperador = idOperador;
		this.empresaTaxi = empresaTaxi;
		this.pessoa = pessoa;
	}

	// Property accessors
	public int getIdOperador() {
		return this.idOperador;
	}

	public void setIdOperador(int idOperador) {
		this.idOperador = idOperador;
	}

	public EmpresaTaxi getEmpresaTaxi() {
		return this.empresaTaxi;
	}

	public void setEmpresaTaxi(EmpresaTaxi empresaTaxi) {
		this.empresaTaxi = empresaTaxi;
	}

	public Pessoa getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

}
