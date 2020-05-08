import glob
import json
import numpy as np
import re
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures

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

strats = ['BFS', 'CP', 'DLX']
metrics = {'BFS' : "Method Count", 'CP' : "New Count", 'DLX' : "New Array Count"}

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
                ret.append(items[metric])
                if (strat == "DLX"):
                    ret[0].append(items[metric])
                else:
                    ret[0].append(items[metric])
                ret[1].append(items["Instruction Count"])
    return ret


for strat in strats:
    print("\n\nStrategy : " + strat + "\n\n")
    a = getMetricsForStrat(strat)
    print(a)
    x = np.array(a[0]).reshape((-1, 1))    
    y = np.array(a[1])
    print("Simple linear regression")

    model = LinearRegression()
    model.fit(x, y)
    r_sq = model.score(x, y)
    print('coefficient of determination:', r_sq)
    print('intercept:', model.intercept_)
    print('slope:', model.coef_)
    print("\n")

    for i in range(2, 10):
        print(f"Polinomial linear regression degree {i}.")
        transformer = PolynomialFeatures(degree=i, include_bias=False)
        transformer.fit(x)
        x_ = transformer.transform(x)
        model = LinearRegression().fit(x_, y)
        r_sq = model.score(x_, y)
        print('coefficient of determination:', r_sq)
        print('intercept:', model.intercept_)
        print('slope:', model.coef_)
        print("\n")
        