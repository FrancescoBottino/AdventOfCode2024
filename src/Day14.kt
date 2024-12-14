object Day14 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day14_test")
        val input = readInput("Day14")

        println("Part 1 test")
        printTimedResult(expectedValue = 12) {
            part1(testInput, Size2D(rowCount = 7, colCount = 11))
        }

        println("Part 1")
        printTimedResult(expectedValue = 218295000) {
            part1(input, Size2D(rowCount = 103, colCount = 101))
        }

        println("Part 2")
        printTimedResult(expectedValue = 6870) {
            part2(input, Size2D(rowCount = 103, colCount = 101))
        }
    }

    private fun parseInput(input: String): List<Robot> {
        return input.split("\n").map { robotLine ->
            val (positionLine, velocityLine) = robotLine.split(" ", limit = 2)
            Robot(
                position = positionLine.removePrefix("p=").split(",").map { it.toInt() }.let { (x, y) -> Position2D(rowIndex = y, colIndex = x) },
                velocity = velocityLine.removePrefix("v=").split(",").map { it.toInt() }.let { (x, y) -> Direction2D(rowOffset = y, colOffset = x) },
            )
        }
    }

    private data class Robot(
        val position: Position2D,
        val velocity: Direction2D,
    )

    private data class Direction2D(
        val rowOffset: Int,
        val colOffset: Int
    )

    private data class Position2D(
        val rowIndex: Int,
        val colIndex: Int
    )

    private data class Size2D(
        val rowCount: Int,
        val colCount: Int,
    )

    private operator fun Direction2D.times(amount: Int) = Direction2D(rowOffset * amount, colOffset * amount)

    private operator fun Position2D.plus(other: Direction2D): Position2D = Position2D(
        this.rowIndex + other.rowOffset,
        this.colIndex + other.colOffset
    )

    private fun Position2D.wrapAround(size: Size2D): Position2D {
        return Position2D(
            rowIndex = Math.floorMod(this.rowIndex, size.rowCount),
            colIndex = Math.floorMod(this.colIndex, size.colCount),
        )
    }

    private fun Robot.quadrant(size: Size2D): Int? {
        with(position) {
            val middleRow = size.rowCount / 2
            val middleCol = size.colCount / 2

            return when {
                (rowIndex == middleRow) || (colIndex == middleCol) -> null
                (rowIndex < middleRow) && (colIndex < middleCol) -> 1
                (rowIndex < middleRow) && (colIndex > middleCol) -> 2
                (rowIndex > middleRow) && (colIndex < middleCol) -> 3
                (rowIndex > middleRow) && (colIndex > middleCol) -> 4
                else -> throw RuntimeException()
            }
        }
    }

    private fun List<Robot>.simulateTurns(turns: Int, size: Size2D): List<Robot> {
        return this.map { robot ->
            robot.copy(position = (robot.position + (robot.velocity * turns)).wrapAround(size))
        }
    }

    private fun List<Robot>.safetyScore(size: Size2D): Long {
        return this
            .mapNotNull { it.quadrant(size) }
            .groupingBy { it }
            .eachCount()
            .values
            .fold(1L) { acc, amount -> acc * amount.toLong() }
    }

    private fun part1(input: String, size: Size2D): Long {
        return parseInput(input)
            .simulateTurns(100, size)
            .safetyScore(size)
    }

    private fun debug(list: List<Robot>, size: Size2D) {
        return buildString {
            (0..<size.rowCount).forEach { rowIndex ->
                if(rowIndex != 0) append("\n")
                (0..<size.colCount).forEach { colIndex ->
                    val position = Position2D(rowIndex = rowIndex, colIndex = colIndex)

                    append(list.count { it.position == position }.takeIf { it != 0 } ?: ".")
                }
            }
        }.println()
    }

    private data class Simulation(
        val robots: List<Robot>,
        val turns: Int,
        val safetyScore: Long,
    )

    private fun part2(input: String, size: Size2D): Int {
        val robots = parseInput(input)

        return (1..(size.rowCount * size.colCount))
            .map { turns ->
                robots.simulateTurns(turns, size)
                    .let { state ->
                        Simulation(
                            state,
                            turns,
                            state.safetyScore(size)
                        )
                    }
            }
            .minBy { it.safetyScore }
            .also { debug(it.robots, size) }
            .turns
    }
}
