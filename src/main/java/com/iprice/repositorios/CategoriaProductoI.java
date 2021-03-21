package com.iprice.repositorios;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.CategoriaProducto;

@Repository
public interface CategoriaProductoI  extends CrudRepository<CategoriaProducto, Integer>{

}
