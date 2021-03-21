package com.iprice.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity 
@Data
public class Localidad {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "loca_id")
	private int locaId;
	@Column(name = "loca_nombre")
	private String locaNombre;
	@Column(name = "loca_porc_desc")
	private double locaPorcDesc;
}
