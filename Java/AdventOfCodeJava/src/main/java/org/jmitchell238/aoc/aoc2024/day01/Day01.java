package org.jmitchell238.aoc.aoc2024.day01;

import java.io.FileNotFoundException;

public class Day01 {
    private static final Boolean DEBUGGING = false;
    private static final Boolean VERBOSE = false;

    public static void main(String[] args) throws FileNotFoundException {
        Day00Run();
    }

    public static void Day00Run() throws FileNotFoundException {
        System.out.println("\n--- Day 4: Scratchcards ---\n");

        String input = "src/main/java/org/jmitchell238/aoc/aoc2024/day00/input.txt";
        String inputTest = "src/main/java/org/jmitchell238/aoc/aoc2024/day00/input_test_part1.txt";

        int partOneAnswer = part1(inputTest);
        System.out.printf("Part 1: Answer: %d%n", partOneAnswer);

        int partTwoAnswer = part2(inputTest);
        System.out.printf("Part 2: Answer: %d%n", partTwoAnswer);
    }

    public static int part1(String input) {
        return -1;
    }

    public static int part2(String input) {
        return -1;
    }
}
