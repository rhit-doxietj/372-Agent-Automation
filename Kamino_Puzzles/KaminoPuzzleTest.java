package Kamino_Puzzles;

import java.util.List;

public class KaminoPuzzleTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    private static void displayBoard(String[][] grid, List<KaminoPuzzleSolver.Point> path) {
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

        List<KaminoPuzzleSolver.Point> path = solver.solve();

        System.out.println("\nBoard Layout (* marks path):");
        displayBoard(grid, path);

        if (path != null) {
            System.out.println("\nPath Found (" + (path.size() - 1) + " moves):");
            for (int i = 0; i < path.size(); i++) {
                System.out.print(path.get(i));
                if (i < path.size() - 1) System.out.print(" -> ");
                if ((i + 1) % 6 == 0) System.out.println();
            }
            System.out.println();

            if (expectSuccess) {
                System.out.println("TEST RESULT: PASSED (Path found as expected)\n");
                totalPassed++;
            } else {
                System.out.println("TEST RESULT: FAILED (Expected NO path, but one was found)\n");
                totalFailed++;
            }
        } else {
            if (!expectSuccess) {
                System.out.println("\nNo path found.");
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
        System.out.println("Running Kamino Test Suite (Sizes 5x5 to 10x10)...\n");

        // ====================================================
        // 5x5 GRIDS
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
        runTest("5x5 - UNSOLVABLE (Walled-in Target)", unsolvable5x5, false);

        // ====================================================
        // 6x6 GRIDS
        // ====================================================
        String[][] solvable6x6 = {
            { "9",   "8",   "7",   "6",   "5",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "8"   },
            { "7",   "6",   "5",   "4",   "3",   "6"   },
            { "6",   "5",   "4",   "3",   "2",   "4"   },
            { "5",   "2",   "2",   "2",   "2",   "2"   },
            { "4",   "E:1", "3",   "4",   "5",   "6"   }
        };
        runTest("6x6 - SOLVABLE", solvable6x6, true);

        String[][] unsolvable6x6 = {
            { "9",   "8",   "7",   "6",   "5",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3"   },
            { "9",   "9",   "9",   "9",   "9",   "9"   },
            { "1",   "2",   "3",   "4",   "5",   "6"   },
            { "2",   "3",   "4",   "5",   "6",   "7"   },
            { "E:2", "3",   "4",   "5",   "6",   "7"   }
        };
        runTest("6x6 - UNSOLVABLE (Horizontal Monotonic Barrier)", unsolvable6x6, false);

        // ====================================================
        // 7x7 GRIDS
        // ====================================================
        String[][] solvable7x7 = {
            { "9",   "8",   "7",   "6",   "5",   "4",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3",   "8"   },
            { "7",   "6",   "5",   "4",   "3",   "2",   "7"   },
            { "6",   "5",   "4",   "3",   "2",   "1",   "6"   },
            { "5",   "4",   "3",   "2",   "1",   "9",   "5"   },
            { "3",   "4",   "4",   "4",   "4",   "4",   "4"   },
            { "E:2", "2",   "3",   "4",   "5",   "6",   "7"   }
        };
        runTest("7x7 - SOLVABLE", solvable7x7, true);

        String[][] unsolvable7x7 = {
            { "9",   "8",   "7",   "6",   "5",   "1",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3",   "9"   },
            { "7",   "6",   "5",   "4",   "3",   "2",   "9"   },
            { "6",   "5",   "4",   "3",   "2",   "1",   "9"   },
            { "5",   "4",   "3",   "2",   "1",   "9",   "9"   },
            { "4",   "3",   "2",   "1",   "9",   "8",   "9"   },
            { "E:2", "2",   "3",   "4",   "5",   "6",   "7"   }
        };
        runTest("7x7 - UNSOLVABLE (Trapped Start Node)", unsolvable7x7, false);

        // ====================================================
        // 8x8 GRIDS
        // ====================================================
        String[][] solvable8x8 = {
            { "9",   "8",   "7",   "6",   "5",   "4",   "3",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3",   "2",   "8"   },
            { "7",   "6",   "5",   "4",   "3",   "2",   "1",   "7"   },
            { "6",   "5",   "4",   "3",   "2",   "1",   "9",   "6"   },
            { "5",   "4",   "5",   "5",   "5",   "5",   "5",   "5"   },
            { "4",   "3",   "4",   "3",   "9",   "8",   "7",   "6"   },
            { "3",   "2",   "3",   "2",   "9",   "8",   "7",   "6"   },
            { "E:2", "2",   "2",   "7",   "6",   "5",   "4",   "3"   }
        };
        runTest("8x8 - SOLVABLE", solvable8x8, true);

        String[][] unsolvable8x8 = {
            { "1",   "2",   "3",   "4",   "5",   "6",   "7",   "S"   },
            { "2",   "3",   "4",   "5",   "6",   "7",   "8",   "9"   },
            { "3",   "4",   "5",   "6",   "7",   "8",   "9",   "1"   },
            { "4",   "5",   "6",   "7",   "8",   "9",   "1",   "2"   },
            { "5",   "6",   "7",   "8",   "9",   "1",   "2",   "3"   },
            { "6",   "7",   "8",   "9",   "1",   "2",   "3",   "4"   },
            { "7",   "8",   "9",   "1",   "2",   "3",   "4",   "5"   },
            { "E:1", "9",   "1",   "2",   "3",   "4",   "5",   "6"   }
        };
        runTest("8x8 - UNSOLVABLE (Fully Inverted Gradient)", unsolvable8x8, false);

        // ====================================================
        // 9x9 GRIDS
        // ====================================================
        String[][] solvable9x9 = {
            { "9",   "8",   "7",   "6",   "5",   "4",   "3",   "2",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9"   },
            { "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8"   },
            { "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7"   },
            { "5",   "4",   "6",   "6",   "6",   "6",   "6",   "6",   "6"   },
            { "4",   "3",   "5",   "1",   "2",   "3",   "4",   "5",   "6"   },
            { "3",   "2",   "4",   "1",   "2",   "3",   "4",   "5",   "6"   },
            { "2",   "1",   "3",   "1",   "2",   "3",   "4",   "5",   "6"   },
            { "E:2", "2",   "2",   "9",   "8",   "7",   "6",   "5",   "4"   }
        };
        runTest("9x9 - SOLVABLE", solvable9x9, true);

        String[][] unsolvable9x9 = {
            { "9",   "8",   "7",   "6",   "5",   "4",   "3",   "2",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9"   },
            { "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8"   },
            { "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7"   },
            { "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7",   "6"   },
            { "4",   "3",   "2",   "1",   "9",   "8",   "7",   "6",   "5"   },
            { "3",   "2",   "1",   "9",   "8",   "7",   "6",   "5",   "4"   },
            { "9",   "9",   "9",   "9",   "9",   "9",   "9",   "9",   "9"   },
            { "E:1", "9",   "1",   "2",   "3",   "4",   "5",   "6",   "7"   }
        };
        runTest("9x9 - UNSOLVABLE (Impassable Final Wall)", unsolvable9x9, false);

        // ====================================================
        // 10x10 GRIDS
        // ====================================================
        String[][] solvable10x10 = {
            { "9",   "8",   "7",   "6",   "5",   "4",   "3",   "2",   "1",   "S"   },
            { "8",   "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8"   },
            { "7",   "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7"   },
            { "6",   "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7",   "6"   },
            { "5",   "4",   "3",   "2",   "1",   "9",   "8",   "7",   "6",   "6"   },
            { "5",   "5",   "5",   "5",   "5",   "5",   "5",   "5",   "5",   "5"   },
            { "3",   "4",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8"   },
            { "2",   "3",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8"   },
            { "2",   "2",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8"   },
            { "E:1", "1",   "9",   "8",   "7",   "6",   "5",   "4",   "3",   "2"   }
        };
        runTest("10x10 - SOLVABLE", solvable10x10, true);

        String[][] unsolvable10x10 = {
            { "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8",   "9",   "S"   },
            { "2",   "3",   "4",   "5",   "6",   "7",   "8",   "9",   "1",   "2"   },
            { "3",   "4",   "5",   "6",   "7",   "8",   "9",   "1",   "2",   "3"   },
            { "4",   "5",   "6",   "7",   "8",   "9",   "1",   "2",   "3",   "4"   },
            { "5",   "6",   "7",   "8",   "9",   "1",   "2",   "3",   "4",   "5"   },
            { "6",   "7",   "8",   "9",   "1",   "2",   "3",   "4",   "5",   "6"   },
            { "7",   "8",   "9",   "1",   "2",   "3",   "4",   "5",   "6",   "7"   },
            { "8",   "9",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8"   },
            { "9",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8",   "9"   },
            { "E:1", "2",   "3",   "4",   "5",   "6",   "7",   "8",   "9",   "1"   }
        };
        runTest("10x10 - UNSOLVABLE (Zero Horizontal Bridges)", unsolvable10x10, false);

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