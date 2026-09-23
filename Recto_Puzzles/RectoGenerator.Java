import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RectoGenerator {

    public static int[][] generate(int rows, int cols) {
        return generateUnique(rows, cols);
    }

    public static int[][] generateUnique(int rows, int cols) {
        int[][] grid = new int[rows][cols];
        Random rand = new Random();

        List<Recto.Rect> allRects = new ArrayList<>();
        int sectorSize = 6;

        for (int r = 0; r < rows; r += sectorSize) {
            int rEnd = Math.min(r + sectorSize - 1, rows - 1);
            for (int c = 0; c < cols; c += sectorSize) {
                int cEnd = Math.min(c + sectorSize - 1, cols - 1);
                
                List<Recto.Rect> sectorRects = generateSectorPartition(r, c, rEnd, cEnd, rand);
                allRects.addAll(sectorRects);
            }
        }

        for (Recto.Rect rect : allRects) {
            int h = rect.r2 - rect.r1 + 1;
            int w = rect.c2 - rect.c1 + 1;
            
            int clueR = rect.r1 + rand.nextInt(h);
            int clueC = rect.c1 + rand.nextInt(w);
            
            grid[clueR][clueC] = h + w;
        }

        return grid;
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

        boolean splitHorizontally;
        if (h > 2 && w > 2) {
            splitHorizontally = rand.nextBoolean();
        } else {
            splitHorizontally = (h > 2);
        }

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