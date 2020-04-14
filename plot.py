import glob
import json
from matplotlib import pyplot as plt
import re
numbers = re.compile(r'(\d+)')


paths = ['./out/9x9/101/BFS/*.json', './out/9x9/101/CP/*.json', './out/9x9/101/DLX/*.json', './out/9x9/102/BFS/*.json', './out/9x9/102/CP/*.json', './out/9x9/102/DLX/*.json',\
'./out/16x16/1-BFS/*.json', './out/16x16/1-CP/*.json', './out/16x16/1-DLX/*.json', './out/16x16/2-BFS/*.json', './out/16x16/2-CP/*.json', './out/16x16/2-DLX/*.json', \
'./out/25x25/1-BFS/*.json', './out/25x25/1-CP/*.json', './out/25x25/1-DLX/*.json', './out/25x25/2-BFS/*.json', './out/25x25/2-CP/*.json', './out/25x25/2-DLX/*.json']
titles = ['9x9-1-BFS', '9x9-1-CP', '9x9-1-DLX', '9x9-2-BFS', '9x9-2-CP', '9x9-2-DLX', \
'16x16-1-BFS', '16x16-1-CP', '16x16-1-DLX', '16x16-2-BFS', '16x16-2-CP', '16x16-2-DLX', \
'25x25-1-BFS', '25x25-1-CP', '25x25-1-DLX', '25x25-2-BFS', '25x25-2-CP', '25x25-2-DLX']
metric_keys = ['A New Array Count', "Basic Instruction Count", "Store Count", "Field Store Count", "New Count", "Load Count", "New Array Count", "Basic Block Count", "Branch Count", "Method Count", "Multi New Array Count", "Field Load Count", "Stack Depth"]
out_dir = "./graphics/"

strats = ['BFS', 'CP', 'DLX']
e = ['9x9-1', '9x9-2', '16x16-1', '16x16-2', '25x25-1', '25x25-2']

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts

def cleanup():
    d = {'A New Array Count' : [], "Basic Instruction Count": [], "Store Count": [], "Field Store Count": [], "New Count": [], "Load Count": [], "New Array Count": [], "Basic Block Count": [], "Branch Count": [], "Method Count": [], "Multi New Array Count": [], "Field Load Count": [], "Stack Depth": [], "Zeros" : []}
    return d


def cleanup2():
    d = {'A New Array Count' : [], "Basic Instruction Count": [], "Store Count": [], "Field Store Count": [], "New Count": [], "Load Count": [], "New Array Count": [], "Basic Block Count": [], "Branch Count": [], "Method Count": [], "Multi New Array Count": [], "Field Load Count": [], "Stack Depth": [], "Zeros" : []}
    #e = {'9x9-1': d, '9x9-2': d, '16x16-1': d, '16x16-2': d, '25x25-1': d, '25x25-2': d}
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

        for k in metric_keys:
            plt.plot(d['Zeros'], d[k], label=k)
            plt.xlabel('Numero de zeros')
            plt.title(title + "-" + k)
            plt.legend()
            plt.savefig(out_dir + title + "-" + k + ".png")
            plt.clf()
        i += 1

def grafico2():
    
        for k in metric_keys:
            for strat in strats:
                kindx = 0
                paths_strat = matching = [s for s in paths if strat in s]
                for path in paths_strat:
                    plot_key = e[kindx]
                
                    d = cleanup()    
                    for filename in sorted(glob.iglob(path), key=numericalSort):
                        with open(filename, 'r') as f:
                            f_contents = f.read()
                            data = json.loads(f_contents)
                            for (key, value) in data.items():
                                if key != 'Board':
                                    d[key].append(value)
                                else:
                                    d['Zeros'].append(data['Board']['Board Zeros'])

                                        
                    plt.plot(d['Zeros'], d[k], label=plot_key + "-" + k)
                    kindx +=1

                plt.xlabel('Numero de zeros')
                plt.title(strat + "-" + k)
                plt.legend()
                plt.savefig(out_dir + strat + "-" + k + ".png")
                plt.clf()
                
grafico()
grafico2()
