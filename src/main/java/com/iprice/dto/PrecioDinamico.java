package com.iprice.dto;

import lombok.Data;

@Data
public class PrecioDinamico {

	private int prod_id;
	private double limite_inferior;
	private double limite_superior;
	private double precio_optimizado;
	private int demanda_predicha;
	private int ganancia_prevista;
	private double prod_precio_norm;
	private double prod_precio_fin;
}
