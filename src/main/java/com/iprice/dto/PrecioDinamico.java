package com.iprice.dto;

import lombok.Data;

@Data
public class PrecioDinamico {

	private int prod_id;
	private String prod_nombre;
	private int capr_id;
	private double prod_precio_co_1;
	private double prod_precio_co_2;
	private double limite_inferior;
	private double limite_superior;
	private double precio_optimizado;
	private int demanda_predicha;
	private double ganancia_prevista;
	private double prod_precio_norm;
	private double prod_precio_fin;
}
