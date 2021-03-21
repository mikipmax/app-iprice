package com.iprice.repositorios;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.ImagenProducto;


@Repository
public interface ImagenProductoI  extends CrudRepository<ImagenProducto, Integer>{

}
