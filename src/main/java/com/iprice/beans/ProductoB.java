package com.iprice.beans;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import javax.faces.view.ViewScoped;

import com.iprice.dto.CategoriaProducto;
import com.iprice.repositorios.CategoriaProductoI;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.iprice.dto.PrecioDinamico;
import com.iprice.dto.Producto;

import com.iprice.repositorios.ProductoI;
import com.iprice.utils.UtileriaI;
import com.iprice.utils.UtileriaImpl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.context.annotation.SessionScope;

@Getter
@Setter
@SessionScope
@Component("productoB")
public class ProductoB implements Serializable {

    private static final long serialVersionUID = 1L;
    private Producto producto;
    private Producto productoSelect;


    private CategoriaProducto categoriaProducto;
    private List<Producto> listaProducto;
    private List<CategoriaProducto> listaCategoriaProductos;
    List<PrecioDinamico> listaPrecioDinamico;
    @Autowired
    private ProductoI productoServicio;
    @Autowired
    private UtileriaI utileriaServicio;
    @Autowired
    private CategoriaProductoI servicioCategoriaProducto;
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        producto = new Producto();
        productoSelect = new Producto();
        categoriaProducto = new CategoriaProducto();
        listar();
        restTemplate = new RestTemplate();
        listaPrecioDinamico = new ArrayList<>();
        listaCategoriaProductos = (List<CategoriaProducto>) servicioCategoriaProducto.findAll();

    }

    public void listar() {

        try {
            listaProducto = (List<Producto>) productoServicio.findAll();

        } catch (Exception e) {
            utileriaServicio.mensajeError("Error:" + e.getMessage());
        }

    }

    public void crear() {

        try {

            Optional<CategoriaProducto> categoria = servicioCategoriaProducto.findById(categoriaProducto.getCaprId());

            if (categoria.isPresent()) {
                categoriaProducto = categoria.get();
            }
            producto.setCaprId(categoriaProducto);
            productoServicio.save(producto);
            utileriaServicio.mensajeInfo("Registro guardado correctamente");
        } catch (Exception e) {
            utileriaServicio.mensajeError("No s� ha podido guardar:" + e.getMessage());

        } finally {

            listar();
            producto = new Producto();
        }

    }

    public void actualizar() {

        try {
            productoSelect.setCaprId(categoriaProducto);
            productoServicio.save(productoSelect);
            utileriaServicio.mensajeInfo("Registro actualizado correctamente");
        } catch (Exception e) {
            utileriaServicio.mensajeError("No s� ha podido actualizar:" + e.getMessage());
        } finally {
            productoSelect = new Producto();
            listar();
        }

    }

    public void leerFila(Producto producto) {
        productoSelect = producto;
        categoriaProducto.setCaprId(producto.getCaprId().getCaprId());
    }

    public void consumirApi() {

        listaPrecioDinamico=new ArrayList<>();

		Stream.of(restTemplate.getForObject("http://127.0.0.1:5000/precio-opt", PrecioDinamico[].class)).forEach(s -> {
			listaPrecioDinamico.add(s);
		});
        utileriaServicio.mensajeInfo("Lista de precios dinámicos cargada");
        PrimeFaces.current().ajax().update("formListar:tablePricing");

    }
}
