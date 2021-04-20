import pandas as pd
from sqlalchemy import create_engine
import dateparser
import numpy as np

# Configuración de presentación de los dataframes
pd.set_option('display.width', 400)
pd.set_option('display.max_columns', 11)

# Parámetros de conexión a la bd
username = "postgres"
password = "admin"
hostname = "localhost"
dbname = "iPrice"


def limpiarData():
    """
    Método que se encarga de redondear el precio
    a dos decimales, eliminar NAN y valores con infinito
    """
    # Creamos conexión con la bd
    engine = create_engine("postgresql://{user}:{password}@{host}/{dbname}"
                           .format(user=username,
                                   password=password,
                                   host=hostname,
                                   dbname=dbname))

    data=pd.read_sql_query("select  orde.* ,depe.prod_id, depe.depe_cantidad, depe.depe_precio "
                           "from detalle_pedido depe, orden orde "
                           "where depe.orde_id=orde.orde_id",engine)

    opt_data=pd.read_sql_table("producto", engine)
    # Redondeo a 2 decimales
    data.depe_precio = round(data.depe_precio, 2)
    # Buscamos los registros con precio 0
    zero_price_data = data.where(data.depe_precio == 0.0).dropna(axis=0)
    # Removemos registros con precio 0
    data.drop(zero_price_data.index, axis=0, inplace=True)
    data.index = range(len(data))
    with pd.option_context('mode.use_inf_as_na', True):
        data = data.dropna(axis=0)  # Removemos datos con valor infinito o na

    data.index = range(len(data))
    return data,opt_data


def filtrar_n_productos_vendidos(data, num_of_sales):
    """
    Mëtodo que filtra los n productos vendidos a lo largo de todo el histórico,
    esto con el fin de tener un mejor dataset para entrenar, además de asignar su correspondiente semana
    en la que cada producto fue vendido
    NOTA: por el momento 1, luego poner en 100 al menos
    """


    data.loc[:, 'orde_fecha'] = pd.to_datetime(data.loc[:, 'orde_fecha'], format='%Y-%m-%d')
    data.loc[:, 'orde_fecha'] = data.loc[:, 'orde_fecha'].dt.strftime('%d/%m/%Y')

    # Calculamos el total de ventas por producto
    products_with_high_vol = data.groupby(["prod_id"]).depe_cantidad.sum()
    # Nos quedamos únicamente con los productos que han sido vendidos más de n veces
    products_with_high_vol = products_with_high_vol.where(products_with_high_vol >= num_of_sales).dropna(axis=0)
    data = data.set_index("prod_id").join(products_with_high_vol,
                                             rsuffix="_total")  # Configuramos el index (product_id+product_quantity_total)
    data.dropna(axis=0, inplace=True)

    data.drop(columns="depe_cantidad_total", inplace=True)  # Eliminamos la columna product_quantity_total

    # A continuación se Buscará el número de semana en la que los productos fueron vendidos
    data.sort_values(by="orde_fecha", inplace=True)  # Ordenamos por fecha de venta
    # Obtenemos la primer fecha de venta del histórico
    first_date = dateparser.parse(data.iloc[0].orde_fecha)
    # Obtenemos la última fecha de venta dle histórica
    last_date = dateparser.parse(data.iloc[-1].orde_fecha)
    # Verifica si el total de días del histórico contienen semanas enteras, es como un factor de ajuste
    shift = 6 - ((last_date - first_date).days % 7)  # 5 si no son semanas enteras y 6 caso contrario
    data["week"] = 0
    weeks = []

    # En este for calcula el número de semana según la fecha de venta
    for i in range(data.shape[0]):
        date = dateparser.parse(data.iloc[i].orde_fecha)
        week = (((date - first_date).days + shift) // 7) + 1  # week empieza desde 1
        weeks.append(week)
    data["week"] = weeks

    data["prod_id"] = data.index

    data.index = range(len(data))
    # Removemos datos con valor infinito o na
    with pd.option_context('mode.use_inf_as_na', True):
        data = data.dropna(axis=0)

    data.index = range(len(data))

    return data


def crear_data_semanal(data):
    """
    Mëtodo que se encarga de agrupar la demanda por producto y precio por semana, el cual calcula
     el costo del producto y el límite del precio máximo
    """

    data["orde_fecha"] = data["orde_fecha"].astype("str")
    print(data)
    # Agrupamos la demanda por producto y semana
    demand_data = data.groupby(["prod_id", "week"]).depe_cantidad.sum()
    print(demand_data)
    # Dado que puede haber diferentes precios en una misma semana se opta por sacar el promedio semanal por producto
    price_data = data.groupby(["prod_id", "week"]).depe_precio.mean().round(2)

    # Construimos un nuevo df que contendra la demanda por producto y precio por semana
    week_data = pd.concat([demand_data, price_data], axis=1)
    week_data = week_data.reset_index(level=["prod_id", "week"])

    # Como no sabemos el costo del producto, lo asuminmos a partir del 80% del precio más bajo del histórico de cada roducto
    products = week_data.prod_id.unique()
    cost = pd.Series(dtype='float64')
    max_prices = pd.Series(dtype='float64')
    # Buscamos el precio mínimo y máximo a lo largo del histórico para cada producto
    for product in products:
        min_price = week_data.loc[week_data.prod_id == product].depe_precio.min()
        max_price = week_data.loc[week_data.prod_id == product].depe_precio.max()
        # Obtenemos los indices de cada uno de los productos que están agrupados por semana
        temp_ind = week_data.loc[week_data.prod_id == product].index
        # Calculamos el costo y el límite máximo de cada producto
        for i in temp_ind:

            cost.loc[i] = round(0.75 * min_price, 2)
            # Aquí igual podemos parametrizar el limite del precio máximo en vez de 1.2, se podría tomar desde el panel admin del iprice
            max_prices.loc[i] = round(1.3 * max_price, 2)
            # NOTA: También, podriamos ahorrarnos este for interno, y enviar directamnte el costo y precio máximo a nuestra tabla producto
            #       Esto se debe a que siempre pondra el mismo costo y precio maximo correspondiente en los productos que se repiten en el historial
    week_data["product_cost"] = cost


    week_data["product_max_bound"] = max_prices #REVISAR SI SE USA EN EL PSO PARA PONER AQUI EL PRECIO COMP 1 Y 2

    return week_data


def full_weeks(missing_weeks, total_weeks):
    """
    Evalua cada producto con respecto a toda el histórico de datos, ubica ceros en las semanas
    donde no se ha vendido
    """
    full_weeks = pd.DataFrame(columns=missing_weeks.columns)

    # Recorro todas las semanas del histórico, como el for empieza en 1 se le suma uno a total_weeks para compensar
    for week in range(1, total_weeks + 1):
        flag = True
        # Compruebo que cada producto tenga ventas en cada una de las semanas de histórico

        if missing_weeks.loc[missing_weeks["week"] == week].size == 0:
            flag = False
        temp = []


        temp.append(missing_weeks.loc[0]["prod_id"])

        temp.append(week)

        # Si no hay ventas del producto en la semana correspondiente, se le asigna 0,
        # caso contrario pasa la demanda y precio correspondiente
        if flag:
            temp.append(missing_weeks.loc[missing_weeks["week"] == week].depe_cantidad.values[0])

            temp.append(missing_weeks.loc[missing_weeks["week"] == week].depe_precio.values[0])

        else:
            temp.append(0)
            temp.append(0)

        # Agregamos las columnas correspondientes al costo del prodcuto y su limite de precio máximo
        temp.append(missing_weeks.loc[0]["product_cost"])

        temp.append(missing_weeks.loc[0]["product_max_bound"])


        full_weeks.loc[(week - 1)] = temp


    return full_weeks.copy()


def nn_row(row, full_weeks, number_of_weeks):
    """
    Mëtodo que se encarga de rellenar los valores correspondientes a pi y qi, basándose en el filtro de las
    últimas n semanas elegidas
    """

    # Seleccionamos las columnas que se aplicarán al nuevo df
    row_data = []
    row_data.append(row["week"])
    row_data.append(row["product_cost"])
    row_data.append(row["product_max_bound"])
    row_data.append(int (row["prod_id"]))

    week = row["week"]  # Obtenemos la semana que contienen ventas de cada fila
    # Genera una matriz que contiene el precio y la demanda por cada una del número de semanas elegidos
    weeks = np.ndarray(shape=(number_of_weeks, 2))

    # Recorremos el número de semanas elegido
    for i in range(1, number_of_weeks + 1):
        temp_week = week - i

        # Se filtra las últimas n semanas elegidas y se les ubica en las correspondientes columnas pi, qi
        if temp_week < 1:
            p = 0
            q = 0
        else:
            # product_price.values es un vector de un solo elemento
            # Tomamos el valor de full_weeks, buscando el número de semana evaluado en dicho df
            p = full_weeks.loc[full_weeks["week"] == temp_week].depe_precio.values[0]

            q = full_weeks.loc[full_weeks["week"] == temp_week].depe_cantidad.values[0]
        # Ubica los valores desde p(n), q(n) hacia p(n-1), q(n-1)
        weeks[(number_of_weeks - i), 0] = p
        weeks[(number_of_weeks - i), 1] = q

    # Agregamos el precio y la demanda a la tabla row_data
    for i in range(number_of_weeks):
        row_data.append(weeks[i, 0])
        row_data.append(weeks[i, 1])

    # Aquí llena p(n+1) y q(n+1) con el precio y demanda correspondiente a cada venta
    row_data.append(row["depe_precio"])
    row_data.append(row["depe_cantidad"])

    return row_data.copy()


def crear_nn_data(week_data, number_of_weeks=16):
    # Creamos conexión con la bd
    engine = create_engine("postgresql://{user}:{password}@{host}/{dbname}"
                           .format(user=username,
                                   password=password,
                                   host=hostname,
                                   dbname=dbname))
    # Obtiene el número máximo de semanas en base al histórico
    total_weeks = week_data.week.max()
    # Declara las columnas base que se usará para la tabla nn_data
    columns = ["week", "product_cost", "product_max_bound", "prod_id"]

    # Aquí creamos columnas p y q el número de semanas elegidas, le suma 2 porque se agrega un p y q extra
    # Tendrá columnas que van desde p1, q1, p2, q2, ....., pn, qn
    for i in range(1, number_of_weeks + 2):
        columns.append("P{:d}".format(i))
        columns.append("Q{:d}".format(i))
    # Agregamos las columnas creadas a nuestro df
    nn_data = pd.DataFrame(columns=columns)

    # Filtramos los productos distintos que hay en el df weed_data
    products = week_data.prod_id.unique()

    for product in products:
        # Segmenta las semanas vendidas de cada producto, incluyendo todas las columnas ya establecidas
        temp_product = week_data.loc[week_data["prod_id"] == product]
        temp_product.index = range(temp_product.shape[0])

        # Se envia a evaluar al metodo full_weeks cada segmento de productos (semanas en que se vendió tal producto)
        # con el número máximo de semanas de todo el histórico
        full_week = full_weeks(temp_product.copy(), total_weeks)

        # Se prepara el nuevo df con las columnas creadas y llenadas hasta el momento
        temp_data = pd.DataFrame(columns=columns)

        # Recorre por cada producto, el número de ventas que ha tenido por semana en todo el histórico
        for index, row in temp_product.iterrows():
            # Se llama al método nn_row para rellenar las columnas pi y qi correspondientes
            temp_data.loc[index] = nn_row(row, full_week.copy(), number_of_weeks)
        # Agregamos a la tabla nn_data las columnas anteriormente tratadas

        nn_data = nn_data.append(temp_data, ignore_index=True)

        nn_data["prod_id"] = nn_data["prod_id"].astype("int")
        nn_data["week"] = nn_data["week"].astype("int")

        nn_data.to_sql(name="tarci", con=engine, if_exists="replace", index=False, chunksize=1000)

    return nn_data.copy()


def pso_data(nn_data,opt_data):
    # Creamos conexión con la bd
    engine = create_engine("postgresql://{user}:{password}@{host}/{dbname}"
                           .format(user=username,
                                   password=password,
                                   host=hostname,
                                   dbname=dbname))

    # Obtemos la semana máximo del histórico
    total_weeks = nn_data.week.max()
    # Obtenemos el número de semans a partir del número de columnas de nn_data
    number_of_weeks = int((nn_data.shape[1] - 6) / 2)

    #Calculamos todos los productos en base a su ultima semana
    prods = opt_data["prod_id"]
    data = nn_data.iloc[0:0]
    # Recorro cada uno de los productos
    for prod in prods:
        dataAux = nn_data.loc[nn_data["prod_id"] == prod].copy()
        max_week = dataAux.week.max()
        dataTemp = nn_data.loc[(nn_data["week"] == max_week) & (nn_data["prod_id"]==prod)].copy()
        data = data.append(dataTemp,ignore_index=True)

    # Se filtra solo aquellos productos que se han vendido en la última semana del histórico
    #data = nn_data.loc[nn_data["week"] == total_weeks].copy()
    data.index = range(len(data))

    # Recorremos el número n de semanas elegidas
    for i in range(1, number_of_weeks + 1):
        # Guardamos en pi y qi los valores correspondientes a p(i+1) y q(i+1)
        data.loc[:, f"P{i}"] = data[f"P{i + 1}"]
        data.loc[:, f"Q{i}"] = data[f"Q{i + 1}"]

    # Borra las columanas p(n+1) y q(n+1) correspondientes
    data.drop(columns=["week", f"P{number_of_weeks + 1}", f"Q{number_of_weeks + 1}"], inplace=True)
    pso = pd.DataFrame(columns=data.columns)  # Cargamos el csv con las columnas elegidas

    # Definimos la variable que representa el límite del precio mínimo
    pso["product_min_bound"] = 0



    # Seleccionamos el id de productos de la tabla opt_data (En nuestro caso es la misma tabla productos)
    products = opt_data["prod_id"]
    # Recorro cada uno de los productos
    for product in products:

        # Solo se seleccionan productos que se hayan vendido la última semana del histórico
        temp = data.loc[data["prod_id"] == product].copy()
        # Precio actual (sea puesto por la tienda o el que se optimzó anteriormente)
        precio_inicial = opt_data.loc[opt_data["prod_id"] == product].prod_precio_norm.values
        # Precio final (es un precio al que se le ha aplicado algún descuento)
        # IMPORTANTE: AQUI SE PODRÍA APLICAR EL PRECIO DE LOS COMPETIDORES, INCLUSIVE EL PRECIO DESCUENTO POR PROVINCIA
        precio_final = opt_data.loc[opt_data["prod_id"] == product].prod_precio_fin.values

        percentage = 1 - (precio_final / precio_inicial)

        max = (1 - (percentage - 0.1)) * precio_inicial
        min = (1 - (percentage + 0.1)) * precio_inicial
        
        #Competencia
        aux1 = opt_data.loc[opt_data["prod_id"] == product].prod_precio_co_1.values
        aux2 = opt_data.loc[opt_data["prod_id"] == product].prod_precio_co_2.values

        if aux1 == 0 or aux2 == 0:
            if max > precio_inicial:
                max = precio_inicial
            else:
                #Establecemos que co1 sea menor que co2
                if aux1 < aux2:
                    co1 = aux1
                    co2 = aux2
                else:
                    co1=aux2
                    co2=aux1
                #Rango de valores
                maxAux = (co1 + co2) / 2
                if max>maxAux and maxAux>min:
                    max = maxAux
                else:
                    if max > precio_inicial:
                        max = precio_inicial

        
        # Asigno el máximo a la columna product_max_bound (es dif al product_max_bound de nn_data anteriormente calculado)
        temp.loc[:, "product_max_bound"] = round(float(max), 2)
        # Asigno el mínimo a la columna product_min_bound
        temp["product_min_bound"] = round(float(min), 2)
        pso = pso.append(temp)

    pso.index = range(len(pso))

    return pso
