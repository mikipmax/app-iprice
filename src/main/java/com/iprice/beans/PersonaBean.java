package com.iprice.beans;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iprice.dto.Persona;
import com.iprice.repositorios.PersonaI;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ViewScoped // ámbito JSF (request)

@Component("personaB") // Ámbito Spring
public class PersonaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Persona> personas;
	private Persona persona;
	private Persona personaSelect;

	@Autowired // Inyección de dependencia de Spring
	private PersonaI servicio;

	@PostConstruct
	public void init() {
		listar();
		persona = new Persona();
		personaSelect = new Persona();
	}

	public void listar() {
		personas = (List<Persona>) servicio.findAll();
	}

	public void crearPersona() {
		try {
			servicio.save(persona);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			persona = new Persona();
			listar();

		}

	}

	public void actualizar() {

		try {

			servicio.save(personaSelect);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			personaSelect = new Persona();
			listar();
		}

	}

	public void leerFila(Persona persona) {

		personaSelect = persona;

	}

	public void eliminar(Persona persona) {
		try {
			servicio.delete(persona);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			persona = new Persona();
			listar();
		}
	}

}