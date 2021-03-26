package com.iprice.repositorios;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.Producto;

import java.util.List;

@Repository
public interface ProductoI extends CrudRepository<Producto, Integer> {
    @Query(value = "select * from producto where lower(prod_nombre) like %?1%", nativeQuery = true)
    List<Producto> buscarProducto(String text);

}
