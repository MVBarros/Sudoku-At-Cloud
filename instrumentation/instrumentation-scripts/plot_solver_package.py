import glob
import json
from matplotlib import pyplot as plt
import re
numbers = re.compile(r'(\d+)')


paths = ['../package-instrumentation-results/9x9/101/BFS/*.json', '../package-instrumentation-results/9x9/101/CP/*.json',\
    '../package-instrumentation-results/9x9/101/DLX/*.json', '../package-instrumentation-results/9x9/102/BFS/*.json',\
    '../package-instrumentation-results/9x9/102/CP/*.json', '../package-instrumentation-results/9x9/102/DLX/*.json',\
    '../package-instrumentation-results/16x16/1-BFS/*.json', '../package-instrumentation-results/16x16/1-CP/*.json',\
    '../package-instrumentation-results/16x16/1-DLX/*.json', '../package-instrumentation-results/16x16/2-BFS/*.json',\
    '../package-instrumentation-results/16x16/2-CP/*.json', '../package-instrumentation-results/16x16/2-DLX/*.json', \
    '../package-instrumentation-results/25x25/1-BFS/*.json', '../package-instrumentation-results/25x25/1-CP/*.json',\
    '../package-instrumentation-results/25x25/1-DLX/*.json', '../package-instrumentation-results/25x25/2-BFS/*.json',\
    '../package-instrumentation-results/25x25/2-CP/*.json', '../package-instrumentation-results/25x25/2-DLX/*.json']

titles = ['9x9-1-BFS', '9x9-1-CP', '9x9-1-DLX', '9x9-2-BFS', '9x9-2-CP', '9x9-2-DLX', \
'16x16-1-BFS', '16x16-1-CP', '16x16-1-DLX', '16x16-2-BFS', '16x16-2-CP', '16x16-2-DLX', \
'25x25-1-BFS', '25x25-1-CP', '25x25-1-DLX', '25x25-2-BFS', '25x25-2-CP', '25x25-2-DLX']

metric_keys = ['A New Array Count', "Instruction Count", "Store Count", "Field Store Count", "New Count", "Load Count", "New Array Count", "Basic Block Count", "Branch Count", "Method Count", "Multi New Array Count", "Field Load Count", "Stack Depth"]

out_dir = "../package-instrumentation-graphics/"

strats = ['BFS', 'CP', 'DLX']

boards = ['9x9-1', '9x9-2', '16x16-1', '16x16-2', '25x25-1', '25x25-2']

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts




def cleanup(metric):
    d = {metric : [], "Unassigned Entries" : []}
    return d


def grafico():
    for metric in metric_keys:
        for strat in strats:
            curr_paths = [s for s in paths if strat in s]
            for path, board in zip(curr_paths, boards):
                d = cleanup(metric)    
                filenames = sorted(glob.iglob(path), key=numericalSort) 
                for filename in filenames:
                    with open(filename, 'r') as f:
                        items = json.load(f)
                        d[metric].append(items[metric])
                        d['Unassigned Entries'].append(items['Board']['UN'])
                                    
                plt.plot(d['Unassigned Entries'], d[metric], label=board + "-" + metric)

            plt.xlabel("Unassigned Entries")
            plt.title(strat + "-" + metric)
            plt.legend()
            plt.savefig(out_dir + strat + "-" + metric + ".png")
            plt.clf()
                

grafico()
