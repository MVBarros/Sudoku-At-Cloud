import glob
import json
from matplotlib import pyplot as plt
import re
numbers = re.compile(r'(\d+)')

#sudo apt-get install python-tk
#pip install matplotlib
#python plot.py

paths = ['./out/9x9/101/BFS/*.json', './out/9x9/101/CP/*.json', './out/9x9/101/DLX/*.json', './out/9x9/102/BFS/*.json', './out/9x9/102/CP/*.json', './out/9x9/102/DLX/*.json']
titles = ['9x9-101-BFS', '9x9-101-CP', '9x9-101-DLX', '9x9-102-BFS', '9x9-102-CP', '9x9-102-DLX']

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts

def cleanup():
    d = {'A New Array Count' : [], "Basic Instruction Count": [], "Store Count": [], "Field Store Count": [], "New Count": [], "Load Count": [], "New Array Count": [], "Basic Block Count": [], "Branch Count": [], "Method Count": [], "Multi New Array Count": [], "Field Load Count": [], "Stack Depth": [], "Zeros" : []}
    return d


def grafico():
    i = 0
    for path in paths:
        d = cleanup()
        title = titles[i] 
        for filename in sorted(glob.iglob(path), key=numericalSort):
            with open(filename, 'r') as f:
                f_contents = f.read()
                data = json.loads(f_contents)
                for (key, value) in data.items():
                    if key != 'Board':
                        d[key].append(value)
                    else:
                        d['Zeros'].append(data['Board']['Board Zeros'])

        for (key, value) in d.items():
            if key != 'Zeros':
                plt.plot(d['Zeros'], d[key], label=key)

        plt.xlabel('Numero de zeros')
        plt.title(title)
        plt.legend()
        plt.show()
        i += 1
        

grafico()
