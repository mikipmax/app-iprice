package com.iprice.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@PrimaryKeyJoinColumn(name = "cred_id")
public class Persona extends Credencial {

	@Column(name = "pers_nombre")
	private String persNombre;
	@Column(name = "pers_apellido")
	private String persApellido;
	@Column(name = "pers_direccion")
	private String persDireccion;
	@Column(name = "pers_telefono")
	private String persTelefono;
	@Column(name = "pers_email")
	private String persEmail;
	@JoinColumn(name = "loca_id", referencedColumnName = "loca_id")
	@ManyToOne
	private Localidad locaId;
}
