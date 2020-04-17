import glob
import json
from matplotlib import pyplot as plt
import re
numbers = re.compile(r'(\d+)')

paths = [ \
'./out/9x9/1-BFS/*.json', './out/9x9/1-CP/*.json', './out/9x9/1-DLX/*.json', \
'./out/9x9/2-BFS/*.json', './out/9x9/2-CP/*.json', './out/9x9/2-DLX/*.json',\
'./out/9x9/3-BFS/*.json', './out/9x9/3-CP/*.json', './out/9x9/3-DLX/*.json', \
'./out/16x16/1-BFS/*.json', './out/16x16/1-CP/*.json', './out/16x16/1-DLX/*.json', \
'./out/16x16/2-BFS/*.json', './out/16x16/2-CP/*.json', './out/16x16/2-DLX/*.json', \
'./out/16x16/3-BFS/*.json', './out/16x16/3-CP/*.json', './out/16x16/3-DLX/*.json', \
'./out/25x25/1-BFS/*.json', './out/25x25/1-CP/*.json', './out/25x25/1-DLX/*.json', \
'./out/25x25/2-BFS/*.json', './out/25x25/2-CP/*.json', './out/25x25/2-DLX/*.json' \
]

metric_keys = ['A New Array Count', "Instruction Count", "Store Count", \
 "Field Store Count", "New Count", "Load Count", "New Array Count", "Basic Block Count", \
 "Branch Count", "Method Count", "Multi New Array Count", "Field Load Count", "Stack Depth"]

out_dir = "./graphics/"

strats = ['BFS', 'CP', 'DLX']

boards = ['9x9-1', '9x9-2', '9x9-3', '16x16-1', '16x16-2', '16x16-3', '25x25-1', '25x25-2']

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts


def cleanup2(metric):
    d = {metric : [], "Zeros" : []}
    return d


def grafico2():
    for metric in metric_keys:
        for strat in strats:
            curr_paths = [s for s in paths if strat in s]
            for path, board in zip(curr_paths, boards):
                d = cleanup2(metric)
                filenames = sorted(glob.iglob(path), key=numericalSort)
                for filename in filenames:
                    with open(filename, 'r') as f:
                        items = json.load(f)
                        d[metric].append(items[metric])
                        d['Zeros'].append(items['Board']['Board Zeros'])

                plt.plot(d['Zeros'], d[metric], label=board + "-" + metric)

            plt.xlabel('Numero de zeros')
            plt.title(strat + "-" + metric)
            plt.legend()
            plt.savefig(out_dir + strat + "-" + metric + ".png")
            plt.clf()

#grafico()
grafico2()
