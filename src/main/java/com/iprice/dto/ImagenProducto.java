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
public class ImagenProducto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "impr_id")
	private int imprId;
	@JoinColumn(name = "prod_id", referencedColumnName = "prod_id")
	@ManyToOne
	private Producto prodId;
	@Column(name = "impr_img")
	private byte [] imprImg;
}
