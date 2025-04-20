package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class LogAnalyzer {

    static class IndexInfo {
        final String index;
        final long sizeBytes;
        final int shards;
        final double sizeGb;
        final double ratio;
        final int recommendedShards;

        IndexInfo(String index, long sizeBytes, int shards) {
            this.index = index;
            this.sizeBytes = sizeBytes;
            this.shards = shards;
            this.sizeGb = roundGb(sizeBytes);
            this.ratio = (shards == 0) ? Double.POSITIVE_INFINITY : this.sizeGb / shards;
            this.recommendedShards = Math.max(1, (int) (this.sizeGb / 30));
        }

        private static double roundGb(long bytes) {
            double gb = bytes / 1_000_000_000.0;
            return Math.round(gb * 100.0) / 100.0;
        }
    }

    public static void main(String[] args) throws Exception {
        String endpoint = "https://your-es-endpoint.com";
        boolean debug = true;
        int days = 1;

        List<IndexInfo> indices;
        if (debug) {
            indices = getDataFromFile("/Users/anuraagramineni/Downloads/interview/example-in.json");
        } else {
            indices = getDataFromServer(endpoint, days);
        }

        StringBuilder output = new StringBuilder();

        appendLargestBySize(indices, output);
        appendLargestByShards(indices, output);
        appendLeastBalanced(indices, output);

        // Write to file
        try (FileWriter writer = new FileWriter("analysis-output.txt")) {
            writer.write(output.toString());
        }

        System.out.println("Analysis written to analysis-output.txt");
    }

    public static List<IndexInfo> getDataFromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> raw = mapper.readValue(
                new File(path),
                new TypeReference<List<Map<String, String>>>() {}
        );
        return toIndexInfo(raw);
    }

    public static List<IndexInfo> getDataFromServer(String endpoint, int daysAgo) throws IOException {
        LocalDate date = LocalDate.now().minusDays(daysAgo);
        String urlStr = String.format(
                "%s/_cat/indices/*%d*%02d*%02d?v&h=index,pri.store.size,pri&format=json&bytes=b",
                endpoint,
                date.getYear(), date.getMonthValue(), date.getDayOfMonth()
        );

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestProperty("Accept", "application/json");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> raw = mapper.readValue(
                sb.toString(),
                new TypeReference<List<Map<String, String>>>() {}
        );
        return toIndexInfo(raw);
    }

    private static List<IndexInfo> toIndexInfo(List<Map<String, String>> raw) {
        List<IndexInfo> list = new ArrayList<>();
        for (Map<String, String> m : raw) {
            try {
                String idx = m.get("index");
                long sz = Long.parseLong(m.get("pri.store.size"));
                int sh = Integer.parseInt(m.get("pri"));
                list.add(new IndexInfo(idx, sz, sh));
            } catch (Exception ignored) {
                System.err.println("Skipping malformed entry: " + m);
            }
        }
        return list;
    }

    public static void appendLargestBySize(List<IndexInfo> data, StringBuilder out) {
        out.append("Printing largest indexes by storage size\n");
        data.stream()
                .sorted(Comparator.comparingDouble(i -> -i.sizeGb))
                .limit(5)
                .forEach(i -> {
                    out.append(String.format("Index: %s%n", i.index));
                    out.append(String.format("Size: %.2f GB%n", i.sizeGb));
                });
        out.append("\n");
    }

    public static void appendLargestByShards(List<IndexInfo> data, StringBuilder out) {
        out.append("Printing largest indexes by shard count\n");
        data.stream()
                .sorted(Comparator.comparingInt(i -> -i.shards))
                .limit(5)
                .forEach(i -> {
                    out.append(String.format("Index: %s%n", i.index));
                    out.append(String.format("Shards: %d%n", i.shards));
                });
        out.append("\n");
    }

    public static void appendLeastBalanced(List<IndexInfo> data, StringBuilder out) {
        out.append("Printing least balanced indexes\n");
        data.stream()
                .sorted((a, b) -> Double.compare(b.ratio, a.ratio))
                .limit(5)
                .forEach(i -> {
                    int ratio = (i.shards <= 2)
                            ? (int) Math.floor(i.ratio)
                            : (int) Math.round(i.ratio);

                    out.append(String.format("Index: %s%n", i.index));
                    out.append(String.format("Size: %.2f GB%n", i.sizeGb));
                    out.append(String.format("Shards: %d%n", i.shards));
                    out.append(String.format("Balance Ratio: %d%n", ratio));
                    out.append(String.format("Recommended shard count is %d%n", i.recommendedShards));
                });
    }
}
