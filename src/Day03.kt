object Day03 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput1 = readInput("Day03_test1")
        val testInput2 = readInput("Day03_test2")
        val input = readInput("Day03")

        println("Part 1 test")
        printTimedResult(expectedValue = 161) {
            part1(testInput1)
        }

        println("Part 1")
        printTimedResult(expectedValue = 190604937) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(expectedValue = 48) {
            part2(testInput2)
        }

        println("Part 2")
        printTimedResult(expectedValue = 82857512) {
            part2(input)
        }
    }

    private val MUL by lazy { """mul\((\d{1,3}),(\d{1,3})\)""".toRegex() }
    private val DO by lazy { """do\(\)""".toRegex() }
    private val DONT by lazy { """don't\(\)""".toRegex() }
    private val INSTRUCTIONS by lazy { "$MUL|$DO|$DONT".toRegex() }

    private fun MatchResult.performMultiplication(): Int {
        val (a, b) = destructured
        return a.toInt() * b.toInt()
    }

    private fun part1(input: String): Int {
        return MUL.findAll(input).sumOf { it.performMultiplication() }
    }

    private fun part2(input: String): Int {
        var total = 0
        var mulEnabled = true

        INSTRUCTIONS.findAll(input = input).forEach { match ->
            when {
                match.value.matches(DO) ->
                    mulEnabled = true
                match.value.matches(DONT) ->
                    mulEnabled = false
                mulEnabled && match.value.matches(MUL) ->
                    total += match.performMultiplication()
            }
        }

        return total
    }
}
