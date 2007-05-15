package br.com.digitaxi.pessoa.nomeendereco;

import br.com.digitaxi.pessoa.celular.Celular;
import br.com.digitaxi.pessoa.endereco.Endereco;

// Generated 23/03/2007 15:18:07 by Hibernate Tools 3.2.0.beta8

/**
 * NomeEndereco generated by hbm2java
 */
public class NomeEndereco implements java.io.Serializable {

	// Fields    

	private int idNomeEndereco;

	private Celular celular;

	private Endereco endereco;

	// Constructors

	/** default constructor */
	public NomeEndereco() {
	}

	/** full constructor */
	public NomeEndereco(int idNomeEndereco, Celular celular, Endereco endereco) {
		this.idNomeEndereco = idNomeEndereco;
		this.celular = celular;
		this.endereco = endereco;
	}

	// Property accessors
	public int getIdNomeEndereco() {
		return this.idNomeEndereco;
	}

	public void setIdNomeEndereco(int idNomeEndereco) {
		this.idNomeEndereco = idNomeEndereco;
	}

	public Celular getCelular() {
		return this.celular;
	}

	public void setCelular(Celular celular) {
		this.celular = celular;
	}

	public Endereco getEndereco() {
		return this.endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

}
