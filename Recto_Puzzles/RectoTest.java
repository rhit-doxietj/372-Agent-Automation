public class RectoTest {

    public static void main(String[] args) {
        System.out.println("Running Recto test suite...\n");

        testSingleCellGrid();       // 1x1
        testSingleRowStrip();       // 1xn
        testSingleColStrip();       // nx1
        testSquareGrid();           // nxn (4x4)
        testWideRectangleGrid();    // nxm (3x6 where n < m)
        testTallRectangleGrid();    // mxn (6x3 where m > n)
        testExampleFromImage();     // 6x6 sheet example
        testOriginal8x8Puzzle();    // 8x8 primary puzzle
        testUnsolvableGrid();       // Contradiction / invalid case

        System.out.println("\nAll tests passed successfully!");
    }

    /**
     * 1x1 Grid: Single cell with a single 1x1 region (h=1, w=1 -> sum=2).
     */
    private static void testSingleCellGrid() {
        System.out.println("--- Test 1: 1x1 Grid ---");
        int[][] puzzle = {
            {2}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "1x1 grid should be solvable";
        solver.printSolution();
    }

    /**
     * 1xn Grid (1x6): Single row partitioned into 1x2 (sum=3) and 1x4 (sum=5).
     */
    private static void testSingleRowStrip() {
        System.out.println("\n--- Test 2: 1xn Grid (1x6) ---");
        int[][] puzzle = {
            {0, 3, 0, 0, 5, 0}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "1xn strip should be solvable";
        solver.printSolution();
    }

    /**
     * nx1 Grid (6x1): Single column partitioned into 2x1 (sum=3) and 4x1 (sum=5).
     */
    private static void testSingleColStrip() {
        System.out.println("\n--- Test 3: nx1 Grid (6x1) ---");
        int[][] puzzle = {
            {0},
            {3},
            {0},
            {0},
            {5},
            {0}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "nx1 strip should be solvable";
        solver.printSolution();
    }

    /**
     * nxn Grid (4x4): Square grid split into four 2x2 quadrants (h=2, w=2 -> sum=4).
     */
    private static void testSquareGrid() {
        System.out.println("\n--- Test 4: nxn Grid (4x4) ---");
        int[][] puzzle = {
            {4, 0, 0, 4},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {4, 0, 0, 4}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "4x4 square grid should be solvable";
        solver.printSolution();
    }

    /**
     * nxm Grid (3x6, rows < cols):
     * Partitioned into:
     * - Top left: 3x2 (sum=5)
     * - Top right: 1x4 (sum=5)
     * - Bottom right: 2x4 (sum=6)
     */
    private static void testWideRectangleGrid() {
        System.out.println("\n--- Test 5: nxm Grid (3x6) ---");
        int[][] puzzle = {
            {0, 5, 0, 5, 0, 0},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 6, 0}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "3x6 grid should be solvable";
        solver.printSolution();
    }

    /**
     * mxn Grid (6x3, rows > cols):
     * Transposed layout of the 3x6:
     * - Top left: 2x3 (sum=5)
     * - Bottom left: 4x1 (sum=5)
     * - Bottom right: 4x2 (sum=6)
     */
    private static void testTallRectangleGrid() {
        System.out.println("\n--- Test 6: mxn Grid (6x3) ---");
        int[][] puzzle = {
            {0, 0, 0},
            {5, 0, 0},
            {0, 0, 0},
            {5, 0, 0},
            {0, 0, 6},
            {0, 0, 0}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "6x3 grid should be solvable";
        solver.printSolution();
    }

    /**
     * 6x6 Example from the top-left corner of the newspaper clipping.
     */
    private static void testExampleFromImage() {
        System.out.println("\n--- Test 7: Example from Puzzle Sheet (6x6) ---");
        int[][] puzzle = {
            {0, 0, 0, 0, 2, 0},
            {6, 0, 0, 0, 0, 0},
            {0, 0, 3, 0, 5, 0},
            {0, 5, 0, 4, 0, 0},
            {0, 0, 0, 0, 0, 6},
            {0, 7, 0, 0, 0, 0}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "Example 6x6 puzzle should be solvable";
        solver.printSolution();
    }

    /**
     * The main 8x8 puzzle from the image.
     */
    private static void testOriginal8x8Puzzle() {
        System.out.println("\n--- Test 8: Main Puzzle from Sheet (8x8) ---");
        int[][] puzzle = {
            {4, 6, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 3, 5, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 3, 2, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 5, 4, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 7, 6, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 6, 7, 0}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "Main 8x8 puzzle should be solvable";
        solver.printSolution();
    }

    /**
     * Contradiction: Sums cannot tile the cells.
     */
    private static void testUnsolvableGrid() {
        System.out.println("\n--- Test 9: Unsolvable Contradiction Grid ---");
        int[][] puzzle = {
            {2, 0},
            {0, 2}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert !solved : "Unsolvable puzzle must return false";
        System.out.println("Correctly identified unsolvable state (solved = " + solved + ").");
    }
}