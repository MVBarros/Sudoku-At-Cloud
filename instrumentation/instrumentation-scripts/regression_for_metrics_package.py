import glob
import json
import numpy as np
import re
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures

numbers = re.compile(r'(\d+)')


paths = [ \
'../package-instrumentation-results/9x9/101/BFS/*.json', '../package-instrumentation-results/9x9/101/CP/*.json', '../package-instrumentation-results/9x9/101/DLX/*.json', \
'../package-instrumentation-results/9x9/102/BFS/*.json', '../package-instrumentation-results/9x9/102/CP/*.json', '../package-instrumentation-results/9x9/102/DLX/*.json',\
'../package-instrumentation-results/16x16/1-BFS/*.json', '../package-instrumentation-results/16x16/1-CP/*.json', '../package-instrumentation-results/16x16/1-DLX/*.json', \
'../package-instrumentation-results/16x16/2-BFS/*.json', '../package-instrumentation-results/16x16/2-CP/*.json', '../package-instrumentation-results/16x16/2-DLX/*.json', \
'../package-instrumentation-results/25x25/1-BFS/*.json', '../package-instrumentation-results/25x25/1-CP/*.json', '../package-instrumentation-results/25x25/1-DLX/*.json', \
'../package-instrumentation-results/25x25/2-BFS/*.json', '../package-instrumentation-results/25x25/2-CP/*.json', '../package-instrumentation-results/25x25/2-DLX/*.json' \
]

strats = ['BFS', 'CP', 'DLX']
metrics = {'BFS' : "Method Count", 'CP' : "New Count", 'DLX' : "Method Count"}

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts




def getMetricsForStrat(strat):
    ret = [[], []]
    metric = metrics[strat]
    curr_paths = [s for s in paths if strat in s]
    for path in curr_paths:
        for filename in sorted(glob.iglob(path), key=numericalSort):
            with open(filename, 'r') as f:
                items = json.load(f)
                ret[0].append([items[metric], items["Board"]["N1"], items["Board"]["N2"], items["Board"]["UN"]])
                ret[1].append(items["Instruction Count"])
    return ret
    

for strat in strats:
    print("\n\nStrategy : " + strat + "\n\n")
    a = getMetricsForStrat(strat)
    x = np.array(a[0])    
    y = np.array(a[1])
    print("Simple linear regression")

    model = LinearRegression()
    model.fit_intercept = True
    model.fit(x, y)
    r_sq = model.score(x, y)
    print('coefficient of determination:', r_sq)
    print('intercept:', model.intercept_)
    print('slope:', model.coef_)
    y_predict = model.predict(x)
    for i in range(0, y.size):
        print("\tPredicted: ", y_predict[i])
        print("\tReal:      ", y[i])

        