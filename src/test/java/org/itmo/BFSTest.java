package org.itmo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BFSTest {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void bfsTest() throws IOException {
        System.out.println(Runtime.getRuntime().availableProcessors());
        GraphSettings[] graphSettings = new GraphSettings[]{
                new GraphSettings(100, 500),
                new GraphSettings(1000, 5000),
                new GraphSettings(10_000, 50_000),
                new GraphSettings(100_000, 500_000),
                new GraphSettings(100_000, 1_000_000),
                new GraphSettings(150_000, 10_000_000)
        };
        int[] countThreadsArr = new int[]{2, 4, 6, 8, 12};

        ArrayList<GraphResult> graphResults = new ArrayList<>();

        Random r = ThreadLocalRandom.current();
        try (FileWriter fw = new FileWriter("tmp/results.txt")) {
            for (int j = 0; j < graphSettings.length; j++) {
                int countNodes = graphSettings[j].countNodes;
                int countEdges = graphSettings[j].countEdges;
                Graph g = generateGraph(r, countNodes, countEdges);

                fw.append("Times for ").append(String.valueOf(countNodes)).append(" vertices and ").append(String.valueOf(countEdges)).append(" connections: ")
                        .flush();

                long serialTime = executeSerialBfsAndGetTime(g);
                fw.append("\nSerial: ").append(String.valueOf(serialTime))
                        .flush();

                ArrayList<ParallelResult> parallelResults = new ArrayList<>();
                for (int countThreads : countThreadsArr) {
                    long parallelTime = executeParallelBfsAndGetTime(g, countThreads);
                    parallelResults.add(new ParallelResult(parallelTime, countThreads));
                    fw.append(String.format("\nParallel(%d): ", countThreads)).append(String.valueOf(parallelTime))
                            .flush();
                }

                graphResults.add(new GraphResult(countNodes, countEdges, serialTime, parallelResults));
                fw.append("\n--------\n")
                        .flush();
            }
        }

        try (FileWriter fw = new FileWriter("tmp/results.json")) {
            GSON.toJson(new Results(countThreadsArr, graphResults), fw);
        }
    }

    private Graph generateGraph(Random r, int size, int numOfConnections) {
        Graph graph = new Graph(size);
        for (int i = 0; i < numOfConnections; i++) {
            graph.addEdge(r.nextInt(size), r.nextInt(size));
        }
        return graph;
    }

    private long executeSerialBfsAndGetTime(Graph g) {
        long startTime = System.currentTimeMillis();
        g.dfs(0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long executeParallelBfsAndGetTime(Graph g, int countThreads) {
        long startTime = System.currentTimeMillis();
        g.parallelDFS(0, countThreads);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static class GraphSettings {
        private final int countNodes;
        private final int countEdges;

        private GraphSettings(int countNodes, int countEdges) {
            this.countNodes = countNodes;
            this.countEdges = countEdges;
        }
    }

    private static class Results {
        private final int[] countThreadsArr;
        private final List<GraphResult> graphResults;

        private Results(int[] countThreadsArr, List<GraphResult> graphResults) {
            this.countThreadsArr = countThreadsArr;
            this.graphResults = graphResults;
        }
    }

    private static class GraphResult {
        private final long countNodes;
        private final long countEdges;
        private final long serialTimeMillis;
        private final List<ParallelResult> parallelResults;

        private GraphResult(long countNodes, long countEdges, long serialTimeMillis, List<ParallelResult> parallelResults) {
            this.countNodes = countNodes;
            this.countEdges = countEdges;
            this.serialTimeMillis = serialTimeMillis;
            this.parallelResults = parallelResults;
        }
    }

    private static class ParallelResult {
        private final long parallelTimeMillis;
        private final int countThreads;

        private ParallelResult(long parallelTimeMillis, int countThreads) {
            this.parallelTimeMillis = parallelTimeMillis;
            this.countThreads = countThreads;
        }
    }
}
