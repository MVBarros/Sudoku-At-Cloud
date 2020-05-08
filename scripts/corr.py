import glob
import json
import numpy as np
import re
numbers = re.compile(r'(\d+)')


paths = [ \
'../docs/out/9x9/1-BFS/*.json', '../docs/out/9x9/1-CP/*.json', '../docs/out/9x9/1-DLX/*.json', \
'../docs/out/9x9/2-BFS/*.json', '../docs/out/9x9/2-CP/*.json', '../docs/out/9x9/2-DLX/*.json',\
'../docs/out/9x9/3-BFS/*.json', '../docs/out/9x9/3-CP/*.json', '../docs/out/9x9/3-DLX/*.json', \
'../docs/out/16x16/1-BFS/*.json', '../docs/out/16x16/1-CP/*.json', '../docs/out/16x16/1-DLX/*.json', \
'../docs/out/16x16/2-BFS/*.json', '../docs/out/16x16/2-CP/*.json', '../docs/out/16x16/2-DLX/*.json', \
'../docs/out/16x16/3-BFS/*.json', '../docs/out/16x16/3-CP/*.json', '../docs/out/16x16/3-DLX/*.json', \
'../docs/out/25x25/1-BFS/*.json', '../docs/out/25x25/1-CP/*.json', '../docs/out/25x25/1-DLX/*.json', \
'../docs/out/25x25/2-BFS/*.json', '../docs/out/25x25/2-CP/*.json', '../docs/out/25x25/2-DLX/*.json' \
]

metric_keys_bfs = ["Instruction Count", "Store Count", "Load Count", "Basic Block Count", "Branch Count", "Method Count", "Field Load Count", "Stack Depth"]
metric_keys_cp = ["Instruction Count", "Store Count", "New Count", "Load Count", "Basic Block Count", "Branch Count", "Method Count", "Field Load Count", "Stack Depth"]
metric_keys_dlx = ["Instruction Count", "Store Count", "Field Store Count", "New Count", "Load Count", "New Array Count", "Basic Block Count", "Branch Count", "Method Count", "Field Load Count", "Stack Depth"]

strats = ['BFS', 'CP', 'DLX']

metric_keys = {'BFS' : metric_keys_bfs, 'CP' : metric_keys_cp, 'DLX' : metric_keys_dlx}


def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts




def getMetricsForStrat(strat, metric):
    ret = list()
    curr_paths = [s for s in paths if strat in s]
    for path in curr_paths:
        for filename in sorted(glob.iglob(path), key=numericalSort):
            with open(filename, 'r') as f:
                items = json.load(f)
                ret.append(items[metric])
    return ret


for strat in strats:
    print("\n\nStrategy : " + strat + "\n\n")

    a = []
    for metric in metric_keys[strat]:
        a.append(getMetricsForStrat(strat, metric))
    corr = np.corrcoef(a)
    baseline = metric_keys[strat][0]
    e = 0
    for metric in metric_keys[strat]:
        print("Correlation between [ " + baseline + " ] and [ " + metric + " ] : { " + str(corr[0,e]) + " }")
        e += 1
