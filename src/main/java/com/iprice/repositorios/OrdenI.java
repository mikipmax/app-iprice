package com.iprice.repositorios;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.Orden;


@Repository
public interface OrdenI  extends CrudRepository<Orden, Integer>{

}
