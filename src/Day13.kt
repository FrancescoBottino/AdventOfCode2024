object Day13 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day13_test")
        val input = readInput("Day13")

        println("Part 1 test")
        printTimedResult(expectedValue = 480) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 36954) {
            part1(input)
        }

        println("Part 2")
        printTimedResult(expectedValue = 79352015273424) {
            part2(input)
        }
    }

    private fun parseInput(input: String): List<ClawMachine> {
        return input.split("\n\n").map {
            val (buttonA, buttonB, prize) = it.split("\n", limit = 3)
            ClawMachine(
                buttonA = buttonA
                    .removePrefix("Button A: ")
                    .split(", ", limit = 2)
                    .let { (x, y) ->
                        Point2D(
                            x = x.removePrefix("X+").toLong(),
                            y = y.removePrefix("Y+").toLong(),
                        )
                    },
                buttonB = buttonB
                    .removePrefix("Button B: ")
                    .split(", ", limit = 2)
                    .let { (x, y) ->
                        Point2D(
                            x = x.removePrefix("X+").toLong(),
                            y = y.removePrefix("Y+").toLong(),
                        )
                    },

                prize = prize
                    .removePrefix("Prize: ")
                    .split(", ", limit = 2)
                    .let { (x, y) ->
                        Point2D(
                            x = x.removePrefix("X=").toLong(),
                            y = y.removePrefix("Y=").toLong(),
                        )
                    },
            )
        }
    }

    private data class ClawMachine(
        val buttonA: Point2D,
        val buttonB: Point2D,
        val prize: Point2D,
    )

    private data class Point2D(val x: Long, val y: Long) {
        operator fun times(amount: Long): Point2D = Point2D(x*amount, y*amount)
        operator fun plus(other: Point2D): Point2D = Point2D(this.x + other.x, this.y + other.y)
    }

    private const val A_COST = 3
    private const val B_COST = 1

    private fun getScore(claw: ClawMachine, buttonPressesRange: LongRange): Long? {
        val (c1, c2) = claw.prize
        val (a1, a2) = claw.buttonA
        val (b1, b2) = claw.buttonB

        val det = (a1*b2) - (a2*b1)

        if(det == 0L) return null

        val aButtonPresses = ((c1*b2) - (c2*b1)) / det
        val bButtonPresses = ((c2*a1) - (c1*a2)) / det

        if(aButtonPresses !in buttonPressesRange || bButtonPresses !in buttonPressesRange) return null
        if((claw.buttonA * aButtonPresses) + (claw.buttonB * bButtonPresses) != claw.prize) return null

        return (aButtonPresses * A_COST) + (bButtonPresses * B_COST)
    }

    private fun part1(input: String): Long {
        return parseInput(input).sumOf { claw -> getScore(claw, 0L..100L) ?: 0L }
    }

    private fun part2(input: String): Long {
        return parseInput(input).sumOf { claw ->
            getScore(
                claw = claw.copy(prize = claw.prize + Point2D(10000000000000L,10000000000000L)),
                buttonPressesRange = 0L..Long.MAX_VALUE
            ) ?: 0L
        }
    }
}
