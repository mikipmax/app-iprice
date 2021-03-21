package com.iprice.utils;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.springframework.stereotype.Service;

@Service
public class UtileriaImpl implements UtileriaI{

	@Override
	public void mensajeInfo(String mensaje) {
		anadirMensaje(mensaje, FacesMessage.SEVERITY_INFO);
		
	}

	@Override
	public void mensajeError(String mensaje) {
		anadirMensaje(mensaje, FacesMessage.SEVERITY_ERROR);
		
	}

	@Override
	public void anadirMensaje(String mensaje, Severity severity) {
		FacesMessage mensajeJSF = new FacesMessage();
		mensajeJSF.setSeverity(severity);
		mensajeJSF.setSummary(mensaje);
		FacesContext.getCurrentInstance().addMessage(null, mensajeJSF);
		
	}
		
}
