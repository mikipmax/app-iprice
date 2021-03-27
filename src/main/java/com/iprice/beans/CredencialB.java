package com.iprice.beans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import com.iprice.dto.Credencial;
import com.iprice.dto.Persona;
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
    private Persona personaSelect;

    private List<Persona> listaCliente;
    private List<Credencial> listaCredencial;

    @Autowired
    private CredencialI servicioCredencial;
    @Autowired
    private PersonaI servicioPersona;

    @Autowired
    private UtileriaI servicioUtileria;


    @PostConstruct
    public void init() {

        acceso = new Credencial();
        accesoSelect = new Credencial();
        persona = new Persona();
        personaSelect = new Persona();

        listar();
        listarAdmin();
    }

    public void listar() {

        try {

            listaCliente = (List<Persona>) servicioPersona.findAll();

        } catch (Exception e) {
            servicioUtileria.mensajeError("Error:" + e.getMessage());
        }

    }

    public void listarAdmin() {

        try {
            listaCredencial = servicioCredencial.findByCredRolIsTrue();
        } catch (Exception e) {
            servicioUtileria.mensajeError("Error:" + e.getMessage());
        }

    }

    public void registrarNuevoUsuario() {

        try {

            servicioPersona.save(persona);
            servicioUtileria.mensajeInfo("Registro guardado correctamente");

        } catch (Exception e) {
            servicioUtileria.mensajeError("No sé ha podido guardar:" + e.getMessage());

        } finally {

            listar();
            persona = new Persona();

        }

    }

    public void crearAdmin() {


        try {
            acceso.setCredRol(true);
            servicioCredencial.save(acceso);

            servicioUtileria.mensajeInfo("Registro guardado correctamente");
        } catch (Exception e) {
            servicioUtileria.mensajeError("No sé ha podido guardar:" + e.getMessage());

        } finally {
            listarAdmin();
            acceso = new Credencial();
        }


    }


    public void actualizar() {


        try {


            servicioPersona.save(personaSelect);
            servicioUtileria.mensajeInfo("Registro actualizado correctamente");
        } catch (Exception e) {
            servicioUtileria.mensajeError("No sé ha podido actualizar:" + e.getMessage());

        } finally {
            personaSelect = new Persona();

            listar();
        }

    }

    public void actualizarAdmin() {


        try {

            servicioCredencial.save(accesoSelect);
            servicioUtileria.mensajeInfo("Registro actualizado correctamente");
        } catch (Exception e) {
            servicioUtileria.mensajeError("No s� ha podido actualizar:" + e.getMessage());

        } finally {
            accesoSelect = new Credencial();

            listarAdmin();
        }

    }

    public void leerFila(Persona persona) {
        personaSelect = persona;

    }

    public void leerFilaAdmin(Credencial credencial) {
        accesoSelect = credencial;

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
                    redireccion = "/admin-persona?faces-redirect=true";

                } else {
                     redireccion = "/cliente-tienda?faces-redirect=true";

                }

            } else {
                acceso = new Credencial();
                // redireccion = "/publico-barrio?faces-redirect=true";
                servicioUtileria.mensajeError("Credenciales Incorrectas");
            }
        } catch (Exception e) {
            servicioUtileria.mensajeError("Error: "+ e.getMessage());
        }
        return redireccion;

    }

}
