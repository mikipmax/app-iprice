package com.iprice.dto;

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
public class DetallePedido {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "depe_id")
	private int depeId;
	@Column(name = "depe_cantidad")
	private int depeCantidad;
	@Column(name = "depe_precio")
	private double depePrecio;
	@JoinColumn(name = "orde_id", referencedColumnName = "orde_id")
	@ManyToOne
	private Orden ordeId;
	@JoinColumn(name = "prod_id", referencedColumnName = "prod_id")
	@ManyToOne
	private Producto prodId;
}
