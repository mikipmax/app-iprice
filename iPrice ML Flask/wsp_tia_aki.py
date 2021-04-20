import pandas as pd
import requests
from bs4 import BeautifulSoup
import psycopg2
import json
###############################################################################
from pandas import DataFrame
from sqlalchemy import create_engine

# Parámetros de conexión a la bd
username = "postgres"
password = "admin"
hostname = "localhost"
dbname = "iPrice"


################################DEFINICIÓN DE MÉTODOS NECESARIOS###########################
def updateTable(campo, records):
    try:
        connection = psycopg2.connect(user=username,
                                      password=password,
                                      host=hostname,
                                      port="5432",
                                      database=dbname)

        cursor = connection.cursor()

        # Actualizo un registro
        sql_update_query = "Update producto set " + campo + " = %s where prod_id = %s"
        cursor.executemany(sql_update_query, records)
        connection.commit()
        count = cursor.rowcount
        print(count, "Registro(s) actualizado(s) correctamente en el campo "+campo)


    except (Exception, psycopg2.Error) as error:
        print("Error: ", error)

    finally:
        # cerramos conexión con la bd
        if connection:
            cursor.close()
            connection.close()


def preparar_datos_match_aki(data):
    engine = create_engine("postgresql://{user}:{password}@{host}/{dbname}"
                           .format(user=username,
                                   password=password,
                                   host=hostname,
                                   dbname=dbname))
    prod_data = pd.read_sql_table("producto", engine)

    iprice = []
    for i in prod_data.index:
        iprice.append([prod_data['prod_id'][i], prod_data['prod_nombre'][i]])
    return iprice, convertir_df_lista(data)


def convertir_df_lista(data):
    scrap = []
    for i in data.index:
        scrap.append([data['producto'][i], data['precio'][i]])
    return scrap


def wsp_tia_match_bd(scrap):
    # Asignamos valores a los datos
    existentes = [[1, 'DETERGENTE FLORAL CICLON 1.2 KG'],
                  [2, 'CEPILLO DENTAL COLGATE MAXWHITE 2 UNI'],
                  [3, 'DETERGENTE FLORAL DEJA 1.2 KG'],
                  [4, 'SHAMPOO CONTROL TOTAL FAMILY 970 ML'],
                  [5, 'PROTECTOR DIARIO KOTEX 15 UNI'],
                  [6, 'LAVAVAJILLA EN CREMA UVA LAVA 1 KG'],
                  [7, 'DESODORANTE PARA MUJER  ACLARADO NATURAL NIVEA 150 ML'],
                  [8, 'DESINFECTANTE EUCALIPTO SILVESTREOLIMPIA 900 ML'],
                  [9,'PAÑITOS HÚMEDOS ORIGINAL PEQUEÑIN 50 UNI'],
                  [10, 'Crema dental Polar 75 ml x3 unds + Jabón Jolly Clásico x3 unds'],
                  [11, 'DESODORANTE PARA MUJER  BAMBOO REXONA 50 ML']
                  ]

    actualiza = []
    for i in existentes:
        for j in scrap:
            if i[1] == j[0]:
                actualiza.append([float(j[1]), i[0]])

    return actualiza


def wsp_aki_match_bd(scrap, iprice):
    actualiza = []
    for i in iprice:
        for j in scrap:
            print(i[1])
            print(j[0])
            son_iguales=i[1] == j[0]
            if son_iguales:
                actualiza.append([float(j[1]), i[0]])

    return actualiza


def enviar_precio_bd(actualiza, campo):
    df = pd.DataFrame(actualiza, columns=[campo, 'prod_id'])
    df = df.drop_duplicates()
    precio_co = list(df.itertuples(index=False, name=None))
    updateTable(campo, precio_co)


def formatear_datos(nombres_prod, precios_prod):
    l_pre = [x.replace("$", " ") for x in precios_prod]
    data = pd.DataFrame(zip(nombres_prod, l_pre), columns=['producto', 'precio'])
    return data


###################################WEB SCRAPPING DE LA COMPETENCIA EN ACCIÓN############################
headers = {
    "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
}


def wsp_tia():
    # LINKS TIA
    url1 = "https://www.tia.com.ec/cuidado-personal"
    url2 = "https://www.tia.com.ec/cuidado-personal/higiene-bucal/cepillos-e-hilos-dentales/colgate"
    url3 = "https://www.tia.com.ec/supermercado/ciclon"
    url4 = "https://www.tia.com.ec/cuidado-personal/cuidado-del-cabello/shampoo-y-acondicionador/family"
    url5 = "https://www.tia.com.ec/supermercado/limpieza-del-hogar/jabones-detergentes-y-suavizantes/deja"
    url6 = "https://www.tia.com.ec/cuidado-personal/kotex"
    url7 = "https://www.tia.com.ec/supermercado/limpieza-del-hogar/lavavajillas/"
    url8 = "https://www.tia.com.ec/cuidado-personal/nivea/nivea%20desodorante"
    url9 = "https://www.tia.com.ec/supermercado/DESINFECTANTE%20EUCALIPTO%20SILVESTREOLIMPIA"
    url10="https://www.tia.com.ec/bebes/Peque%C3%B1%C3%ADn"
    url11="https://www.tia.com.ec/cuidado-personal/DESODORANTE%20PARA%20MUJER%20BAMBOO%20REXONA%2050%20ML"
    lista_url = [url1, url2, url3, url4, url5, url6, url7, url8, url9,url10,url11]

    nombres_prod = []
    precios_prod = []

    for x in lista_url:
        respuesta = requests.get(x, headers=headers)
        soup = BeautifulSoup(respuesta.text, "html.parser")
        nombres = soup.findAll('span', class_="title")
        precios = soup.findAll('span', class_="unit-price")
        for n in nombres:
            nombres_prod.append(n.text)
        for p in precios:
            precios_prod.append(p.text)

    data = formatear_datos(nombres_prod, precios_prod)
    scrap = convertir_df_lista(data)

    actualiza = wsp_tia_match_bd(scrap)

    enviar_precio_bd(actualiza, "prod_precio_co_2")
    return DataFrame(actualiza, columns=['prod_precio_co_2', 'prod_id'])

def wsp_aki():
    url = "https://www.aki.com.ec/ofertas/?oferta=quincenazo"
    respuesta = requests.get(url, headers=headers)

    soup = BeautifulSoup(respuesta.text, "html.parser")

    productos_l = soup.findAll('div', {"class": "contenedor_producto limpieza quincenazo"})
    productos_h = soup.findAll('div', {"class": "contenedor_producto higiene quincenazo"})
    productos_hg = soup.findAll('div', {"class": "contenedor_producto hogar quincenazo"})

    lista_categorias = [productos_l, productos_h, productos_hg]
    nombres_prod = []
    precios_prod = []
    for categoria in lista_categorias:
        for producto in categoria:
            nombres = producto.find('p', class_="titulo").text
            nombres_prod.append(nombres)
            precios = producto.find('h1', class_="precio-aki").text
            precios_prod.append(precios)

    data = formatear_datos(nombres_prod, precios_prod)

    iprice, scrap = preparar_datos_match_aki(data)

    actualiza = wsp_aki_match_bd(scrap, iprice)
    enviar_precio_bd(actualiza, "prod_precio_co_1")
    return DataFrame(actualiza, columns=['prod_precio_co_1', 'prod_id'])


