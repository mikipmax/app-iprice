package com.iprice.beans;


import com.iprice.dto.CategoriaProducto;
import com.iprice.repositorios.CategoriaProductoI;
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
@Component("caprB")
public class categoriaProductoB implements Serializable {

    private static final long serialVersionUID = 1L;
    private CategoriaProducto categoriaProducto;
    private CategoriaProducto categoriaProductoSelect;
    private List<CategoriaProducto> listaCategoriaProducto;
    @Autowired
    private CategoriaProductoI categoriaProductoServicio;
    @Autowired
    private UtileriaI utileriaServicio;

    @PostConstruct
    public void init() {
        categoriaProducto = new CategoriaProducto();
        categoriaProductoSelect = new CategoriaProducto();
        listar();

    }

    public void listar() {

        try {
            listaCategoriaProducto = (List<CategoriaProducto>) categoriaProductoServicio.findAll();

        } catch (Exception e) {
            utileriaServicio.mensajeError("Error:" + e.getMessage());
        }

    }

    public void crear() {

        try {
            categoriaProductoServicio.save(categoriaProducto);
            utileriaServicio.mensajeInfo("Registro guardado correctamente");
        } catch (Exception e) {
            utileriaServicio.mensajeError("No sé ha podido guardar:" + e.getMessage());

        } finally {

            listar();
            categoriaProducto = new CategoriaProducto();
        }

    }


    public void actualizar() {

        try {
            categoriaProductoServicio.save(categoriaProductoSelect);
            utileriaServicio.mensajeInfo("Registro actualizado correctamente");
        } catch (Exception e) {
            utileriaServicio.mensajeError("No sé ha podido actualizar:" + e.getMessage());
        } finally {
            categoriaProductoSelect = new CategoriaProducto();
            listar();
        }

    }

    public void leerFila(CategoriaProducto categoriaProducto) {
        categoriaProductoSelect = categoriaProducto;

    }

}
