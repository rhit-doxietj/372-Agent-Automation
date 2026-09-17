import java.util.ArrayList;
import java.util.List;

public class RectoTest {

    private static int passedCount = 0;
    private static int failedCount = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Running Recto Test Suite...\n");

        runTestCase("1x1 Grid", new int[][]{{2}}, true);

        runTestCase("1xn Grid (1x6)", new int[][]{
            {0, 3, 0, 0, 5, 0}
        }, true);

        runTestCase("nx1 Grid (6x1)", new int[][]{
            {0}, {3}, {0}, {0}, {5}, {0}
        }, true);

        runTestCase("nxn Grid (4x4)", new int[][]{
            {4, 0, 0, 4},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {4, 0, 0, 4}
        }, true);

        runTestCase("nxm Grid (3x6)", new int[][]{
            {0, 5, 0, 5, 0, 0},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 6, 0}
        }, true);

        runTestCase("mxn Grid (6x3)", new int[][]{
            {0, 0, 0},
            {5, 0, 0},
            {0, 0, 0},
            {5, 0, 0},
            {0, 0, 6},
            {0, 0, 0}
        }, true);

        runTestCase("Sheet Example (6x6)", new int[][]{
            {0, 0, 0, 0, 2, 0},
            {6, 0, 0, 0, 0, 0},
            {0, 0, 3, 0, 5, 0},
            {0, 5, 0, 4, 0, 0},
            {0, 0, 0, 0, 0, 6},
            {0, 7, 0, 0, 0, 0}
        }, true);

        runTestCase("Primary Puzzle (8x8)", new int[][]{
            {4, 6, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 3, 5, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 3, 2, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 5, 4, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 7, 6, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 6, 7}
        }, true);

        runTestCase("Unsolvable Contradiction Grid (2x2)", new int[][]{
            {2, 0},
            {0, 2}
        }, false);

        // Intentionally unsolvable 8x8:
        // Four 4x4 quadrants would require sums of 4+4=8, but clues of 7 only provide
        // candidates (h+w=7) that have a maximum area of 12 (3x4), leaving the remaining 16 cells untiled.
        runTestCase("Intentionally Unsolvable (8x8)", new int[][]{
            {7, 0, 0, 0, 0, 0, 0, 7},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {7, 0, 0, 0, 0, 0, 0, 7}
        }, false);

        // Solvable 8x8:
        // Clean symmetrical tiling into four 4x4 quadrants (h=4, w=4 -> sum=8).
        runTestCase("Solvable Quadrants (8x8)", new int[][]{
            {8, 0, 0, 0, 0, 0, 0, 8},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {8, 0, 0, 0, 0, 0, 0, 8}
        }, true);

        System.out.println("=========================================");
        System.out.println("TEST RESULTS SUMMARY");
        System.out.println("=========================================");
        System.out.printf("Total Passed : %d%n", passedCount);
        System.out.printf("Total Failed : %d%n", failedCount);

        if (failedCount > 0) {
            System.err.println("\nFailed Tests:");
            for (String failure : failures) {
                System.err.println(" - " + failure);
            }
            throw new AssertionError(failedCount + " test(s) failed!");
        } else {
            System.out.println("All tests passed successfully!");
        }
    }

    private static void runTestCase(String testName, int[][] puzzle, boolean expectedSolvable) {
        System.out.println("Running: " + testName);
        Recto solver = new Recto(puzzle);
        boolean actualSolvable = solver.solve();

        if (actualSolvable == expectedSolvable) {
            passedCount++;
            System.out.println("[PASS] " + testName + " (Solvable: " + actualSolvable + ")");
            if (actualSolvable) {
                solver.printSolution();
            }
        } else {
            failedCount++;
            String errorMsg = String.format("%s -> Expected solvable: %b, but got: %b",
                    testName, expectedSolvable, actualSolvable);
            failures.add(errorMsg);
            System.err.println("[FAIL] " + errorMsg);
        }
        System.out.println();
    }
}