import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RectoGenerator {

    public static class GenerationResult {
        public int[][] grid;
        public List<Recto.Rect> partitions;

        public GenerationResult(int[][] grid, List<Recto.Rect> partitions) {
            this.grid = grid;
            this.partitions = partitions;
        }
    }

    public static GenerationResult generateWithPartitions(int rows, int cols) {
        Random rand = new Random();
        int attempts = 0;
        int maxAttempts = (rows > 12 || cols > 12) ? 10 : 100;

        int sectorSize = Math.min(6, Math.max(3, Math.min(rows, cols) / 2));

        while (attempts < maxAttempts) {
            attempts++;
            int[][] grid = new int[rows][cols];
            List<Recto.Rect> allRects = new ArrayList<>();

            for (int r = 0; r < rows; r += sectorSize) {
                int rEnd = Math.min(r + sectorSize - 1, rows - 1);
                for (int c = 0; c < cols; c += sectorSize) {
                    int cEnd = Math.min(c + sectorSize - 1, cols - 1);
                    allRects.addAll(generateSectorPartition(r, c, rEnd, cEnd, rand));
                }
            }

            for (int i = 0; i < allRects.size(); i++) {
                Recto.Rect rect = allRects.get(i);
                rect.clueId = i;
                int h = rect.r2 - rect.r1 + 1;
                int w = rect.c2 - rect.c1 + 1;

                int clueR = rect.r1 + rand.nextInt(h);
                int clueC = rect.c1 + rand.nextInt(w);

                grid[clueR][clueC] = h + w;
            }

            if (rows <= 12 && cols <= 12) {
                Recto solver = new Recto(grid);
                if (solver.countSolutions(2) == 1) {
                    return new GenerationResult(grid, allRects);
                }
            } else {
                return new GenerationResult(grid, allRects);
            }
        }

        return generateFallbackResult(rows, cols, rand);
    }

    private static GenerationResult generateFallbackResult(int rows, int cols, Random rand) {
        int[][] grid = new int[rows][cols];
        List<Recto.Rect> allRects = new ArrayList<>();

        int sectorSize = 4;
        for (int r = 0; r < rows; r += sectorSize) {
            int rEnd = Math.min(r + sectorSize - 1, rows - 1);
            for (int c = 0; c < cols; c += sectorSize) {
                int cEnd = Math.min(c + sectorSize - 1, cols - 1);
                allRects.addAll(generateSectorPartition(r, c, rEnd, cEnd, rand));
            }
        }

        for (int i = 0; i < allRects.size(); i++) {
            Recto.Rect rect = allRects.get(i);
            rect.clueId = i;
            int h = rect.r2 - rect.r1 + 1;
            int w = rect.c2 - rect.c1 + 1;
            int clueR = rect.r1 + rand.nextInt(h);
            int clueC = rect.c1 + rand.nextInt(w);
            grid[clueR][clueC] = h + w;
        }
        return new GenerationResult(grid, allRects);
    }

    private static List<Recto.Rect> generateSectorPartition(int r1, int c1, int r2, int c2, Random rand) {
        List<Recto.Rect> result = new ArrayList<>();
        subdivide(r1, c1, r2, c2, result, rand);
        return result;
    }

    private static void subdivide(int r1, int c1, int r2, int c2, List<Recto.Rect> result, Random rand) {
        int h = r2 - r1 + 1;
        int w = c2 - c1 + 1;

        if (h <= 3 && w <= 3 && (h * w <= 6 || rand.nextBoolean())) {
            result.add(new Recto.Rect(-1, r1, c1, r2, c2));
            return;
        }

        boolean splitHorizontally = (h > 2 && w > 2) ? rand.nextBoolean() : (h > 2);

        if (splitHorizontally && h > 2) {
            int split = r1 + 1 + rand.nextInt(h - 2);
            subdivide(r1, c1, split, c2, result, rand);
            subdivide(split + 1, c1, r2, c2, result, rand);
        } else if (w > 2) {
            int split = c1 + 1 + rand.nextInt(w - 2);
            subdivide(r1, c1, r2, split, result, rand);
            subdivide(r1, split + 1, r2, c2, result, rand);
        } else {
            result.add(new Recto.Rect(-1, r1, c1, r2, c2));
        }
    }

    
}