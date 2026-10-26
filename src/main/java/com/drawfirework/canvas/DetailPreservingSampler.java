package com.drawfirework.canvas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Detail-preserving spatial sampling (Strategy B).
 * Prioritizes contours, thin lines, endpoints, and color boundaries while smoothly
 * filling interior solid regions within a bounded sample budget.
 * Pure Java, 100% deterministic, zero client dependencies.
 */
public final class DetailPreservingSampler {
    private DetailPreservingSampler() {}

    public static final int MAX_SAMPLE_BUDGET = ParticleBudget.MAX_CORE;

    public record Pixel(int x, int y, int argb, int importance) {}

    private record Cell(int x0, int y0, int x1, int y1, List<Pixel> pixels, Pixel representative) {}

    private static final Comparator<Cell> CELL_ORDER = Comparator.comparingInt((Cell c) -> c.pixels().size()).reversed()
            .thenComparingInt(Cell::y0).thenComparingInt(Cell::x0);

    public static List<CanvasDownsampler.Sample> sample(CanvasData canvas, int budget) {
        Objects.requireNonNull(canvas, "canvas");
        if (budget < 0 || budget > MAX_SAMPLE_BUDGET) {
            throw new IllegalArgumentException("Sample budget must be 0.." + MAX_SAMPLE_BUDGET);
        }
        if (budget == 0) return List.of();

        int w = canvas.width();
        int h = canvas.height();

        // 1. Collect and classify all visible pixels
        List<Pixel> visible = new ArrayList<>();
        List<Pixel> detail = new ArrayList<>();
        List<Pixel> interior = new ArrayList<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = canvas.pixel(x, y);
                int a = argb >>> 24;
                if (a == 0) continue;

                int importance = calculateImportance(canvas, x, y, argb, w, h);
                Pixel p = new Pixel(x, y, argb, importance);
                visible.add(p);
                if (importance >= 40) {
                    detail.add(p);
                } else {
                    interior.add(p);
                }
            }
        }

        if (visible.isEmpty()) return List.of();

        // If total visible pixels <= budget, return all of them directly
        if (visible.size() <= budget) {
            return convertToSamples(visible, w, h);
        }

        // 2. Budget partition between detail and interior
        List<Pixel> selected = new ArrayList<>(budget);
        if (interior.isEmpty()) {
            selected.addAll(subdivide(detail, budget, true, w, h));
        } else if (detail.isEmpty()) {
            selected.addAll(subdivide(interior, budget, false, w, h));
        } else {
            // Target detail budget: up to 70% of budget, reserving at least 30% for interior
            int targetDetail = Math.min(detail.size(), Math.max(1, (int) Math.round(budget * 0.70)));
            int targetInterior = budget - targetDetail;

            if (interior.size() < targetInterior) {
                targetInterior = interior.size();
                targetDetail = Math.min(detail.size(), budget - targetInterior);
            }
            if (detail.size() < targetDetail) {
                targetDetail = detail.size();
                targetInterior = Math.min(interior.size(), budget - targetDetail);
            }

            selected.addAll(subdivide(detail, targetDetail, true, w, h));
            selected.addAll(subdivide(interior, targetInterior, false, w, h));
        }

        // 3. Sort selected pixels deterministically by row-major position index
        selected.sort(Comparator.comparingInt(p -> p.y() * w + p.x()));

        return convertToSamples(selected, w, h);
    }

    private static int calculateImportance(CanvasData canvas, int x, int y, int argb, int w, int h) {
        int transparentNeighbors = 0;
        int maxColorDist = 0;

        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx < 0 || nx >= w || ny < 0 || ny >= h) {
                transparentNeighbors++;
                continue;
            }
            int nArgb = canvas.pixel(nx, ny);
            int na = nArgb >>> 24;
            if (na == 0) {
                transparentNeighbors++;
            } else {
                int nr = (nArgb >> 16) & 0xFF;
                int ng = (nArgb >> 8) & 0xFF;
                int nb = nArgb & 0xFF;
                int dist = Math.abs(r - nr) + Math.abs(g - ng) + Math.abs(b - nb);
                if (dist > maxColorDist) maxColorDist = dist;
            }
        }

        // Endpoint / tip / isolated pixel (3 or 4 transparent neighbors)
        if (transparentNeighbors >= 3) return 100;
        // Shape perimeter / contour (1 or 2 transparent neighbors)
        if (transparentNeighbors >= 1) return 80;
        // High-contrast internal color edge (e.g. eyes/mouth on face, contrasting shapes)
        if (maxColorDist > 64) return 60;
        // Subtle color contrast
        if (maxColorDist > 20) return 30;
        // Interior uniform fill
        return 10;
    }

    private static List<Pixel> subdivide(List<Pixel> pixels, int targetCount, boolean isDetail, int w, int h) {
        if (pixels.size() <= targetCount) return pixels;
        if (targetCount <= 0) return List.of();

        Cell root = createCell(pixels, isDetail, w);
        if (root == null) return List.of();

        PriorityQueue<Cell> pending = new PriorityQueue<>(CELL_ORDER);
        List<Cell> leaves = new ArrayList<>();
        pending.add(root);

        while (pending.size() + leaves.size() < targetCount && !pending.isEmpty()) {
            Cell c = pending.remove();
            if (c.pixels().size() == 1) {
                leaves.add(c);
                continue;
            }

            List<Pixel> leftGroup = new ArrayList<>();
            List<Pixel> rightGroup = new ArrayList<>();

            if (c.x1() - c.x0() >= c.y1() - c.y0()) {
                int mid = (c.x0() + c.x1()) / 2;
                for (Pixel p : c.pixels()) {
                    if (p.x() < mid) leftGroup.add(p);
                    else rightGroup.add(p);
                }
            } else {
                int mid = (c.y0() + c.y1()) / 2;
                for (Pixel p : c.pixels()) {
                    if (p.y() < mid) leftGroup.add(p);
                    else rightGroup.add(p);
                }
            }

            // Fallback if midpoint partition placed all pixels into one side
            if (leftGroup.isEmpty() || rightGroup.isEmpty()) {
                leftGroup.clear();
                rightGroup.clear();
                boolean splitX = (c.x1() - c.x0() >= c.y1() - c.y0());
                List<Pixel> sorted = new ArrayList<>(c.pixels());
                if (splitX) {
                    sorted.sort(Comparator.comparingInt(Pixel::x).thenComparingInt(Pixel::y));
                } else {
                    sorted.sort(Comparator.comparingInt(Pixel::y).thenComparingInt(Pixel::x));
                }
                int half = sorted.size() / 2;
                leftGroup.addAll(sorted.subList(0, half));
                rightGroup.addAll(sorted.subList(half, sorted.size()));
            }

            Cell cellLeft = createCell(leftGroup, isDetail, w);
            Cell cellRight = createCell(rightGroup, isDetail, w);

            if (cellLeft != null) pending.add(cellLeft);
            if (cellRight != null) pending.add(cellRight);
        }

        leaves.addAll(pending);
        List<Pixel> result = new ArrayList<>(leaves.size());
        for (Cell cell : leaves) {
            result.add(cell.representative());
        }
        return result;
    }

    private static Cell createCell(List<Pixel> pixels, boolean isDetail, int canvasWidth) {
        if (pixels == null || pixels.isEmpty()) return null;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        long sumX = 0, sumY = 0;

        for (Pixel p : pixels) {
            minX = Math.min(minX, p.x());
            minY = Math.min(minY, p.y());
            maxX = Math.max(maxX, p.x());
            maxY = Math.max(maxY, p.y());
            sumX += p.x();
            sumY += p.y();
        }

        double cx = sumX / (double) pixels.size();
        double cy = sumY / (double) pixels.size();

        Pixel best = null;
        if (isDetail) {
            // For detail cells: pick highest importance, then closest to geometric center, then tieRank
            int bestImportance = -1;
            double bestDist = Double.POSITIVE_INFINITY;
            int bestRank = Integer.MAX_VALUE;

            for (Pixel p : pixels) {
                double dist = (p.x() - cx) * (p.x() - cx) + (p.y() - cy) * (p.y() - cy);
                int index = p.y() * canvasWidth + p.x();
                int rank = tieRank(index);

                if (p.importance() > bestImportance
                        || (p.importance() == bestImportance && dist < bestDist)
                        || (p.importance() == bestImportance && dist == bestDist && rank < bestRank)) {
                    best = p;
                    bestImportance = p.importance();
                    bestDist = dist;
                    bestRank = rank;
                }
            }
        } else {
            // For interior cells: pick pixel closest to center of mass, then tieRank
            double bestDist = Double.POSITIVE_INFINITY;
            int bestRank = Integer.MAX_VALUE;

            for (Pixel p : pixels) {
                double dist = (p.x() - cx) * (p.x() - cx) + (p.y() - cy) * (p.y() - cy);
                int index = p.y() * canvasWidth + p.x();
                int rank = tieRank(index);

                if (dist < bestDist || (dist == bestDist && rank < bestRank)) {
                    best = p;
                    bestDist = dist;
                    bestRank = rank;
                }
            }
        }

        return new Cell(minX, minY, maxX + 1, maxY + 1, pixels, best);
    }

    private static int tieRank(int index) {
        int z = index + 0x9e3779b9;
        z = (z ^ (z >>> 16)) * 0x85ebca6b;
        z = (z ^ (z >>> 13)) * 0xc2b2ae35;
        return z ^ (z >>> 16);
    }

    private static List<CanvasDownsampler.Sample> convertToSamples(List<Pixel> pixels, int w, int h) {
        double scale = Math.max(w, h);
        List<CanvasDownsampler.Sample> result = new ArrayList<>(pixels.size());
        for (Pixel p : pixels) {
            double sx = (p.x() + 0.5 - w / 2.0) / scale;
            double sy = (h / 2.0 - p.y() - 0.5) / scale;
            result.add(new CanvasDownsampler.Sample(sx, sy, p.argb(), 1));
        }
        return List.copyOf(result);
    }
}
