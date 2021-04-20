import neural_network as nn
from random import random, uniform
import pandas as pd
import numpy as np
from tensorflow.keras.models import load_model
from sqlalchemy import create_engine

# Parámetros de conexión a la bd
username = "postgres"
password = "admin"
hostname = "localhost"
dbname = "iPrice"


# clase que representa la particula y sus operaciones correspondientes al PSO
class Particle:
    def __init__(self, position, velocity):
        self.position = position
        self.velocity = velocity
        self.pbest_position = position
        self.gbest_position = position
        self.pbest_fitness = 0
        self.gbest_fitness = 0

    def evaluate(self, obj_function, steady, cost, model):
        fitness = obj_function(self.position, steady, cost, model)
        # Check the personal best
        if fitness > self.pbest_fitness:
            self.pbest_fitness = fitness
            self.pbest_position = self.position
        # Check the global best
        if fitness > self.gbest_fitness:
            self.gbest_fitness = fitness
            self.gbest_position = self.position

    def update_velocity(self, w, c1, c2):
        r1 = random()
        r2 = random()
        cognitive = c1 * r1 * (self.pbest_position - self.position)
        social = c2 * r2 * (self.gbest_position - self.position)
        self.velocity = (w * self.velocity) + cognitive + social

    def update_position(self, bounds):
        low_bound = bounds[0]
        high_bound = bounds[1]
        self.position = self.position + self.velocity
        # adjust minimum position if needed
        if self.position < low_bound:
            self.position = low_bound
        # adjust maximum position if needed
        if self.position > high_bound:
            self.position = high_bound


def particle_swarm(number_of_particles, c1, c2, w_min, w_max, iterations, obj_function, steady, cost, model, bounds):
    '''
        Método que se encarga de manejar el enjambre de partículas
        número de partículas: es la población de partículas que intentarán maximizar la fusión
        c1: componente cognitivo de una partícula
        c2: componente social de una partícula
        w_min: inercia mímina
        w_max: inercia máxima
        iterations: número de iteraciones para lograr la convergencia
        obj_function: es la función que deseamos maximizar
        bounds: límites para la posición
        return: la mejor posición y el valor máximo de la función
    '''
    # Inicializamos el enjambre
    swarm = []
    # Recorremos el número de partículas elegidas
    for i in range(number_of_particles):
        position = round(uniform(bounds[0], bounds[1]), 2)
        velocity = round(uniform(-0.5, 0.5), 2)
        swarm.append(Particle(position, velocity))

    for i in range(iterations):
        w = w_max - i * ((w_max - w_min) / iterations)

        for p in range(number_of_particles):
            swarm[p].evaluate(obj_function, steady, cost, model)

            swarm[p].update_velocity(w, c1, c2)

            swarm[p].update_position(bounds)

    best_position = swarm[number_of_particles - 1].gbest_position
    best_value = swarm[number_of_particles - 1].gbest_fitness

    return (best_position, best_value)


def gain(price, steady, cost, model):
    """
    Este método usa el precio, el costo del producto y el modelo que devuelve la demanda que se predijo con la RN
    """
    temp = np.array(steady)
    temp = np.append(temp, price)
    temp = np.reshape(temp, (1, len(temp)))
    quantity = np.round_(model.predict(temp))

    result = (price - cost) * quantity

    return float(np.round_(result, 2))


def optimize_prices(data, opt_data, nn_data):
    """
    Método que optimizara el precio
    """

    # LLamamos al método que entrena la red neuronal
    product_encoder = nn.nn_final_training(nn_data.copy())
    data["prod_id"] = product_encoder.transform(data["prod_id"])
    # Cargamos el modelo entrenado
    model = load_model("final_model.h5")

    # Preparamos las columnas para el nuevo df
    columns = ["prod_id", "prod_nombre","capr_id" ,
               "prod_precio_co_1","prod_precio_co_2",
               "limite_inferior", "limite_superior",
               "precio_optimizado", "demanda_predicha",
               "ganancia_prevista", "prod_precio_norm", "prod_precio_fin"]
    optimization_results = pd.DataFrame(columns=columns)

    for index, row in data.iterrows():
        print(index)
        upper_bound = row.product_max_bound
        lower_bound = row.product_min_bound
        # Contiene pn y qn de cada producto
        steady = row[2:-1]
        cost = row.product_cost
        # Limites de precio inferior y superior
        bounds = (lower_bound, upper_bound)
        number_of_swarms = 40
        c1 = 0.4
        c2 = c1
        w_min = 0.6
        w_max = 0.8

        best_position, best_value = particle_swarm(number_of_swarms, c1, c2,
                                                   w_min, w_max, 100, gain,
                                                   steady, cost, model, bounds)
        temp = np.array(steady)
        temp = np.append(temp, best_position)
        temp = np.reshape(temp, (1, len(temp)))
        predicted_quantity = int(np.round_(model.predict(temp)))

        optimization_results.loc[index, "prod_id"] = row["prod_id"]
        optimization_results.loc[index, "limite_inferior"] = float(bounds[0])
        optimization_results.loc[index, "limite_superior"] = float(bounds[1])
        optimization_results.loc[index, "precio_optimizado"] = float(round(best_position, 2))
        optimization_results.loc[index, "demanda_predicha"] = predicted_quantity
        optimization_results.loc[index, "ganancia_prevista"] = round(best_value, 2)

    optimization_results["prod_id"] = product_encoder.inverse_transform(data["prod_id"])

    for index, row in optimization_results.iterrows():
        prod_nombre = opt_data.loc[opt_data["prod_id"] == row["prod_id"]].prod_nombre.values
        capr_id = opt_data.loc[opt_data["prod_id"] == row["prod_id"]].capr_id.values
        prod_precio_co_1 = opt_data.loc[opt_data["prod_id"] == row["prod_id"]].prod_precio_co_1.values
        prod_precio_co_2 = opt_data.loc[opt_data["prod_id"] == row["prod_id"]].prod_precio_co_2.values
        prod_precio_norm = opt_data.loc[opt_data["prod_id"] == row["prod_id"]].prod_precio_norm.values
        prod_precio_norm = round(float(prod_precio_norm), 2)
        prod_precio_fin = opt_data.loc[opt_data["prod_id"] == row["prod_id"]].prod_precio_fin.values
        prod_precio_fin = round(float(prod_precio_fin), 2)
        optimization_results.loc[index, "prod_precio_norm"] = prod_precio_norm
        optimization_results.loc[index, "prod_precio_fin"] = prod_precio_fin
        optimization_results.loc[index, "prod_nombre"] = prod_nombre
        optimization_results.loc[index, "capr_id"] = capr_id
        optimization_results.loc[index, "prod_precio_co_1"] = prod_precio_co_1
        optimization_results.loc[index, "prod_precio_co_2"] = prod_precio_co_2
    return optimization_results
