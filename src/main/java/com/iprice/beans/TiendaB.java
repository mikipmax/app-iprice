package com.iprice.beans;


import com.iprice.dto.*;
import com.iprice.repositorios.*;
import com.iprice.utils.UtileriaI;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.view.ViewScoped;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@SessionScope
@Component("tiendaB")
public class TiendaB implements Serializable {

    private static final long serialVersionUID = 1L;
    private Producto producto;
    private Producto productoSelect;
    private DetallePedido detallePedido;
    private List<Producto> listaProductos;
    private List<ImagenProducto> listaFiltroProducto;
    private Set<DetallePedido> listaDetallePedido;
    private List<Producto> listaProductoEnCarrito;
    private double total;
    SesionValidaB sesion;
    private Orden orden;
    private String texto;
    @Autowired
    private ProductoI productoServicio;
    @Autowired
    private UtileriaI utileriaServicio;
    @Autowired
    private ImagenProductoI servicioImagenProducto;
    @Autowired
    private PersonaI servicioPersona;
    @Autowired
    private OrdenI servicioOrden;
    @Autowired
    private DetallePedidoI servicioDetallePedido;

    @PostConstruct
    public void init() {
        producto = new Producto();
        productoSelect = new Producto();
        listar();
        listaFiltroProducto = new ArrayList<>();
        listaDetallePedido = new HashSet<>();
        listaProductoEnCarrito = new ArrayList<>();

        sesion = new SesionValidaB();
        detallePedido = new DetallePedido();

        orden = new Orden();
    }

    public String buscarPersonaSesion(int id) {
        Persona personaSesion = null;
        Optional<Persona> personaAux = servicioPersona.findById(id);
        if (personaAux.isPresent()) {
            personaSesion = personaAux.get();
        }

        return personaSesion.getPersNombre();
    }

    public void listar() {

        try {
            listaProductos = (List<Producto>) productoServicio.findAll();

        } catch (Exception e) {
            utileriaServicio.mensajeError("Error:" + e.getMessage());
        }

    }

    public List<ImagenProducto> listarPorProducto(Producto producto) {

        try {

            listaFiltroProducto = servicioImagenProducto.findByProdId(producto);

        } catch (Exception e) {
            utileriaServicio.mensajeError("Error:" + e.getMessage());
        }
        return listaFiltroProducto;
    }

    public StreamedContent mostrarFoto() {

        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        } else {

            String id = context.getExternalContext().getRequestParameterMap()
                    .get("pid");

            int imprId = Integer.valueOf(id);
            Optional<ImagenProducto> imprTemp = servicioImagenProducto.findById(imprId);
            byte[] image = null;
            if (imprTemp.isPresent()) {
                image = imprTemp.get().getImprImg();
            }

            byte[] finalImage = image;
            return DefaultStreamedContent.builder()

                    .stream(() -> {
                        if (finalImage == null) {

                            return null;
                        }
                        try {
                            return new ByteArrayInputStream(finalImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .build();
        }

    }

    public void buscador() {

        listaProductos = productoServicio.buscarProducto(texto.toLowerCase());
        listaProductos.forEach(s -> System.out.println(s));
    }

    public void agregarCarrito(Producto producto) {

        if (producto != null) {
            listaProductoEnCarrito.add(producto);
        }
    }

    public void segmentarCarrito() {
        DetallePedido depe = null;
        listaDetallePedido = new HashSet<>();
        int cantidad = 0;
        for (Producto producto : listaProductoEnCarrito) {
            depe = new DetallePedido();
            cantidad = (int) listaProductoEnCarrito.stream().filter(productoAux -> producto.equals(productoAux)).count();
            depe.setDepeCantidad(cantidad);
            depe.setProdId(producto);
            Orden o = new Orden();
            depe.setOrdeId(o);
            depe.setDepePrecio(producto.getProdPrecioNorm());
            listaDetallePedido.add(depe);
        }
        total = 0;
        for (DetallePedido depeAux : listaDetallePedido) {
            total += (depeAux.getDepeCantidad()) * depeAux.getProdId().getProdPrecioNorm();
            total=utileriaServicio.redondearDecimales(total,2);
        }

    }

    public LocalDate obtenerFechaActual() {
        return LocalDate.now();
    }

    public void generarCompra() {
        try {
            if (sesion.objetoSesion() != null) {
                orden = new Orden();
                orden.setOrdeFecha(obtenerFechaActual());
                orden.setClienteId(new Persona(sesion.objetoSesion().getCredId()));
                servicioOrden.save(orden);
                if (orden.getOrdeId() != 0 && !listaDetallePedido.isEmpty()) {
                    for (DetallePedido depe : listaDetallePedido) {
                        depe.setOrdeId(orden);
                        servicioDetallePedido.save(depe);
                    }
                }
                utileriaServicio.mensajeInfo("Compra realizada con éxito");
            }

        } catch (Exception e) {
            utileriaServicio.mensajeError("Algo salió mal: " + e.getMessage());
        } finally {
            orden = new Orden();
            detallePedido = new DetallePedido();
            listaDetallePedido = new HashSet<>();
            listaProductoEnCarrito = new ArrayList<>();
            total = 0;
        }


    }

}
