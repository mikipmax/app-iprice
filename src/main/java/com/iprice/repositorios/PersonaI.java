package com.iprice.repositorios;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iprice.dto.Persona;
@Repository
public interface PersonaI  extends CrudRepository<Persona, Integer>{

}
