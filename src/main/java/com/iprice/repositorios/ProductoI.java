package com.iprice.repositorios;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.Producto;

@Repository
public interface ProductoI  extends CrudRepository<Producto, Integer>{

}
