import kotlin.math.abs

fun main() {
    val testInput = readInput("Day01_test")
    val input = readInput("Day01")

    println("Part 1 test")
    printTimedResult(11) {
        part1(testInput)
    }

    println("Part 1")
    printTimedResult(1258579) {
        part1(input)
    }

    println("Part 2 test")
    printTimedResult(31) {
        part2(testInput)
    }

    println("Part 2")
    printTimedResult(23981443) {
        part2(input)
    }
}

private fun parseInput(input: String): Pair<List<Int>, List<Int>> {
    return input.lines().map { line ->
        val (first, second) = line.split("   ", limit = 2)
        first.toInt() to second.toInt()
    }.unzip()
}

private fun part1(input: String): Int {
    val (list1, list2) = parseInput(input)
    val list1Sorted = list1.sorted()
    val list2Sorted = list2.sorted()
    return list1Sorted.withIndex().sumOf { abs(it.value - list2Sorted[it.index]) }
}

private fun part2(input: String): Int {
    val (list1, list2) = parseInput(input)
    val occurrences = list2.groupBy { it }.mapValues { it.value.size }
    return list1.sumOf { it * occurrences.getOrDefault(it, 0) }
}
