fun main() {
    val testInput1 = readInput("Day03_test1")
    val testInput2 = readInput("Day03_test2")
    val input = readInput("Day03")

    println("Part 1 test")
    printTimedResult(161) {
        part1(testInput1)
    }

    println("Part 1")
    printTimedResult(190604937) {
        part1(input)
    }

    println("Part 2 test")
    printTimedResult(48) {
        part2(testInput2)
    }

    println("Part 2")
    printTimedResult {
        part2(input)
    }
}

private fun parseInput(input: String): List<String> {
    return input.lines()
}

private val MUL by lazy { """mul\(\d{1,3},\d{1,3}\)""".toRegex() }
private val DO by lazy { """do\(\)""".toRegex() }
private val DONT by lazy { """don't\(\)""".toRegex() }
private val INSTRUCTIONS by lazy { "$MUL|$DO|$DONT".toRegex() }

private fun String.performMultiplication() = this
    .removePrefix("mul(")
    .removeSuffix(")")
    .split(",", limit = 2)
    .let { it[0].toInt() * it[1].toInt() }

private fun part1(input: String): Int {
    return parseInput(input).sumOf { instructionsLine ->
        MUL.findAll(instructionsLine).sumOf { it.value.performMultiplication() }
    }
}

private fun part2(input: String): Int {
    return parseInput(input).sumOf { instructionsLine ->
        var lastInstructionIndex = 0
        var total = 0
        var mulEnabled = true

        do {
            val match = INSTRUCTIONS.find(input = instructionsLine, startIndex = lastInstructionIndex)
            if(match != null) {
                lastInstructionIndex = match.range.last

                when {
                    match.value.matches(DO) ->
                        mulEnabled = true
                    match.value.matches(DONT) ->
                        mulEnabled = false
                    match.value.matches(MUL) ->
                        if(mulEnabled)
                            total += match.value.performMultiplication()

                    else -> throw RuntimeException()
                }
            }
        } while (match != null)

        total
    }
}
