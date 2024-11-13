import json
import matplotlib.pyplot as plt

with open('./tmp/results.json', 'r') as file:
    test_results_json = file.read()

plt.figure(figsize=(12, 8))

results = json.loads(test_results_json)
count_parallels = len(results["countThreadsArr"])

x_labels = []
y_serial = []
y_parallels = [[] for i in range(count_parallels)]

for result in results['graphResults']:
    x_labels.append(f"{result['countNodes']}, {result['countEdges']}")
    y_serial.append(result['serialTimeMillis'])


    for i in range(count_parallels):
        parallel_results = result['parallelResults']
        y_parallels[i].append(parallel_results[i]['parallelTimeMillis'])

# Построение графика
plt.plot(x_labels, y_serial, label='Serial')
for i in range(count_parallels):
    s = f'{results["countThreadsArr"][i]} Threads'
    plt.plot(x_labels, y_parallels[i], label=s)

plt.xlabel('Nodes, Edges')
plt.ylabel('Time (ms)')
plt.title('Serial vs Parallel DFS')
plt.legend()
plt.xticks(rotation=45)
plt.tight_layout()

plt.savefig('tmp/results.png', format='png', dpi=300)
plt.show()