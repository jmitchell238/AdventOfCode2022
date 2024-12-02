package org.jmitchell238.aoc.aoc2023.day10;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import lombok.Getter;
import lombok.Setter;
import org.jmitchell238.aoc.aoc2023.utilities.grid.FloodFill;

public class Day10 {
    private static final Boolean DEBUGGING = false;
    private static final Boolean VERBOSE = false;
    private static final Boolean DRAW_MAP = true;

    @Getter
    @Setter
    private Boolean isPartTwo = false;

    ArrayList<char[]> char2dArrayList = new ArrayList<>();
    Map<Point, Character> grid = new HashMap<>();
    Map<Point, Character> doubledGrid = new HashMap<>();
    Map<Point, Character> doubledGridFilledIn = new HashMap<>();
    ArrayList<Point> pipesPathPoints = new ArrayList<>();
    ArrayList<Point> pipesPathPoints2 = new ArrayList<>();

    public void main(String[] args) throws FileNotFoundException {
        Day10Run();
    }

    public void Day10Run() throws FileNotFoundException {
        System.out.println("\n--- Day Day 10: Pipe Maze ---\n");

        String input = "src/main/java/org/jmitchell238/aoc/aoc2023/day10/input.txt";
        String inputTest = "src/main/java/org/jmitchell238/aoc/aoc2023/day10/input_test.txt";

        long partOneAnswer = part1(input);
        System.out.printf("Part 1: Answer: %d\n", partOneAnswer);

        long partTwoAnswer = part2(input);
        System.out.printf("Part 2: Answer: %d\n", partTwoAnswer);
    }

    public long part1(String filePath) throws FileNotFoundException {
        createCoordinateMap(filePath);

        int totalSteps = countTotalSteps();
        int stepsToCenter = totalSteps / 2;

        if (DRAW_MAP) {
            drawMap();
        }

        reset();
        return stepsToCenter;
    }

    public long part2(String filePath) throws FileNotFoundException {
        createCoordinateMap(filePath);
        addPipesToArrayList();
        addOddRowsAndColumnsToDoubledGrid();

        if (DRAW_MAP) {
            drawMap3();
        }

        FloodFill floodFill = new FloodFill(doubledGridFilledIn, pipesPathPoints2, doubledGrid);

        return floodFill.countPointsInsidePipe(new Point(148, 154));
    }

    private void createCoordinateMap(String filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filePath));

        while (scanner.hasNextLine()) {
            char2dArrayList.add(scanner.nextLine().toCharArray());
        }

        scanner.close();

        for (int y = 0; y < char2dArrayList.size(); y++) {
            char[] row = char2dArrayList.get(y);
            for (int x = 0; x < row.length; x++) {
                Point originalPoint = new Point(x, y);
                Character originalChar = row[x];

                // Add to the original grid
                grid.put(originalPoint, originalChar);

                // Double the coordinates and add to the doubled grid
                doubledGrid.put(new Point(x * 2, y * 2), originalChar);

                if (originalChar == 'S') {
                    System.out.printf("Start Point: %s\n", originalPoint);
                    System.out.printf("Start Point in double map: %s\n", new Point(x * 2, y * 2));
                }
            }
        }

        if (DEBUGGING) {
            for (int y = 0; y < char2dArrayList.size(); y++) {
                for (int x = 0; x < char2dArrayList.getFirst().length; x++) {
                    Point currentPoint = new Point(x, y);
                    Character currentCharacter = grid.get(currentPoint);
                    System.out.print(currentCharacter);
                }

                System.out.println();
            }

            Point StartPoint = grid.keySet().stream()
                    .filter(p -> grid.get(p) == 'S')
                    .findFirst()
                    .get();
            System.out.printf("Start Point: %s\n", StartPoint);

            addSurroundingPoints(char2dArrayList.getFirst().length, char2dArrayList.size());
        }
    }

    private void addOddRowsAndColumnsToDoubledGrid() {
        // Copy values from doubledGrid to doubledGrid2 using stream
        doubledGrid.entrySet().stream().forEach(entry -> doubledGridFilledIn.put(entry.getKey(), entry.getValue()));

        pipesPathPoints2.add(new Point(148, 165));
        doubledGridFilledIn.put(new Point(148, 165), '|');

        for (int x = 0; x < char2dArrayList.getFirst().length * 2; x++) {
            for (int y = 0; y < char2dArrayList.size() * 2; y++) {
                Point currentPoint = new Point(x, y);

                if (doubledGridFilledIn.get(currentPoint) != null) {
                    continue;
                }

                if (doubledGridFilledIn.get(currentPoint) == null) {
                    Point pointLeft = new Point(x - 1, y);
                    if (doubledGridFilledIn.get(pointLeft) == null) {
                        doubledGridFilledIn.put(currentPoint, '.');
                        continue;
                    }

                    Character charLeft = doubledGridFilledIn.get(pointLeft);

                    if (charLeft == 'F' || charLeft == '-' || charLeft == 'L') {
                        doubledGridFilledIn.put(currentPoint, '-');
                        continue;
                    }

                    Point pointAbove = new Point(x, y - 1);
                    if (doubledGridFilledIn.get(pointAbove) == null) {
                        doubledGridFilledIn.put(currentPoint, '.');
                        continue;
                    }

                    Character charAbove = doubledGridFilledIn.get(pointAbove);

                    if (charAbove == 'F' || charAbove == '7' || charAbove == '|' || charAbove == 'S') {
                        doubledGridFilledIn.put(currentPoint, '|');
                        continue;
                    }

                    doubledGridFilledIn.put(currentPoint, '.');
                }
            }
        }
    }

    private void addSurroundingPoints(int xLength, int yLength) {
        // Add row of '.'s to the top and bottom of the grid
        for (int i = -1; i <= yLength; i++) {
            Point topRowPoint = new Point(i, -1);
            Point bottomRowPoint = new Point(i, yLength);

            grid.put(topRowPoint, '.');
            grid.put(bottomRowPoint, '.');
        }

        // Add column of '.'s to the left and right of the grid
        for (int i = -1; i <= xLength + 1; i++) {
            Point leftColumnPoint = new Point(-1, i);
            Point rightColumnPoint = new Point(xLength, i);
            Point rightColumnPoint2 = new Point(xLength + 1, i);

            grid.put(leftColumnPoint, '.');
            grid.put(rightColumnPoint, '.');
        }
    }

    private int countTotalSteps() {
        Point StartPoint = grid.keySet().stream()
                .filter(p -> grid.get(p) == 'S')
                .findFirst()
                .get();

        boolean isSearching = true;
        Point currentPoint = StartPoint;
        Point previousPoint = StartPoint;
        int steps = 0;

        if (grid.get(currentPoint) == 'S') {
            if (DEBUGGING) {
                System.out.printf("Starting to traverse pipes.\n");
            }

            Point pointAbove = new Point(currentPoint.x, currentPoint.y - 1);
            char charAbove = ' ';
            if (grid.containsKey(pointAbove)) {
                charAbove = grid.get(pointAbove);
            }
            Point pointBelow = new Point(currentPoint.x, currentPoint.y + 1);
            char charBelow = ' ';
            if (grid.containsKey(pointBelow)) {
                charBelow = grid.get(pointBelow);
            }
            Point pointLeft = new Point(currentPoint.x - 1, currentPoint.y);
            char charLeft = ' ';
            if (grid.containsKey(pointLeft)) {
                charLeft = grid.get(pointLeft);
            }
            Point pointRight = new Point(currentPoint.x + 1, currentPoint.y);
            char charRight = ' ';
            if (grid.containsKey(pointRight)) {
                charRight = grid.get(pointRight);
            }

            if (charAbove != ' ' && (charAbove == '|' || charAbove == '7' || charAbove == 'F')) {
                if (DEBUGGING) {
                    System.out.printf("Point Above: %s\n", pointAbove);
                    System.out.printf("Char Above: %s\n", charAbove);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointAbove;
                addPipePointToArrayList(currentPoint);
            } else if (charRight != ' ' && (charRight == '-' || charRight == '7' || charRight == 'J')) {
                if (DEBUGGING) {
                    System.out.printf("Point Right: %s\n", pointRight);
                    System.out.printf("Char Right: %s\n", charRight);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointRight;
                addPipePointToArrayList(currentPoint);
            } else if (charBelow != ' ' && (charBelow == '|' || charBelow == 'J' || charBelow == 'L')) {
                if (DEBUGGING) {
                    System.out.printf("Point Below: %s\n", pointBelow);
                    System.out.printf("Char Below: %s\n", charBelow);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointBelow;
                addPipePointToArrayList(currentPoint);
            } else if (charLeft != ' ' && (charLeft == '-' || charLeft == 'F' || charLeft == 'L')) {
                if (DEBUGGING) {
                    System.out.printf("Point Left: %s\n", pointLeft);
                    System.out.printf("Char Left: %s\n", charLeft);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointLeft;
                addPipePointToArrayList(currentPoint);
            }
        }

        while (isSearching) {
            if (grid.get(currentPoint) == 'S' && !Objects.equals(previousPoint.toString(), StartPoint.toString())) {
                if (DEBUGGING) {
                    System.out.printf("Found Start Point\n");
                    System.out.printf("Steps: %d\n", steps);
                }
                isSearching = false;
                break;
            }

            Point pointAbove = new Point(currentPoint.x, currentPoint.y - 1);
            Point pointBelow = new Point(currentPoint.x, currentPoint.y + 1);
            Point pointLeft = new Point(currentPoint.x - 1, currentPoint.y);
            Point pointRight = new Point(currentPoint.x + 1, currentPoint.y);

            char currentChar = grid.get(currentPoint);

            if (currentChar == '|') {
                char charAbove = grid.get(pointAbove);
                char charBelow = grid.get(pointBelow);

                if (Objects.equals(pointAbove.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Above: %s\n", pointBelow);
                        System.out.printf("Char Above: %s\n", charBelow);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointBelow;
                    addPipePointToArrayList(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Below: %s\n", pointAbove);
                        System.out.printf("Char Below: %s\n", charAbove);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointAbove;
                    addPipePointToArrayList(currentPoint);

                    continue;
                }
            }

            if (currentChar == '-') {
                char charLeft = grid.get(pointLeft);
                char charRight = grid.get(pointRight);

                if (Objects.equals(pointLeft.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Right: %s\n", pointRight);
                        System.out.printf("Char Right: %s\n", charRight);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointRight;
                    addPipePointToArrayList(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Left: %s\n", pointLeft);
                        System.out.printf("Char Left: %s\n", charLeft);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointLeft;
                    addPipePointToArrayList(currentPoint);

                    continue;
                }
            }

            if (currentChar == '7') {
                char charLeft = grid.get(pointLeft);
                char charBelow = grid.get(pointBelow);

                if (Objects.equals(pointLeft.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Below: %s\n", pointBelow);
                        System.out.printf("Char Below: %s\n", charBelow);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointBelow;
                    addPipePointToArrayList(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Left: %s\n", pointLeft);
                        System.out.printf("Char Left: %s\n", charLeft);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointLeft;
                    addPipePointToArrayList(currentPoint);

                    continue;
                }
            }

            if (currentChar == 'J') {
                char charLeft = grid.get(pointLeft);
                char charAbove = grid.get(pointAbove);

                if (Objects.equals(pointLeft.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Above: %s\n", pointAbove);
                        System.out.printf("Char Above: %s\n", charAbove);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointAbove;
                    addPipePointToArrayList(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Left: %s\n", pointLeft);
                        System.out.printf("Char Left: %s\n", charLeft);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointLeft;
                    addPipePointToArrayList(currentPoint);

                    continue;
                }
            }

            if (currentChar == 'L') {
                char charRight = grid.get(pointRight);
                char charAbove = grid.get(pointAbove);

                if (Objects.equals(pointRight.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Above: %s\n", pointAbove);
                        System.out.printf("Char Above: %s\n", charAbove);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointAbove;
                    addPipePointToArrayList(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Right: %s\n", pointRight);
                        System.out.printf("Char Right: %s\n", charRight);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointRight;
                    addPipePointToArrayList(currentPoint);

                    continue;
                }
            }

            if (currentChar == 'F') {
                char charRight = grid.get(pointRight);
                char charBelow = grid.get(pointBelow);

                if (Objects.equals(pointRight.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Below: %s\n", pointBelow);
                        System.out.printf("Char Below: %s\n", charBelow);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointBelow;
                    addPipePointToArrayList(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Right: %s\n", pointRight);
                        System.out.printf("Char Right: %s\n", charRight);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointRight;
                    addPipePointToArrayList(currentPoint);

                    continue;
                }
            }
        }

        //    int notEnclosedTiles = getCountOfOutsideTiles();

        return steps;
    }

    private void addPipesToArrayList() {
        Point StartPoint = doubledGrid.keySet().stream()
                .filter(p -> doubledGrid.get(p) == 'S')
                .findFirst()
                .get();
        System.out.printf("Start Point: %s\n", StartPoint);

        boolean isSearching = true;
        Point currentPoint = StartPoint;
        Point previousPoint = StartPoint;
        int steps = 0;

        if (doubledGrid.get(currentPoint) == 'S') {
            if (DEBUGGING) {
                System.out.print("Starting to traverse pipes.\n");
            }

            Point pointAbove = new Point(currentPoint.x, currentPoint.y - 2);
            char charAbove = doubledGrid.get(pointAbove);
            Point pointBelow = new Point(currentPoint.x, currentPoint.y + 2);
            char charBelow = doubledGrid.get(pointBelow);
            Point pointLeft = new Point(currentPoint.x - 2, currentPoint.y);
            char charLeft = doubledGrid.get(pointLeft);
            Point pointRight = new Point(currentPoint.x + 2, currentPoint.y);
            char charRight = doubledGrid.get(pointRight);

            if (charAbove == '|' || charAbove == '7' || charAbove == 'F') {
                if (DEBUGGING) {
                    System.out.printf("Point Above: %s\n", pointAbove);
                    System.out.printf("Char Above: %s\n", charAbove);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointAbove;
                Point pointAbove2 = new Point(currentPoint.x, currentPoint.y - 1);
                addPipePointToArrayList2(currentPoint);
                addPipePointToArrayList2(pointAbove2);
            } else if (charRight == '-' || charRight == '7' || charRight == 'J') {
                if (DEBUGGING) {
                    System.out.printf("Point Right: %s\n", pointRight);
                    System.out.printf("Char Right: %s\n", charRight);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointRight;
                Point pointRight2 = new Point(currentPoint.x + 1, currentPoint.y);
                addPipePointToArrayList2(currentPoint);
                addPipePointToArrayList2(pointRight2);
            } else if (charBelow == '|' || charBelow == 'J' || charBelow == 'L') {
                if (DEBUGGING) {
                    System.out.printf("Point Below: %s\n", pointBelow);
                    System.out.printf("Char Below: %s\n", charBelow);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointBelow;
                Point pointBelow2 = new Point(currentPoint.x, currentPoint.y + 1);
                addPipePointToArrayList2(currentPoint);
                addPipePointToArrayList2(pointBelow2);
            } else if (charLeft == '-' || charLeft == 'F' || charLeft == 'L') {
                if (DEBUGGING) {
                    System.out.printf("Point Left: %s\n", pointLeft);
                    System.out.printf("Char Left: %s\n", charLeft);
                }

                steps++;
                previousPoint = currentPoint;
                currentPoint = pointLeft;
                Point pointLeft2 = new Point(currentPoint.x - 1, currentPoint.y);
                addPipePointToArrayList2(currentPoint);
                addPipePointToArrayList2(pointLeft2);
            }
        }

        Point pointBelow2 = new Point(currentPoint.x, currentPoint.y + 1);
        addPipePointToArrayList2(pointBelow2);

        while (isSearching) {
            if (doubledGrid.get(currentPoint) == 'S'
                    && !Objects.equals(previousPoint.toString(), StartPoint.toString())) {
                if (DEBUGGING) {
                    System.out.print("Found Start Point\n");
                    System.out.printf("Steps: %s\n", steps);
                }
                isSearching = false;
                break;
            }

            Point pointAbove = new Point(currentPoint.x, currentPoint.y - 2);
            Point pointAbove2 = new Point(currentPoint.x, currentPoint.y - 1);
            Point pointBelow = new Point(currentPoint.x, currentPoint.y + 2);
            pointBelow2 = new Point(currentPoint.x, currentPoint.y + 1);
            Point pointLeft = new Point(currentPoint.x - 2, currentPoint.y);
            Point pointLeft2 = new Point(currentPoint.x - 1, currentPoint.y);
            Point pointRight = new Point(currentPoint.x + 2, currentPoint.y);
            Point pointRight2 = new Point(currentPoint.x + 1, currentPoint.y);

            char currentChar = doubledGrid.get(currentPoint);

            if (currentChar == '|') {
                char charAbove = doubledGrid.get(pointAbove);
                char charBelow = doubledGrid.get(pointBelow);

                if (Objects.equals(pointAbove.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Below: %s\n", pointBelow);
                        System.out.printf("Char Below: %s\n", charBelow);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointBelow;
                    addPipePointToArrayList2(pointBelow2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Above: %s\n", pointAbove);
                        System.out.printf("Char Above: %s\n", charAbove);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointAbove;
                    addPipePointToArrayList2(pointAbove2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                }
            }

            if (currentChar == '-') {
                char charLeft = doubledGrid.get(pointLeft);
                char charRight = doubledGrid.get(pointRight);

                if (Objects.equals(pointLeft.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Right: %s\n", pointRight);
                        System.out.printf("Char Right: %s\n", charRight);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointRight;
                    addPipePointToArrayList2(pointRight2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Left: %s\n", pointLeft);
                        System.out.printf("Char Left: %s\n", charLeft);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointLeft;
                    addPipePointToArrayList2(pointLeft2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                }
            }

            if (currentChar == '7') {
                char charLeft = doubledGrid.get(pointLeft);
                char charBelow = doubledGrid.get(pointBelow);

                if (Objects.equals(pointLeft.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Below: %s\n", pointBelow);
                        System.out.printf("Char Below: %s\n", charBelow);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointBelow;
                    addPipePointToArrayList2(pointBelow2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Left: %s\n", pointLeft);
                        System.out.printf("Char Left: %s\n", charLeft);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointLeft;
                    addPipePointToArrayList2(pointLeft2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                }
            }

            if (currentChar == 'J') {
                char charLeft = doubledGrid.get(pointLeft);
                char charAbove = doubledGrid.get(pointAbove);

                if (Objects.equals(pointLeft.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Above: %s\n", pointAbove);
                        System.out.printf("Char Above: %s\n", charAbove);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointAbove;
                    addPipePointToArrayList2(pointAbove2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Left: %s\n", pointLeft);
                        System.out.printf("Char Left: %s\n", charLeft);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointLeft;
                    addPipePointToArrayList2(pointLeft2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                }
            }

            if (currentChar == 'L') {
                char charRight = doubledGrid.get(pointRight);
                char charAbove = doubledGrid.get(pointAbove);

                if (Objects.equals(pointRight.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Above: %s\n", pointAbove);
                        System.out.printf("Char Above: %s\n", charAbove);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointAbove;
                    addPipePointToArrayList2(pointAbove2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Right: %s\n", pointRight);
                        System.out.printf("Char Right: %s\n", charRight);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointRight;
                    addPipePointToArrayList2(pointRight2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                }
            }

            if (currentChar == 'F') {
                char charRight = doubledGrid.get(pointRight);
                char charBelow = doubledGrid.get(pointBelow);

                if (Objects.equals(pointRight.toString(), previousPoint.toString())) {
                    if (DEBUGGING) {
                        System.out.printf("Point Below: %s\n", pointBelow);
                        System.out.printf("Char Below: %s\n", charBelow);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointBelow;
                    addPipePointToArrayList2(pointBelow2);
                    addPipePointToArrayList2(currentPoint);

                    continue;
                } else {
                    if (DEBUGGING) {
                        System.out.printf("Point Right: %s\n", pointRight);
                        System.out.printf("Char Right: %s\n", charRight);
                    }

                    steps++;
                    previousPoint = currentPoint;
                    currentPoint = pointRight;
                    addPipePointToArrayList2(pointRight2);
                    addPipePointToArrayList2(currentPoint);
                }
            }
        }
    }

    private void addPipePointToArrayList(Point point) {
        //    if (isPartTwo) {
        if (DEBUGGING) {
            System.out.printf("Adding Point: %s to ArrayList\n", point);
        }

        pipesPathPoints.add(point);
        //    }
    }

    private void addPipePointToArrayList2(Point point) {
        pipesPathPoints2.add(point);
    }

    private void drawMap() {
        for (int y = 0; y < char2dArrayList.size(); y++) {
            for (int x = 0; x < char2dArrayList.getFirst().length; x++) {
                Point currentPoint = new Point(x, y);
                Character currentCharacter = grid.get(currentPoint);

                if (isPartOfPath(currentPoint)) {
                    if (currentCharacter == 'S') {
                        System.out.print('S');
                    } else if (currentCharacter == 'F') {
                        System.out.print('╔');
                    } else if (currentCharacter == '7') {
                        System.out.print('╗');
                    } else if (currentCharacter == 'J') {
                        System.out.print('╝');
                    } else if (currentCharacter == 'L') {
                        System.out.print('╚');
                    } else if (currentCharacter == '|') {
                        System.out.print('║');
                    } else if (currentCharacter == '-') {
                        System.out.print('═');
                    } else if (currentCharacter == '.') {
                        System.out.print(' ');
                    }
                } else {
                    System.out.print('.');
                }
            }

            System.out.println();
        }
    }

    private void drawMap3() {
        for (int y = 0; y < char2dArrayList.size() * 2; y++) {
            for (int x = 0; x < char2dArrayList.getFirst().length * 2; x++) {
                Point currentPoint = new Point(x, y);
                Character currentCharacter = doubledGridFilledIn.get(currentPoint);

                if (currentPoint.x == 148 && currentPoint.y == 165) {
                    System.out.print('&');
                    continue;
                }

                if (isPartOfPath2(currentPoint)) {
                    if (currentCharacter == 'S') {
                        System.out.print('S');
                    } else if (currentCharacter == 'F') {
                        System.out.print('╔');
                    } else if (currentCharacter == '7') {
                        System.out.print('╗');
                    } else if (currentCharacter == 'J') {
                        System.out.print('╝');
                    } else if (currentCharacter == 'L') {
                        System.out.print('╚');
                    } else if (currentCharacter == '|') {
                        System.out.print('║');
                    } else if (currentCharacter == '-') {
                        System.out.print('═');
                    } else if (currentCharacter == '.') {
                        System.out.print(' ');
                    }
                } else {
                    System.out.print('.');
                }
            }

            System.out.println();
        }
    }

    private boolean isPartOfPath(Point point) {
        return pipesPathPoints.contains(point);
    }

    private boolean isPartOfPath2(Point point) {
        return pipesPathPoints2.contains(point);
    }

    private void reset() {
        char2dArrayList = new ArrayList<>();
        grid = new HashMap<>();
        doubledGrid = new HashMap<>();
        doubledGridFilledIn = new HashMap<>();
        pipesPathPoints = new ArrayList<>();
        pipesPathPoints2 = new ArrayList<>();
    }
}
