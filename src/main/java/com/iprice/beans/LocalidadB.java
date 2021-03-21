package com.iprice.beans;

import java.io.Serializable;

import java.util.List;

import javax.annotation.PostConstruct;

import javax.faces.view.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import com.iprice.dto.Localidad;
import com.iprice.repositorios.LocalidadI;
import com.iprice.utils.UtileriaI;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ViewScoped
@Component("localidadB")
public class LocalidadB implements Serializable {

	private static final long serialVersionUID = 1L;
	private Localidad localidad;
	private Localidad localidadSelect;
	private List<Localidad> listaLocalidad;
	@Autowired
	private LocalidadI localidadServicio;
	@Autowired
	private UtileriaI utileriaServicio;

	@PostConstruct
	public void init() {
		localidad = new Localidad();
		localidadSelect = new Localidad();
		listar();

	}

	public void listar() {

		try {
			listaLocalidad = (List<Localidad>) localidadServicio.findAll();

		} catch (Exception e) {
			utileriaServicio.mensajeError("Error:" + e.getMessage());
		}

	}

	public void crear() {

		try {
			localidadServicio.save(localidad);
			utileriaServicio.mensajeInfo("Registro guardado correctamente");
		} catch (Exception e) {
			utileriaServicio.mensajeError("No s� ha podido guardar:" + e.getMessage());

		} finally {

			listar();
			localidad = new Localidad();
		}

	}


	public void actualizar() {

		try {
			localidadServicio.save(localidadSelect);
			utileriaServicio.mensajeInfo("Registro actualizado correctamente");
		} catch (Exception e) {
			utileriaServicio.mensajeError("No s� ha podido actualizar:" + e.getMessage());
		} finally {
			localidadSelect = new Localidad();
			listar();
		}

	}

	public void leerFila(Localidad localidad) {
		localidadSelect = localidad;

	}

}
