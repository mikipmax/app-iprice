package com.iprice.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;

import com.iprice.dto.Localidad;
import com.iprice.dto.Credencial;
import com.iprice.dto.Persona;
import com.iprice.repositorios.LocalidadI;
import com.iprice.repositorios.CredencialI;
import com.iprice.repositorios.PersonaI;
import com.iprice.utils.UtileriaI;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ViewScoped
@Component("CredencialB")
public class CredencialB implements Serializable {

	private static final long serialVersionUID = 1L;
	private Credencial acceso;
	private Credencial accesoSelect;
	private Persona persona;
	private Localidad localidad;
	private List<Localidad> listaLocalidad;

	@Autowired
	private CredencialI servicioCredencial;
	@Autowired
	private PersonaI servicioPersona;
	@Autowired
	private LocalidadI servicioLocalidad;
	@Autowired private UtileriaI servicioUtileria;

	/*
	 * private Contacto contacto; private Contacto contactoSelect; private Empresa
	 * empresa; private List<Credencial> listaAcceso;
	 * 
	 * private List<Contacto> listaContacto;
	 * 
	 * private List<Empresa> listaEmpresa;
	 */

	@PostConstruct
	public void init() {

		acceso = new Credencial();
		persona = new Persona();
		localidad=new Localidad();
		listaLocalidad=(List<Localidad>) servicioLocalidad.findAll();

		/*
		 * eso = new Credencial(); contacto = new Contacto(); empresa = new Empresa();
		 * accesoSelect = new Credencial(); contactoSelect = new Contacto(); listar();
		 * listarAcceso(); listaEmpresa = (List<Empresa>) empresaServicio.findAll();
		 */
	}

	public void listar() {
		/*
		 * try {
		 * 
		 * listaContacto = (List<Contacto>) contactoServicio.findAll();
		 * 
		 * } catch (Exception e) { mensajeError("Error:" + e.getMessage()); }
		 */
	}

	public void listarAcceso() {

		/*
		 * try { listaAcceso = accesoServicio.findByCredRol((short) 1);
		 * 
		 * } catch (Exception e) { mensajeError("Error:" + e.getMessage()); }
		 */
	}

	public void registrarNuevoUsuario() {

		try {
			
			persona.setLocaId(localidad);;
			servicioPersona.save(persona); // accesoServicio.save(acceso);
			
		servicioUtileria.mensajeInfo("Registro guardado correctamente");

		} catch (Exception e) {
			//mensajeError("No s� ha podido guardar:" + e.getMessage());

		} finally {

			//listar();
			persona= new Persona();
			//empresa = new Empresa();
		}

	}

	public void crearAcceso() {

		/*
		 * try { acceso.setCredRol((short) 1); accesoServicio.save(acceso);
		 * 
		 * mensajeInfo("Registro guardado correctamente"); } catch (Exception e) {
		 * mensajeError("No s� ha podido guardar:" + e.getMessage());
		 * 
		 * } finally {
		 * 
		 * listarAcceso();
		 * 
		 * acceso = new Credencial();
		 * 
		 * }
		 */

	}

	public void actualizarAcceso() {

		/*
		 * try {
		 * 
		 * accesoServicio.save(accesoSelect);
		 * mensajeInfo("Registro actualizado correctamente"); } catch (Exception e) {
		 * mensajeError("No s� ha podido actualizar:" + e.getMessage());
		 * 
		 * } finally { accesoSelect = new Credencial();
		 * 
		 * listarAcceso(); }
		 */
	}

	public void actualizar() {

		/*
		 * try { contactoSelect.setEmprId(empresa);
		 * contactoSelect.setCredUsuario(contactoSelect.getContEmail());
		 * contactoServicio.save(contactoSelect);
		 * mensajeInfo("Registro actualizado correctamente"); } catch (Exception e) {
		 * mensajeError("No s� ha podido actualizar:" + e.getMessage());
		 * 
		 * } finally { contactoSelect = new Contacto(); empresa = new Empresa();
		 * listar(); }
		 */

	}

	public void leerFila(Credencial acceso) {
		/* accesoSelect = acceso; */

	}

	public String login() {
		String redireccion = null;
		try {
			List<Credencial> listaCredenciales = servicioCredencial.findByCredCedulaAndCredClave(acceso.getCredCedula(),
					acceso.getCredClave());
			Credencial permitido = null;
			if (!listaCredenciales.isEmpty()) {
				permitido = listaCredenciales.get(0);
			}

			if (permitido != null) {

				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuario", permitido);

				if (permitido.isCredRol()) {
					redireccion = "/admin-ciudad?faces-redirect=true";

				} else {
					// redireccion = "/formulario?faces-redirect=true";

				}

			} else {
				acceso = new Credencial();
				// redireccion = "/publico-barrio?faces-redirect=true";
				servicioUtileria.mensajeError("Credenciales Incorrectas");
			}
		} catch (Exception e) {
			// mensajeError(e.getMessage());
		}
		return redireccion;

	}

}
