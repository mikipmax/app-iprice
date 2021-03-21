package com.iprice.utils;
import javax.faces.application.FacesMessage.Severity;

public interface UtileriaI {
	
	void mensajeInfo(String mensaje);

	void mensajeError(String mensaje);

	void anadirMensaje(String mensaje, Severity severity);

}
