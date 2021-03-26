package com.iprice.repositorios;

import com.iprice.dto.Producto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.ImagenProducto;

import java.util.List;


@Repository
public interface ImagenProductoI  extends CrudRepository<ImagenProducto, Integer>{
List<ImagenProducto> findByProdId(Producto prodId);
}
