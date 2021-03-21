package com.iprice.dto;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class Orden {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "orde_id")
	private int ordeId;
	@Column(name = "orde_fecha")
	private LocalDate ordeFecha;
	@JoinColumn(name = "cliente_id", referencedColumnName = "cred_id")
	@ManyToOne
	private Persona clienteId;
}
