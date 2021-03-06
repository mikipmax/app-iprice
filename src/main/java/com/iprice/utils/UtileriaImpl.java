package com.iprice.utils;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class UtileriaImpl implements UtileriaI {

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

    @Override
    public double redondearDecimales(double valorInicial, int numeroDecimales) {
        BigDecimal numeroTemp = BigDecimal.valueOf(valorInicial).setScale(numeroDecimales, RoundingMode.HALF_UP);
        return numeroTemp.doubleValue();
    }

}
