fun main() {
    val testInput = readInput("Day02_test")
    val input = readInput("Day02")

    println("Part 1 test")
    printTimedResult(2) {
        part1(testInput)
    }

    println("Part 1")
    printTimedResult(321) {
        part1(input)
    }

    println("Part 2 test")
    printTimedResult(4) {
        part2(testInput)
    }

    println("Part 2")
    printTimedResult(386) {
        part2(input)
    }
}

private fun parseInput(input: String): List<List<Int>> {
    return input.lines().map { line -> line.split(" ").map { it.toInt() } }
}

private fun isSafe(levels: List<Int>): Boolean {
    val changes = levels.indices.map { i ->
        if(i == 0) 0 else levels[i] - levels[i - 1]
    }.drop(1)

    return changes.all { it in 1..3 } || changes.all { it in (-3)..(-1) }
}

private fun part1(input: String): Int {
    return parseInput(input).count { levels -> isSafe(levels) }
}

private fun part2(input: String): Int {
    return parseInput(input).count { levels ->
        levels.indices
            .asSequence()
            .map { indexToRemove -> levels.toMutableList().apply { removeAt(indexToRemove) } }
            .any { isSafe(it) }
    }
}
