package com.iprice.beans;

import java.io.Serializable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.iprice.dto.Credencial;


@SessionScope
@Component("SesionValidaB")
public class SesionValidaB implements Serializable {

	private static final long serialVersionUID = 1L;

	public void verificarSesionAdmin() {
		verificarSesion(true);
	}

	public void verificarSesionCliente() {
		verificarSesion(false);
	}

	public void verificarSesion(boolean rol) {
		try {
			ExternalContext exContext = FacesContext.getCurrentInstance().getExternalContext();
			Credencial acceso = (Credencial) exContext.getSessionMap().get("usuario");
			if (acceso != null) {

				if (acceso.isCredRol() != rol) {
					exContext.redirect("./login.xhtml");

				}

			} else {
				exContext.redirect("./login.xhtml");
			}

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public void cerrarSesion() {

		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
	}

	public Credencial objetoSesion() {
		Credencial cred = new Credencial();
		try {
			ExternalContext exContext = FacesContext.getCurrentInstance().getExternalContext();
			cred = (Credencial) exContext.getSessionMap().get("usuario");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return cred;
	}

}
