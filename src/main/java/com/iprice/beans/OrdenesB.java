package com.iprice.beans;


import com.iprice.dto.DetallePedido;

import com.iprice.repositorios.DetallePedidoI;
import com.iprice.utils.UtileriaI;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ViewScoped
@Component("ordenB")
public class OrdenesB implements Serializable {

    private static final long serialVersionUID = 1L;
    private DetallePedido detallePedido;

    private List<DetallePedido> listaDepe;
    @Autowired
    private DetallePedidoI depeServicio;
    @Autowired
    private UtileriaI utileriaServicio;

    @PostConstruct
    public void init() {
        detallePedido = new DetallePedido();

        listar();

    }

    public void listar() {

        try {
            listaDepe = (List<DetallePedido>) depeServicio.findAll();

        } catch (Exception e) {
            utileriaServicio.mensajeError("Error:" + e.getMessage());
        }

    }


}
