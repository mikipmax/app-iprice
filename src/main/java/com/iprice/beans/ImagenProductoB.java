package com.iprice.beans;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;

import com.iprice.dto.CategoriaProducto;
import com.iprice.dto.ImagenProducto;
import com.iprice.dto.Producto;
import com.iprice.repositorios.ImagenProductoI;
import com.iprice.repositorios.ProductoI;
import com.iprice.utils.UtileriaI;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;

import org.primefaces.event.FileUploadEvent;


import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component("imprB")
@SessionScope
@Getter
@Setter
public class ImagenProductoB implements Serializable {


    private static final long serialVersionUID = 1L;

    private ImagenProducto imagenProducto;
    private Producto producto;
    private List<ImagenProducto> listaImagenProducto;
    private List<ImagenProducto> listaFiltroProducto;
    private int idProd;

    @Autowired
    ImagenProductoI servicioImagenProducto;
    @Autowired
    ProductoI servicioProducto;
    @Autowired
    private UtileriaI servicioUtileria;

    @PostConstruct
    public void init() {
        imagenProducto = new ImagenProducto();
        listaFiltroProducto = new ArrayList<>();
        producto = new Producto();

    }


    public void leerFila(Producto producto) {
        this.producto = producto;
    }

    public void previoListarPorProducto(Producto producto) {


        listarPorProducto(producto);
    }


    public void listarPorProducto(Producto producto) {
        try {

            listaFiltroProducto = servicioImagenProducto.findByProdId(producto);


        } catch (Exception e) {
            servicioUtileria.mensajeError("Error:" + e.getMessage());
        }

    }

    public void eliminar(ImagenProducto imagen) {
        try {
            servicioImagenProducto.delete(imagen);
            servicioUtileria.mensajeInfo("Imagen eliminada correctamente");
        } catch (Exception e) {
            servicioUtileria.mensajeError("Algo salió mal: " + e.getMessage());
        }
    }

    /**
     * Esté metodo está modificado dado que pf tiene un bug
     * el cual no permite mostrar imagenes dentro de un dataview, datatalbe, datascroll, etc.
     * es ideal para imagenes que vienen de la bd
     *
     * @return un objeto que es capaz de mostrarse en graphicImage de PF
     */
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


    private byte[] convertirBufferedImageToArrayByte(BufferedImage bufImage, String ex) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufImage, ex, baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    public BufferedImage convertirArrayByteToBufferedImage(byte[] fotoByte) throws IOException {
        InputStream in = new ByteArrayInputStream(fotoByte);
        return ImageIO.read(in);
    }

    //Revisar, otra manera de redimensionar imagenes
    public BufferedImage resize(byte[] originalImage) {
        try {
            BufferedImage bimg = convertirArrayByteToBufferedImage(originalImage);
            BufferedImage imagenReducida;
            int nuevoAncho = 200;
            int nuevaAltura = 180;
            imagenReducida = Scalr.resize(bimg, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, nuevoAncho, nuevaAltura);

            return imagenReducida;
        } catch (Exception e) {
            return null;
        }

    }

    //Este metodo es por default para manejar el <p:fileUpload> debe tener como parametro un FileUploadEvent para que funcione
    public void handleFileUpload(FileUploadEvent event) {

        UploadedFile ufile = event.getFile();
        ImagenProducto f = new ImagenProducto();
        byte[] fotoByte = null;
        byte[] fotoByteAux = null;
        if (ufile != null && ufile.getContent() != null && ufile.getContent().length > 0 && ufile.getFileName() != null) {

            try {
                String extension = event.getFile().getContentType().split("/")[1];
                fotoByte = IOUtils.toByteArray(ufile.getInputStream());
                fotoByteAux = convertirBufferedImageToArrayByte(resize(fotoByte), extension);

                f.setImprImg(fotoByteAux);
                f.setProdId(producto);
                servicioImagenProducto.save(f);
                servicioUtileria.mensajeInfo("Imagen subida con éxito");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                producto = new Producto();
            }
        }
    }


}

