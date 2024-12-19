object Day19 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day19_test")
        val input = readInput("Day19")

        println("part 1 test")
        printTimedResult(expectedValue = 6) {
            part1(testInput)
        }

        println("part 1")
        printTimedResult(expectedValue = 340) {
            part1(input)
        }

        println("part 2 test")
        printTimedResult(expectedValue = 16) {
            part2(testInput)
        }

        println("part 2")
        printTimedResult(expectedValue = 717561822679428) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Input {
        return input.split("\n\n", limit = 2).let { (availablePatternsLine, desiredPatternsLines) ->
           Input(
               availablePatterns = availablePatternsLine.split(", "),
               desiredPatterns = desiredPatternsLines.split("\n"),
           )
        }
    }

    private data class Input(
        val availablePatterns: List<String>,
        val desiredPatterns: List<String>,
    )

    private fun String.isPossible(availablePatterns: List<String>): Boolean {
        if(this.isEmpty()) return true

        val usablePatterns = availablePatterns.filter { this.startsWith(it) }

        if(usablePatterns.isEmpty()) return false

        return usablePatterns.any { pattern ->
            this.removePrefix(pattern).isPossible(availablePatterns)
        }
    }

    private fun part1(input: String): Int {
        val (availablePatterns, desiredPatterns) = parseInput(input)

        return desiredPatterns.count { it.isPossible(availablePatterns) }
    }

    private fun countPossibilitiesWithMemoization(desiredPattern: String, availablePatterns: List<String>, memory: MutableMap<String, Long>): Long {
        val cachedValue = memory[desiredPattern]
        if(cachedValue != null)
            return cachedValue

        val usablePatterns = availablePatterns.filter { desiredPattern.startsWith(it) }

        if(usablePatterns.isEmpty()) {
            memory[desiredPattern] = 0
            return 0
        }

        return usablePatterns.sumOf { pattern ->
            desiredPattern.removePrefix(pattern)
                .takeIf { it.isNotEmpty() }
                ?.let { countPossibilitiesWithMemoization(it, availablePatterns, memory) }
                ?: 1
        }.also { memory[desiredPattern] = it }
    }

    private fun part2(input: String): Long {
        val (availablePatterns, desiredPatterns) = parseInput(input)
        val memo = mutableMapOf<String, Long>()

        return desiredPatterns.sumOf { desiredPattern ->
            countPossibilitiesWithMemoization(desiredPattern, availablePatterns, memo)
        }
    }
}