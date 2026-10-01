package Kamino_Puzzles;

import java.util.List;

public class KaminoPuzzleTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    private static void displayBoard(String[][] grid, List<KaminoPuzzleSolver.Point> path) {
        if (grid.length > 10 || grid[0].length > 10) {
            System.out.println("[Board display omitted for large size: " + grid.length + "x" + grid[0].length + "]");
            return;
        }

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                KaminoPuzzleSolver.Point p = new KaminoPuzzleSolver.Point(r, c);
                boolean onPath = (path != null && path.contains(p));
                String cell = grid[r][c];
                if (onPath) {
                    System.out.printf("[%4s*]", cell);
                } else {
                    System.out.printf("[ %4s ]", cell);
                }
            }
            System.out.println();
        }
    }

    private static void runTest(String testTitle, String[][] grid, boolean expectSuccess) {
        System.out.println("==================================================");
        System.out.println(testTitle);
        System.out.println("==================================================");

        KaminoPuzzleSolver solver = new KaminoPuzzleSolver(grid);
        System.out.println("Start Position (S): " + solver.getStartPos());
        System.out.println("End Position   (E): " + solver.getTargetPos());
        System.out.println("Is Start > Halfway from End? " + (solver.isStartMoreThanHalfway() ? "YES" : "NO"));

        long startTime = System.currentTimeMillis();
        List<KaminoPuzzleSolver.Point> path = solver.solve();
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("\nBoard Layout (* marks path):");
        displayBoard(grid, path);

        if (path != null) {
            System.out.println("\nPath Found (" + (path.size() - 1) + " moves in " + elapsed + "ms):");
            if (path.size() <= 25) {
                for (int i = 0; i < path.size(); i++) {
                    System.out.print(path.get(i));
                    if (i < path.size() - 1) System.out.print(" -> ");
                    if ((i + 1) % 6 == 0) System.out.println();
                }
                System.out.println();
            } else {
                System.out.println(path.get(0) + " -> ... -> " + path.get(path.size() - 1) + " [truncated " + path.size() + " points]");
            }

            if (expectSuccess) {
                System.out.println("TEST RESULT: PASSED (Path found as expected)\n");
                totalPassed++;
            } else {
                System.out.println("TEST RESULT: FAILED (Expected NO path, but one was found)\n");
                totalFailed++;
            }
        } else {
            if (!expectSuccess) {
                System.out.println("\nNo path found (solved in " + elapsed + "ms).");
                System.out.println("TEST RESULT: PASSED (Correctly identified as unsolvable)\n");
                totalPassed++;
            } else {
                System.out.println("\nNo path found.");
                System.out.println("TEST RESULT: FAILED (Expected a path, but none was found)\n");
                totalFailed++;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Running Kamino Comprehensive Test Suite...\n");

        // ====================================================
        // PART 1: STATIC TESTS (Sizes 5x5 to 10x10)
        // ====================================================
        String[][] solvable5x5 = {
            { "9",   "8",   "7",   "6",   "S"   },
            { "8",   "7",   "6",   "5",   "9"   },
            { "7",   "6",   "5",   "4",   "7"   },
            { "6",   "5",   "4",   "3",   "5"   },
            { "E:3", "3",   "3",   "3",   "3"   }
        };
        runTest("5x5 - SOLVABLE", solvable5x5, true);

        String[][] unsolvable5x5 = {
            { "9",   "8",   "7",   "6",   "S"   },
            { "8",   "7",   "6",   "5",   "4"   },
            { "7",   "6",   "5",   "4",   "3"   },
            { "1",   "1",   "1",   "1",   "1"   },
            { "E:9", "1",   "1",   "1",   "1"   }
        };
        runTest("5x5 - UNSOLVABLE", unsolvable5x5, false);

        String[][] solvable10x10 = {
            { "9",   "8",   "7",   "6",   "5",   "4",   "3",   "2",   "1",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9",   "9"   },
            { "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8",   "8"   },
            { "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7",   "7"   },
            { "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7",   "6",   "6"   },
            { "5",   "5",   "5",   "5",   "5",   "5",   "5",   "5",   "5",   "5"   },
            { "3",   "4",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8"   },
            { "2",   "3",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8"   },
            { "2",   "2",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8"   },
            { "E:1", "1",   "9",   "8",   "7",   "6",   "5",   "4",   "3",   "2"   }
        };
        runTest("10x10 - SOLVABLE", solvable10x10, true);

        // ====================================================
        // PART 2: DYNAMIC GENERATOR TESTS (Sizes 3 to 75)
        // ====================================================
        System.out.println("==================================================");
        System.out.println("      AUTOMATED PUZZLE GENERATOR TESTS            ");
        System.out.println("==================================================");

        // Test 1: Small board (3x3)
        String[][] gen3x3 = KaminoPuzzleGenerator.generate(3);
        runTest("Generated 3x3 (Smallest Halfway Board)", gen3x3, true);

        // Test 2: Medium board (8x8)
        String[][] gen8x8 = KaminoPuzzleGenerator.generate(8);
        runTest("Generated 8x8 Solvable", gen8x8, true);

        // Test 3: Large board (20x20)
        String[][] gen20x20 = KaminoPuzzleGenerator.generate(20);
        runTest("Generated 20x20 Solvable", gen20x20, true);

        // Test 4: Extra large board (50x50)
        String[][] gen50x50 = KaminoPuzzleGenerator.generate(50);
        runTest("Generated 50x50 Solvable", gen50x50, true);

        // Test 5: Maximum size board (75x75 Solvable)
        String[][] gen75x75 = KaminoPuzzleGenerator.generate(75);
        runTest("Generated 75x75 Solvable (Max Spec)", gen75x75, true);

        // Test 6: Maximum size board (75x75 Unsolvable)
        String[][] gen75x75Unsolvable = KaminoPuzzleGenerator.generate(75, false);
        runTest("Generated 75x75 Unsolvable (Max Spec)", gen75x75Unsolvable, false);

        // ====================================================
        // FINAL SUMMARY REPORT
        // ====================================================
        int totalTests = totalPassed + totalFailed;
        System.out.println("==================================================");
        System.out.println("                TEST SUITE SUMMARY                ");
        System.out.println("==================================================");
        System.out.println("Total Tests Run:    " + totalTests);
        System.out.println("Total Tests Passed: " + totalPassed);
        System.out.println("Total Tests Failed: " + totalFailed);
        System.out.println("Success Rate:       " + String.format("%.1f", ((double) totalPassed / totalTests) * 100) + "%");
        System.out.println("==================================================");
    }
}