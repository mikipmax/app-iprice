package com.iprice.repositorios;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iprice.dto.DetallePedido;


@Repository
public interface DetallePedidoI  extends CrudRepository<DetallePedido, Integer>{

}
