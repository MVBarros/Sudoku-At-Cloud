import glob
import json
import numpy as np
import re
numbers = re.compile(r'(\d+)')


paths = ['./out/9x9/1-BFS/*.json', './out/9x9/1-CP/*.json', './out/9x9/1-DLX/*.json', './out/9x9/2-BFS/*.json', './out/9x9/2-CP/*.json', './out/9x9/2-DLX/*.json',\
'./out/16x16/1-BFS/*.json', './out/16x16/1-CP/*.json', './out/16x16/1-DLX/*.json', './out/16x16/2-BFS/*.json', './out/16x16/2-CP/*.json', './out/16x16/2-DLX/*.json', \
'./out/25x25/1-BFS/*.json', './out/25x25/1-CP/*.json', './out/25x25/1-DLX/*.json', './out/25x25/2-BFS/*.json', './out/25x25/2-CP/*.json', './out/25x25/2-DLX/*.json']

titles = ['9x9-1-BFS', '9x9-1-CP', '9x9-1-DLX', '9x9-2-BFS', '9x9-2-CP', '9x9-2-DLX', \
'16x16-1-BFS', '16x16-1-CP', '16x16-1-DLX', '16x16-2-BFS', '16x16-2-CP', '16x16-2-DLX', \
'25x25-1-BFS', '25x25-1-CP', '25x25-1-DLX', '25x25-2-BFS', '25x25-2-CP', '25x25-2-DLX']

metric_keys = ['A New Array Count', "Instruction Count", "Store Count", "Field Store Count", "New Count", "Load Count", "New Array Count", "Basic Block Count", "Branch Count", "Method Count", "Multi New Array Count", "Field Load Count", "Stack Depth"]

out_dir = "./graphics/"

strats = ['BFS', 'CP', 'DLX']

boards = ['9x9-1', '9x9-2', '16x16-1', '16x16-2', '25x25-1', '25x25-2']

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts




def getMetricsForStrat(strat, metric):
    ret = list()
    curr_paths = [s for s in paths if strat in s]
    for path in paths:
        for filename in sorted(glob.iglob(path), key=numericalSort):
            with open(filename, 'r') as f:
                items = json.load(f)
                ret.append(items[metric])
    return ret


for strat in strats:
    a = []
    for metric in metric_keys:
        a.append(getMetricsForStrat(strat, metric))
    print("\n\nStrategy :" + strat + "\n\n")
    corr = np.corrcoef(a)
    d = 0
    e = 0
    for m in metric_keys:
        e = 0
        for j in metric_keys[0:d]:
            print("Correlation between [" + metric_keys[d] + "] and [" + metric_keys[e] + "] : {" + str(corr[d,e]) + "}")
            e += 1
        d += 1
