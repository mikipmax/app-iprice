package com.iprice.repositorios;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.Localidad;


@Repository
public interface LocalidadI  extends CrudRepository<Localidad, Integer>{

}
