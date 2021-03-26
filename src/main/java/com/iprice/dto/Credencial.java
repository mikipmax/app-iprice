package com.iprice.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Credencial  {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cred_id")
	private int credId;
	@Column(name = "cred_cedula")
	private String credCedula;
	@Column(name = "cred_clave")
	private String credClave;
	@Column(name = "cred_rol")
	private boolean credRol=false;

	public Credencial(boolean credRol) {
		this.credRol = credRol;
	}

	public Credencial(int credId) {
		this.credId = credId;
	}
}
