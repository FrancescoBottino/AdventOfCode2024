import kotlinx.coroutines.*

object Day07 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day07_test")
        val input = readInput("Day07")

        println("Part 1 test")
        printTimedResult(3749) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(4555081946288) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(11387) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(227921760109726) {
            part2(input)
        }
    }

    private fun parseInput(input: String): List<Equation> {
        return input.lines().map { line ->
            val (testValueString, operandsString) = line.trim().split(": ", limit = 2)
            val operands = operandsString.trim().split(" ").map { it.toLong() }

            Equation(
                testValue = testValueString.toLong(),
                operands = operands
            )
        }
    }

    private data class Equation(
        val testValue: Long,
        val operands: List<Long>,
    )

    private fun performCalculation(equation: Equation, chosenOperators: List<Operator>): Long {
        var total: Long = equation.operands.first()

        chosenOperators.forEachIndexed { index, operator ->
            total = operator.execute(total, equation.operands[index+1])
        }

        return total
    }

    private sealed interface Operator {
        fun execute(a: Long, b: Long): Long

        data object Addition: Operator {
            override fun execute(a: Long, b: Long): Long = a+b

        }

        data object Multiplication: Operator {
            override fun execute(a: Long, b: Long): Long = a*b
        }

        data object Concatenation: Operator {
            override fun execute(a: Long, b: Long): Long = (a.toString()+b.toString()).toLong()
        }
    }

    private fun checkIsValid(equation: Equation, validOperators: List<Operator>, chosenOperators: List<Operator> = emptyList()): Boolean {
        if(chosenOperators.size == equation.operands.size - 1) {
            val result = performCalculation(equation, chosenOperators)
            return equation.testValue == result
        }
        return validOperators.any { checkIsValid(equation, validOperators, chosenOperators + it) }
    }

    private fun part1(input: String): Long {
        val operators = listOf(Operator.Addition, Operator.Multiplication)
        return parseInput(input).filter { equation -> checkIsValid(equation, operators) }.sumOf { it.testValue }
    }

    private fun part2(input: String): Long {
        val operators = listOf(Operator.Addition, Operator.Multiplication, Operator.Concatenation)
        val equation = parseInput(input)

        return runBlocking {
            coroutineScope {
                equation
                    .map { equation ->
                        async(Dispatchers.Default) {
                            if(checkIsValid(equation, operators)) {
                                equation.testValue
                            } else null
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
                    .sum()
            }
        }
    }
}
