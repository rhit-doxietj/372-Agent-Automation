public class RectoTest {

    public static void main(String[] args) {
        System.out.println("Running recto test suite...\n");

        testExampleFromImage();
        testSingleCellGrid();
        testSingleRowStrip();
        testFourCornersGrid();
        testUnsolvableGrid();

        System.out.println("\nAll tests passed successfully!");
    }

    private static void testExampleFromImage() {
        System.out.println("--- Test 1: Example from Puzzle Sheet (6x6) ---");
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

    private static void testSingleCellGrid() {
        System.out.println("\n--- Test 2: Minimal 1x1 Grid ---");
        int[][] puzzle = {
            {2}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "1x1 grid should be solvable";
        solver.printSolution();
    }

    private static void testSingleRowStrip() {
        System.out.println("\n--- Test 3: 1x6 Strip Grid ---");
        int[][] puzzle = {
            {0, 3, 0, 0, 5, 0}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "1x6 strip should be solvable";
        solver.printSolution();
    }

    private static void testFourCornersGrid() {
        System.out.println("\n--- Test 4: Four 2x2 Quadrants (4x4) ---");
        int[][] puzzle = {
            {4, 0, 0, 4},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {4, 0, 0, 4}
        };

        Recto solver = new Recto(puzzle);
        boolean solved = solver.solve();
        assert solved : "4x4 quadrants grid should be solvable";
        solver.printSolution();
    }

    private static void testUnsolvableGrid() {
        System.out.println("\n--- Test 5: Unsolvable Contradiction Grid ---");
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