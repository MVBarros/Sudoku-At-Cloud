import glob
import json
import numpy as np
import re
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures

numbers = re.compile(r'(\d+)')

#Can only do field store since they are the only ones that have exactly the same number of measures with respect to UN taken

paths_class = [ \
'../solver-class-instrumentation-results/16x16/1-BFS/*.json', '../solver-class-instrumentation-results/16x16/1-CP/*.json',\
'../solver-class-instrumentation-results/16x16/1-DLX/*.json', \
'../solver-class-instrumentation-results/16x16/2-BFS/*.json', '../solver-class-instrumentation-results/16x16/2-CP/*.json',\
'../solver-class-instrumentation-results/16x16/2-DLX/*.json', \
]

paths_package = [\
    '../package-instrumentation-results/16x16/1-BFS/*.json', '../package-instrumentation-results/16x16/1-CP/*.json',\
    '../package-instrumentation-results/16x16/1-DLX/*.json', '../package-instrumentation-results/16x16/2-BFS/*.json',\
    '../package-instrumentation-results/16x16/2-CP/*.json', '../package-instrumentation-results/16x16/2-DLX/*.json', \

]

strats = ['BFS', 'CP', 'DLX']

def numericalSort(value):
    parts = numbers.split(value)
    parts[1::2] = map(int, parts[1::2])
    return parts

metric_strat = {"BFS" : "Method Count", "CP" : "New Count", "DLX" : "Field Store Count"}


def getMetricsForStratClass(strat):
    ret = []
    curr_metric = metric_strat[strat]
    print(curr_metric)
    curr_paths = [s for s in paths_class if strat in s]
    for path in curr_paths:
        for filename in sorted(glob.iglob(path), key=numericalSort):
            with open(filename, 'r') as f:
                items = json.load(f)
                ret.append(items[curr_metric])
    return ret

def getMetricsForStratPackage(strat):
    ret = []
    curr_paths = [s for s in paths_package if strat in s]
    for path in curr_paths:
        for filename in sorted(glob.iglob(path), key=numericalSort):
            with open(filename, 'r') as f:
                items = json.load(f)
                ret.append(items["Instruction Count"])
    return ret

for strat in strats:
    print("\n\nStrategy : " + strat + "\n\n")
    x = np.array(getMetricsForStratClass(strat)).reshape((-1, 1))
    y = getMetricsForStratPackage(strat)
   
    model = LinearRegression()
    model.fit_intercept = False #we're removing intercept to avoid negative costs
    model.fit(x, y)
    r_sq = model.score(x, y)
    print('coefficient of determination:', r_sq)
    print('intercept:', model.intercept_)
    print('slope:', model.coef_)
    y_predict = model.predict(x)
    for i in range(0, len(y)):
        print("\tPredicted: ", y_predict[i])
        print("\tReal:      ", y[i])

#Despite for DLX r_sq being negative, the results are still approximately correct