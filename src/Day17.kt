import kotlin.math.pow

object Day17 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day17_test")
        val testInput2 = readInput("Day17_test2")
        val input = readInput("Day17")

        println("part 1 test")
        printTimedResult(expectedValue = "4,6,3,5,6,3,5,2,1,0") {
            part1(testInput)
        }

        println("part 1")
        printTimedResult(expectedValue = "2,1,0,1,7,2,5,0,3") {
            part1(input)
        }

        println("part 2 test")
        printTimedResult(expectedValue = 117440) {
            part2(testInput2)
        }

        println("part 2")
        printTimedResult(expectedValue = 267265166222235) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Program {
        return input.split("\n\n", limit = 2).let { (registersLines, instructionsLine) ->
            val (A, B, C) = registersLines.split("\n", limit = 3).map { it.drop("Register X: ".length).toLong() }
            val instructions = instructionsLine.removePrefix("Program: ").split(",").map { it.toLong() }

            Program(A, B, C, instructions)
        }
    }

    private data class Program(
        val registerA: Long,
        val registerB: Long,
        val registerC: Long,
        val instructions: List<Long>,
        val programCounter: Int = 0,
    ) {
        val lastValidProgramCounter: Int get() = instructions.lastIndex - 1

        fun execute(onPrint: (Long) -> Unit): Program? {
            if(programCounter > lastValidProgramCounter) return null

            val operator: Long = instructions[programCounter]
            val operand: Long = instructions[programCounter + 1]

            var newState = this

            when(operator) {
                0L /*adv*/ -> {
                    newState = newState.copy(
                        registerA = registerA / (2 pow operand.asCombo()),
                        programCounter = programCounter + 2,
                    )
                }
                1L /*bxl*/ -> {
                    newState = newState.copy(
                        registerB = registerB xor operand,
                        programCounter = programCounter + 2,
                    )
                }
                2L /*bst*/ -> {
                    newState = newState.copy(
                        registerB = operand.asCombo() % 8,
                        programCounter = programCounter + 2,
                    )
                }
                3L /*jnz*/ -> {
                    newState = newState.copy(
                        programCounter = if(registerA == 0L) {
                            programCounter + 2
                        } else {
                            operand.toInt()
                        },
                    )
                }
                4L /*bxc*/ -> {
                    newState = newState.copy(
                        registerB = registerB xor registerC,
                        programCounter = programCounter + 2,
                    )
                }
                5L /*out*/ -> {
                    onPrint(operand.asCombo() % 8)
                    newState = newState.copy(
                        programCounter = programCounter + 2,
                    )
                }
                6L /*bdv*/ -> {
                    newState = newState.copy(
                        registerB = registerA / (2 pow operand.asCombo()),
                        programCounter = programCounter + 2,
                    )
                }
                7L /*cdv*/ -> {
                    newState = newState.copy(
                        registerC = registerA / (2 pow operand.asCombo()),
                        programCounter = programCounter + 2,
                    )
                }
                else -> return null
            }

            return newState
        }

        private infix fun Int.pow(other: Long): Long {
            return this.toDouble().pow(other.toDouble()).toLong()
        }

        private fun Long.asCombo(): Long {
            return when(this) {
                in 0L..3L -> this
                4L -> registerA
                5L -> registerB
                6L -> registerC
                else -> throw RuntimeException()
            }
        }
    }

    private fun Program.run(): List<Long> {
        val output = mutableListOf<Long>()

        var program: Program? = this
        while(program != null) {
            program = program.execute(onPrint = output::add)
        }

        return output
    }

    private fun part1(input: String): String {
        return parseInput(input).run().joinToString(separator = ",", prefix = "", postfix = "")
    }

    private infix fun Int.pow(other: Int): Long = (this.toDouble()).pow(other.toDouble()).toLong()

    private fun List<Long>.fromBase8(): Long = this.mapIndexed { index, value -> (8 pow (this.size - index - 1)) * value }.sum()

    private fun isValidBranch(program: Program, expected: List<Long>, digits: List<Long>, digitsToCheck: Int): Boolean {
        val partialResultTruncated = digits.fromBase8()
            .let { program.copy(registerA = it) }
            .run()
            .takeLast(digitsToCheck)

        val expectedTruncated = expected.takeLast(digitsToCheck)

        return partialResultTruncated == expectedTruncated
    }

    private fun searchBase8(program: Program, expected: List<Long>, currentDigits: List<Long> = emptyList(), remainingDigits: Int = expected.size): Long? {
        if(remainingDigits == 0) {
            return currentDigits.fromBase8().takeIf { program.copy(registerA = it).run() == expected }
        }

        val padding = (0..(expected.size - (currentDigits.size - 1))).map { 0L }

        return (0L ..< 8L)
            .map { digit -> currentDigits + digit }
            .filter { updatedDigits -> isValidBranch(program, expected, updatedDigits + padding, updatedDigits.size) }
            .mapNotNull { updatedDigits -> searchBase8(program, expected, updatedDigits, remainingDigits - 1) }
            .minOrNull()
    }

    private fun part2(input: String): Long {
        val originalProgram = parseInput(input)

        return searchBase8(program = originalProgram, expected = originalProgram.instructions) ?: throw RuntimeException()
    }
}