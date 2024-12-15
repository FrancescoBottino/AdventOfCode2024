object Day15 {
    @JvmStatic
    fun main(args: Array<String>) {
        val smallTestInput = readInput("Day15_test_small")
        val bigTestInput = readInput("Day15_test_big")
        val input = readInput("Day15")

        println("Part 1 test small")
        printTimedResult(expectedValue = 2028) {
            part1(smallTestInput)
        }

        println("Part 1 test big")
        printTimedResult(expectedValue = 10092) {
            part1(bigTestInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 1476771) {
            part1(input)
        }

        println("part 2 test small")
        printTimedResult(expectedValue = 1751) {
            part2(smallTestInput)
        }

        println("Part 2 test big")
        printTimedResult(expectedValue = 9021) {
            part2(bigTestInput)
        }

        println("Part 2")
        printTimedResult(expectedValue = 1468005) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Warehouse {
        return input.split("\n\n", limit = 2).let { (gridString, directionsLine) ->
            lateinit var robotPosition: Position2D

            val grid = gridString.lines().mapIndexed { rowIndex, row ->
                row.mapIndexed { colIndex, char ->
                    val position = Position2D(rowIndex, colIndex)

                    if(char == '@')
                        robotPosition = position

                    position to char
                }
            }.flatten().toMap()

            val directions = directionsLine.mapNotNull {
                when(it) {
                    '>' -> Direction2D.RIGHT
                    '<' -> Direction2D.LEFT
                    'v' -> Direction2D.DOWN
                    '^' -> Direction2D.UP
                    else -> null
                }
            }

            Warehouse(grid, robotPosition, directions)
        }
    }

    private data class Warehouse(
        val grid: Map<Position2D, Char>,
        val robotPosition: Position2D,
        val robotDirections: List<Direction2D>,
    )

    private sealed class Direction2D private constructor (val rowOffset: Int, val colOffset: Int) {
        data object UP: Direction2D(-1, 0) {
            override fun toString(): String = "^"
        }
        data object RIGHT: Direction2D(0, +1) {
            override fun toString(): String = ">"
        }
        data object DOWN: Direction2D(+1, 0) {
            override fun toString(): String = "v"
        }
        data object LEFT: Direction2D(0, -1) {
            override fun toString(): String = "<"
        }
    }

    private data class Position2D(
        val rowIndex: Int,
        val colIndex: Int
    ) {
        override fun toString(): String = "($rowIndex, $colIndex)"
    }

    private operator fun Position2D.plus(other: Direction2D): Position2D = Position2D(
        this.rowIndex + other.rowOffset,
        this.colIndex + other.colOffset
    )

    private fun Warehouse.simulateMovements(): Warehouse {
        val grid = this.grid.toMutableMap()
        var robotPosition = this.robotPosition

        fun move(position: Position2D, direction: Direction2D): Boolean {
            val targetPosition = position + direction

            when(grid[targetPosition]!!) {
                '#' -> return false
                '.' -> {
                    grid[targetPosition] = grid[position]!!
                    grid[position] = '.'
                    return true
                }
                'O' -> {
                    if(move(targetPosition, direction)) {
                        grid[targetPosition] = grid[position]!!
                        grid[position] = '.'
                        return true
                    } else {
                        return false
                    }
                }

                else -> throw RuntimeException("Unknown char ${grid[targetPosition]} in grid")
            }
        }

        this.robotDirections.forEach { direction ->
            if(move(robotPosition, direction)) {
                robotPosition += direction
            }
        }

        return Warehouse(grid, robotPosition, emptyList())
    }

    private fun Map<Position2D, Char>.getBoxesPositions(): List<Position2D> {
        return entries
            .filter { it.value == 'O' }
            .map { it.key }
    }

    private fun Position2D.gpsCoordinate(): Int {
        return (rowIndex * 100) + colIndex
    }

    private fun part1(input: String): Int {
        return parseInput(input)
            .simulateMovements()
            .grid
            .getBoxesPositions()
            .sumOf { it.gpsCoordinate() }
    }

    private fun Position2D.expand(): Position2D {
        return Position2D(this.rowIndex, this.colIndex * 2)
    }

    private fun Warehouse.expand(): Warehouse {
        return Warehouse(
            grid = buildMap {
                grid.forEach { (position, char) ->
                    val newPosition = position.expand()
                    when(char) {
                        '#' -> {
                            put(newPosition, '#')
                            put(newPosition + Direction2D.RIGHT, '#')
                        }
                        '.' -> {
                            put(newPosition, '.')
                            put(newPosition + Direction2D.RIGHT, '.')
                        }
                        '@' -> {
                            put(newPosition, '@')
                            put(newPosition + Direction2D.RIGHT, '.')
                        }
                        'O' -> {
                            put(newPosition, '[')
                            put(newPosition + Direction2D.RIGHT, ']')
                        }
                        else -> throw RuntimeException("Unknown char ${char}")
                    }
                }
            },
            robotPosition = robotPosition.expand(),
            robotDirections = robotDirections,
        )
    }

    private fun Warehouse.simulateMovementsExpanded(): Warehouse {
        val grid = this.grid.toMutableMap()
        var robotPosition = this.robotPosition

        fun getMovedBoxesIfPossible(position: Position2D, direction: Direction2D): List<Position2D>? {
            val (leftSide, rightSide) = when(grid[position]) {
                '[' -> position to (position + Direction2D.RIGHT)
                ']' -> (position + Direction2D.LEFT) to position
                else -> throw RuntimeException("Not a box at $position: ${grid[position]}")
            }

            check(grid[leftSide] == '[' && grid[rightSide] == ']')

            val leftTarget = leftSide + direction
            val rightTarget = rightSide + direction

            return when(direction) {
                Direction2D.UP, Direction2D.DOWN -> when {
                    grid[leftTarget] == '#' || grid[rightTarget] == '#' -> null
                    grid[leftTarget] == '.' && grid[rightTarget] == '.' -> listOf(leftSide)
                    grid[leftTarget] == '[' && grid[rightTarget] == ']' -> getMovedBoxesIfPossible(leftTarget, direction)?.let { listOf(leftSide) + it }

                    (grid[leftTarget] == '[' && grid[rightTarget] != ']') || (grid[leftTarget] != '[' && grid[rightTarget] == ']') ->
                        throw RuntimeException("Malformed box at ${leftTarget}|${rightTarget}")

                    grid[leftTarget] == ']' || grid[rightTarget] == '[' -> {
                        val moves = mutableListOf(leftSide)

                        if(grid[leftTarget] == ']') {
                            moves += getMovedBoxesIfPossible(leftTarget, direction) ?: return null
                        }

                        if(grid[rightTarget] == '[') {
                            moves += getMovedBoxesIfPossible(rightTarget, direction) ?: return null
                        }

                        moves
                    }

                    else -> throw RuntimeException("Unknown chars ${grid[leftTarget]}${grid[rightTarget]} in grid at ${leftTarget}|${rightTarget}")
                }

                Direction2D.RIGHT -> {
                    when(grid[rightTarget]!!) {
                        '#' -> null
                        '.' -> listOf(leftSide)
                        '[' -> getMovedBoxesIfPossible(rightTarget, direction)?.let { listOf(leftSide) + it }
                        ']' -> throw RuntimeException("Malformed box at ${leftSide}|${rightSide}|${rightTarget}")
                        else -> throw RuntimeException("Unknown char ${grid[rightTarget]} in grid at $rightTarget")
                    }
                }

                Direction2D.LEFT -> {
                    when(grid[leftTarget]!!) {
                        '#' -> null
                        '.' -> listOf(leftSide)
                        ']' -> getMovedBoxesIfPossible(leftTarget, direction)?.let { listOf(leftSide) + it }
                        '[' -> throw RuntimeException("Malformed box at ${leftTarget}|${leftSide}|${rightSide}")
                        else -> throw RuntimeException("Unknown char ${grid[leftTarget]} in grid at $leftTarget")
                    }
                }
            }
        }

        fun moveRobot(direction: Direction2D) {
            when(grid[robotPosition + direction]!!) {
                '#' -> { /*no op*/ }
                '.' -> {
                    grid[robotPosition + direction] = '@'
                    grid[robotPosition] = '.'
                    robotPosition += direction
                }
                '[', ']' -> {
                    val boxesToMove = getMovedBoxesIfPossible(robotPosition + direction, direction) ?: return

                    boxesToMove.reversed().toSet().forEach { left ->
                        val right = left + Direction2D.RIGHT
                        when(direction) {
                            Direction2D.UP, Direction2D.DOWN -> {
                                grid[left + direction] = '['
                                grid[right + direction] = ']'
                                grid[left] = '.'
                                grid[right] = '.'
                            }
                            Direction2D.LEFT -> {
                                grid[left + direction] = '['
                                grid[left] = ']'
                                grid[right] = '.'
                            }
                            Direction2D.RIGHT -> {
                                grid[right + direction] = ']'
                                grid[right] = '['
                                grid[left] = '.'
                            }
                        }
                    }

                    grid[robotPosition + direction] = '@'
                    grid[robotPosition] = '.'
                    robotPosition += direction
                }

                else -> throw RuntimeException("Unknown char ${grid[robotPosition + direction]} in grid at ${robotPosition + direction}")
            }
        }

        this.robotDirections.forEachIndexed { index, direction ->
            //println("$index) moving to $direction")
            moveRobot(direction)
            //grid.debug()
        }

        return Warehouse(grid, robotPosition, emptyList())
    }

    private fun Map<Position2D, Char>.getBoxesPositionsExpanded(): List<Position2D> {
        return entries
            .filter { it.value == '[' }
            .map { it.key }
    }

    private fun Map<Position2D, Char>.debug() {
        val lastRow = keys.maxOf { it.rowIndex }
        val lastCol = keys.maxOf { it.colIndex }

        buildString {
            (0..lastRow).forEach { rowIndex ->
                if(rowIndex != 0) append("\n")
                (0..lastCol).forEach { colIndex ->
                    val position = Position2D(rowIndex, colIndex)
                    append(this@debug[position])
                }
            }
        }.println()
    }

    private fun part2(input: String): Int {
        return parseInput(input)
            //.also { it.grid.debug() }
            .expand()
            //.also { it.grid.debug() }
            .simulateMovementsExpanded()
            .grid
            .getBoxesPositionsExpanded()
            .sumOf { it.gpsCoordinate() }
    }
}
