import numpy as np
import matplotlib.pyplot as plt
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import r2_score
from sklearn.metrics import mean_absolute_error
import pandas as pd


# Método que separa el dataset de entrenamiento y de test
def get_data_splits(dataframe, valid_fraction=0.2):
    valid_size = int(len(dataframe) * valid_fraction)
    if valid_size < 1:
        valid_size = 1
    train = dataframe[:-valid_size]
    valid = dataframe[-valid_size:]
    return train, valid


def neural_network(nodes, input_length):
    '''
        Modelo de la red neuronal
    '''
    model = Sequential()
    model.add(Dense(nodes, input_dim=input_length, kernel_initializer='normal', activation='relu'))
    model.add(Dense(nodes+8, kernel_initializer='normal', activation='relu'))
    model.add(Dense(1, kernel_initializer='normal', activation='relu'))

    model.compile(loss='mean_absolute_error', optimizer='adam', metrics=["mae"])

    return model


# Crea el conjunto de validación y entrenamiento
def create_train_valid_set(nn_data):



    nn_data = nn_data.loc[:, nn_data.columns != "week"]
    nn_data = nn_data.loc[:, nn_data.columns != "product_cost"]
    nn_data = nn_data.loc[:, nn_data.columns != "product_max_bound"]

    train = pd.DataFrame(columns=nn_data.columns)

    valid = pd.DataFrame(columns=nn_data.columns)

    for product in nn_data.prod_id.unique():
        dataframe = nn_data.loc[nn_data.prod_id == product]
        std = dataframe.iloc[:, -1].std()
        mean = dataframe.iloc[:, -1].mean()
        if std <= mean:
            temp_train, temp_valid = get_data_splits(dataframe)
            train = train.append(temp_train, ignore_index=True)
            valid = valid.append(temp_valid, ignore_index=True)

    X_train = train.iloc[:, 0:-1]

    y_train = train.iloc[:, -1]

    X_valid = valid.iloc[:, 0:-1]
    y_valid = valid.iloc[:, -1]

    product_encoder = LabelEncoder()
    product_encoder.fit(X_train["prod_id"])
    X_train["prod_id"] = product_encoder.transform(X_train["prod_id"])
    X_valid["prod_id"] = product_encoder.transform(X_valid["prod_id"])

    return X_train, X_valid, y_train, y_valid


def nn_testing(nn_data):

    X_train, X_valid, y_train, y_valid = create_train_valid_set(nn_data)

    model = neural_network(40, X_train.shape[1])
    history = model.fit(X_train, y_train,
                        epochs=50, batch_size=16,
                        validation_data=[X_valid, y_valid],
                        verbose=0)

    history_dict = history.history

    loss_values = history_dict['loss']
    val_loss_values = history_dict['val_loss']

    plt.plot(loss_values, label='Pérdida Entrenamiento')
    plt.plot(val_loss_values, label='Pérdida Validación')
    plt.legend()
    plt.show()

    y_train_pred = np.round_(model.predict(X_train))
    y_valid_pred = np.round_(model.predict(X_valid))
    train_mae = mean_absolute_error(y_train, y_train_pred)
    valid_mae = mean_absolute_error(y_valid, y_valid_pred)
    
    # Calculamos el R2
    print("R2 para el conjunto de entrenamiento: ", r2_score(y_train, y_train_pred))
    print("R2 para el conjunto de validación: ", r2_score(y_valid, y_valid_pred))
    # Calculamos el MAE
    print("MAE para el conjunto de entrenamiento:\t{:0.3f}".format(train_mae))
    print("MAE para el conjunto de validación:\t{:0.3f}".format(valid_mae))


# Entrenamiento final de la red neuronal
def nn_final_training(nn_data):
    # Ignoramos las columnas: week, product_cost y prodct_max_bound
    nn_data = nn_data.loc[:, nn_data.columns != "week"]
    nn_data = nn_data.loc[:, nn_data.columns != "product_cost"]
    nn_data = nn_data.loc[:, nn_data.columns != "product_max_bound"]

    # Seleccionamos todas los datos para X_train
    X_train = nn_data.iloc[:, 0:-1]
    # Seleccionamos la columna Q17, ya que está será nuestra variable a predecir
    y_train = nn_data.iloc[:, -1]

    # Preparamos el modelo
    product_encoder = LabelEncoder()
    X_train["prod_id"] = product_encoder.fit_transform(X_train["prod_id"])

    # Configuramos los parámetros del modelo
    model = neural_network(40, X_train.shape[1])
    model.fit(X_train, y_train,
              epochs=50, batch_size=16,
              verbose=0)

    y_train_pred = np.round_(model.predict(X_train))

    print("R2 para el conjunto de entrenamiento: ", r2_score(y_train, y_train_pred))

    # Guardamos el modelo en un archivo
    model.save("final_model.h5")
 
    return product_encoder

