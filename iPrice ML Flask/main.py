import data as dt
import pso as ps
from flask import Flask, Response
import wsp_tia_aki as wsp
import pandas as pd

app = Flask(__name__)


@app.route("/precio-opt", methods=['GET'])
def server_pricing():
    try:

        data_limpia, opt_data = dt.limpiarData()

        data = dt.filtrar_n_productos_vendidos(data_limpia, 60)

        week_data = dt.crear_data_semanal(data)

        nn_data = dt.crear_nn_data(
            week_data)  # Importante solo funciona con la versión 1 de tensorflow, con la 2 se demorá demasiado en ejecutar
        # nn.nn_testing(nn_data)
        pso_data = dt.pso_data(nn_data, opt_data)

        data_optimizada = ps.optimize_prices(pso_data, opt_data, nn_data)
        resp = Response(response=data_optimizada.to_json(orient="records"),
                        status=200,
                        mimetype="application/json")
        return (resp)



    except Exception as e:
        print("Oops!", e.__class__, "Ocurrió.")
    


#@app.route("/wsp")
#def server_wsp():
#   try:
#       d1 = wsp.wsp_aki()
#       d2 = wsp.wsp_tia()
#       resultado = pd.merge(d1, d2, on='prod_id')
#       return resultado.to_json(orient="records")
#   except Exception as e:
#       print("Oops!", e.__class__, "Ocurrió.")
#       print()


if __name__ == "__main__":
    app.run()
