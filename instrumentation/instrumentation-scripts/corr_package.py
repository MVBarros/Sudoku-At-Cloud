import glob
import json
import numpy as np
import re

numbers = re.compile(r'(\d+)')


paths = [ \
'../package-instrumentation-results/9x9/101/BFS/*.json', '../package-instrumentation-results/9x9/101/CP/*.json', '../package-instrumentation-results/9x9/101/DLX/*.json', \
'../package-instrumentation-results/9x9/102/BFS/*.json', '../package-instrumentation-results/9x9/102/CP/*.json', '../package-instrumentation-results/9x9/102/DLX/*.json',\
'../package-instrumentation-results/16x16/1-BFS/*.json', '../package-instrumentation-results/16x16/1-CP/*.json', '../package-instrumentation-results/16x16/1-DLX/*.json', \
'../package-instrumentation-results/16x16/2-BFS/*.json', '../package-instrumentation-results/16x16/2-CP/*.json', '../package-instrumentation-results/16x16/2-DLX/*.json', \
'../package-instrumentation-results/25x25/1-BFS/*.json', '../package-instrumentation-results/25x25/1-CP/*.json', '../package-instrumentation-results/25x25/1-DLX/*.json', \
'../package-instrumentation-results/25x25/2-BFS/*.json', '../package-instrumentation-results/25x25/2-CP/*.json', '../package-instrumentation-results/25x25/2-DLX/*.json' \
]

metric_keys = [\
    "A New Array Count",\
    "Basic Block Count",\
    "Branch Count",\
    "Field Load Count",\
    "Field Store Count",\
    "Instruction Count",\
    "Load Count",\
    "Method Count",\
    "Multi New Array Count",\
    "New Array Count",\
    "New Count",\
    "Stack Depth"\
]

strats = ['BFS', 'CP', 'DLX']



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
    for metric in metric_keys:
        a.append(getMetricsForStrat(strat, metric))
    corr = np.corrcoef(a)
    baseline = metric_keys[5]
    e = 0
    for metric in metric_keys:
        print("Correlation between [ " + baseline + " ] and [ " + metric + " ] : { " + str(corr[5][e]) + " }")
        e += 1