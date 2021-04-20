from sqlalchemy import create_engine
import pandas as pd

# Credentials to connect to the database
usuario = "postgres"
clave = "admin"
hostname = "localhost"
base = "iprice_test"

# Connect to the database of the e-shop
conexion = create_engine("postgresql://{user}:{password}@{host}/{dbname}"
                         .format(user=usuario,
                               password=clave,
                               host=hostname,
                               dbname=base))

# Read the data
data = pd.read_csv('producto.csv',sep=";")
# Write the processed data
data.to_sql(name="producto", con=conexion, if_exists="replace", index=False, chunksize=1000)