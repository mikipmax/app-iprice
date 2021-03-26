package com.iprice.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prod_id")
	private int prodId;
	@Column(name = "prod_nombre")
	private String prodNombre;
	@JoinColumn(name = "capr_id", referencedColumnName = "capr_id")
	@ManyToOne
	private CategoriaProducto caprId;
	@Column(name = "prod_precio_opt")
	private double prodPrecioOpt;
	@Column(name = "prod_precio_norm")
	private double prodPrecioNorm;
	@Column(name = "prod_precio_co_1")
	private double prodPrecioCo1;
	@Column(name = "prod_precio_co_2")
	private double prodPrecioCo2;

	@Column(name = "prod_precio_fin")
	private double prodPrecioFin;


}
