import glob
import json
from Tkinter import *
from matplotlib import pyplot as plt
import re
numbers = re.compile(r'(\d+)')

#sudo apt-get install python-tk
#pip install matplotlib
#python plot.py

path = '/home/goncalo/Documents/Mestrado/CNV/Sudoku-At-Cloud/out/9x9/101/BFS/*.json'
title = '9x9 - BFS'

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts

def grafico():
    d = {'A New Array Count' : [], "Basic Instruction Count": [], "Store Count": [], "Field Store Count": [], "New Count": [], "Load Count": [], "New Array Count": [], "Basic Block Count": [], "Branch Count": [], "Method Count": [], "Multi New Array Count": [], "Field Load Count": [], "Stack Depth": [], "Zeros" : []}

    for filename in sorted(glob.iglob(path), key=numericalSort):
        f = open(filename, 'r')
        f_contents = f.read()
        data = json.loads(f_contents)
        for (key, value) in data.items():
            if key != 'Board':
                d[key].append(value)
            else:
                d['Zeros'].append(data['Board']['Board Zeros'])

    # Ha metricas com intervalos de valores bastante diferentes e por isso alguns vao aparecer sobrepostos
    # Pode-se especificar aqui as metricas que queremos agrupar e fazer varios graficos
    # Neste caso como tou a agrupar por 9x9 e por estrategia, fixei o numero de zeros no eixo dos x
    for (key, value) in d.items():
        if key != 'Zeros':
            plt.plot(d['Zeros'], d[key], label=key)

    plt.xlabel('Numero de zeros')
    plt.title(title)
    plt.legend()
    plt.show()


grafico()
