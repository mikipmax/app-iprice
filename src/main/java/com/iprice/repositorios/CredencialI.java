package com.iprice.repositorios;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.iprice.dto.Credencial;

@Repository
public interface CredencialI extends CrudRepository<Credencial, Integer> {
    List<Credencial> findByCredCedulaAndCredClave(String cedula, String clave);

    List<Credencial> findByCredRolIsTrue();
}
