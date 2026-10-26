package com.drawfirework.canvas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Occupancy-adaptive spatial subdivision; representatives always lie on a visible source pixel. */
public final class CanvasDownsampler {
    private CanvasDownsampler() {}
    /** Pixel centres on a common longest-axis scale, +Y up. Weight is represented visible pixel count. */
    public record Sample(double x, double y, int argb, int weight) {
        public double alpha() { return (argb >>> 24) / 255.0; }
    }
    private record Cell(int x0, int y0, int x1, int y1, int count, int representative) {}
    private static final Comparator<Cell> ORDER = Comparator.comparingInt(Cell::count).reversed()
            .thenComparingInt(Cell::y0).thenComparingInt(Cell::x0);

    public static List<Sample> sample(CanvasData canvas, int budget, SamplingStrategy strategy) {
        if (strategy == SamplingStrategy.DETAIL_PRESERVING) {
            return DetailPreservingSampler.sample(canvas, budget);
        }
        return sample(canvas, budget);
    }

    public static List<Sample> sample(CanvasData canvas, int budget) {
        if (budget < 0 || budget > ParticleBudget.MAX_CORE) throw new IllegalArgumentException("Sample budget must be 0..800");
        if (budget == 0) return List.of();
        Cell root = cell(canvas, 0, 0, canvas.width(), canvas.height());
        if (root == null) return List.of();
        var pending = new PriorityQueue<>(ORDER);
        var leaves = new ArrayList<Cell>();
        pending.add(root);
        while (pending.size() + leaves.size() < budget && !pending.isEmpty()) {
            Cell c = pending.remove();
            if (c.count == 1) { leaves.add(c); continue; }
            // Bounds are tight around occupancy, so splitting the longest axis creates two nonempty cells.
            if (c.x1 - c.x0 >= c.y1 - c.y0) {
                int mid = (c.x0 + c.x1) / 2;
                pending.add(cell(canvas, c.x0, c.y0, mid, c.y1));
                pending.add(cell(canvas, mid, c.y0, c.x1, c.y1));
            } else {
                int mid = (c.y0 + c.y1) / 2;
                pending.add(cell(canvas, c.x0, c.y0, c.x1, mid));
                pending.add(cell(canvas, c.x0, mid, c.x1, c.y1));
            }
        }
        leaves.addAll(pending);
        leaves.sort(Comparator.comparingInt(Cell::representative));
        double scale = Math.max(canvas.width(), canvas.height());
        var result = new ArrayList<Sample>(leaves.size());
        for (Cell c : leaves) {
            int x = c.representative % canvas.width(), y = c.representative / canvas.width();
            result.add(new Sample((x + .5 - canvas.width() / 2.0) / scale,
                    (canvas.height() / 2.0 - y - .5) / scale, canvas.pixel(x, y), c.count));
        }
        return List.copyOf(result);
    }

    private static Cell cell(CanvasData c, int x0, int y0, int x1, int y1) {
        int minX = x1, minY = y1, maxX = -1, maxY = -1, count = 0;
        long mass = 0, sx = 0, sy = 0;
        for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) {
            int a = c.pixel(x, y) >>> 24;
            if (a == 0) continue;
            count++; mass += a; sx += (long) x * a; sy += (long) y * a;
            minX = Math.min(minX, x); minY = Math.min(minY, y);
            maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
        }
        if (count == 0) return null;
        double cx = sx / (double) mass, cy = sy / (double) mass, best = Double.POSITIVE_INFINITY;
        int representative = -1;
        for (int y = minY; y <= maxY; y++) for (int x = minX; x <= maxX; x++) {
            if ((c.pixel(x, y) >>> 24) == 0) continue;
            double distance = (x - cx) * (x - cx) + (y - cy) * (y - cy);
            int index = y * c.width() + x;
            if (distance < best || (distance == best && Integer.compareUnsigned(tieRank(index), tieRank(representative)) < 0)) {
                best = distance; representative = index;
            }
        }
        return new Cell(minX, minY, maxX + 1, maxY + 1, count, representative);
    }

    // Fixed integer mixing avoids always selecting the upper-left colour on equally distant checker pixels.
    private static int tieRank(int index) {
        int z = index + 0x9e3779b9;
        z = (z ^ (z >>> 16)) * 0x85ebca6b;
        z = (z ^ (z >>> 13)) * 0xc2b2ae35;
        return z ^ (z >>> 16);
    }
}
