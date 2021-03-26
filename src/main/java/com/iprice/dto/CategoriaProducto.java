package com.iprice.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data
@NoArgsConstructor
public class CategoriaProducto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "capr_id")
	private int caprId;
	@Column(name = "capr_nombre")
	private String caprNombre;

	public CategoriaProducto(int caprId) {
		this.caprId = caprId;
	}
}
