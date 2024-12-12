import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object Day06 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day06_test")
        val input = readInput("Day06")

        println("Part 1 test")
        printTimedResult(expectedValue = 41) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 4758) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(expectedValue = 6) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(expectedValue = 1670) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Map {
        val grid = input.lines().map { line -> line.toList() }
        val size = Size(grid.size, grid[0].size)
        val obstacles = mutableSetOf<Position>()
        lateinit var guardState: GuardState

        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, char ->
                val position = Position(rowIndex, colIndex)

                when (char) {
                    '#' -> obstacles.add(position)
                    '^' -> guardState = GuardState(position, Direction.UP)
                    '>' -> guardState = GuardState(position, Direction.RIGHT)
                    'v' -> guardState = GuardState(position, Direction.DOWN)
                    '<' -> guardState = GuardState(position, Direction.LEFT)
                }
            }
        }

        return Map(
            obstacles = obstacles,
            initialGuardState = guardState,
            size = size,
        )
    }

    private data class Map(
        val obstacles: Set<Position>,
        val initialGuardState: GuardState,
        val size: Size,
    )

    private data class Position(val rowIndex: Int, val colIndex: Int)

    private data class Size(val rows: Int, val cols: Int) {
        val rowIndexes get() = 0 until rows
        val colIndexes get() = 0 until cols
    }

    private fun Position.isValid(size: Size): Boolean {
        return rowIndex in size.rowIndexes && colIndex in size.colIndexes
    }

    private operator fun Position.plus(direction: Direction): Position {
        return Position(rowIndex + direction.rowOffset, colIndex + direction.colOffset)
    }

    private sealed class Direction(val rowOffset: Int, val colOffset: Int) {
        abstract fun next(): Direction

        data object UP: Direction(-1, 0) {
            override fun next(): Direction = RIGHT
        }
        data object RIGHT: Direction(0, +1) {
            override fun next(): Direction = DOWN
        }
        data object DOWN: Direction(+1, 0) {
            override fun next(): Direction = LEFT
        }
        data object LEFT: Direction(0, -1) {
            override fun next(): Direction = UP
        }
    }

    private data class GuardState(
        val position: Position,
        val direction: Direction,
    )

    private fun GuardState.move(): GuardState {
        return GuardState(position + direction, direction)
    }

    private fun GuardState.turn(): GuardState {
        return GuardState(position, direction.next())
    }

    private fun getGuardPath(map: Map): List<GuardState> {
        val guardPath = mutableListOf(map.initialGuardState)
        var currentGuardState = map.initialGuardState

        do {
            var nextGuardState = currentGuardState.move()

            while(nextGuardState.position in map.obstacles) {
                currentGuardState = currentGuardState.turn()
                nextGuardState = currentGuardState.move()
            }

            val isValid = nextGuardState.position.isValid(map.size)

            if(isValid) {
                guardPath.add(nextGuardState)
            }
            currentGuardState = nextGuardState

        } while(isValid)

        return guardPath
    }

    private fun part1(input: String): Int {
        val map = parseInput(input)
        return getGuardPath(map).map { it.position }.toSet().size
    }

    private fun isLoop(map: Map, newObstacle: Position, path: List<GuardState>): Boolean {
        val updatedObstacles = map.obstacles + newObstacle
        val guardPath = path.toMutableSet()
        var currentGuardState = path.lastOrNull() ?: map.initialGuardState

        do {
            var nextGuardState = currentGuardState.move()

            while(nextGuardState.position in updatedObstacles) {
                currentGuardState = currentGuardState.turn()
                nextGuardState = currentGuardState.move()
            }

            val isValid = nextGuardState.position.isValid(map.size)

            if(!isValid) {
                return false
            }

            if(nextGuardState in guardPath)
                return true

            guardPath.add(nextGuardState)
            currentGuardState = nextGuardState

        } while(true)
    }

    private fun part2(input: String): Int {
        val map = parseInput(input)
        val originalPath = getGuardPath(map)

        //Get positions to check by taking only the first instance of each but keep track of the index in the original path
        val toCheck = originalPath
            .map { it.position }
            .withIndex()
            .distinctBy { it.value }

        return runBlocking {
            var count = 0
            val mutex = Mutex()

            coroutineScope {
                val results = toCheck.map { (index, position) ->
                    async(Dispatchers.Default) {
                        if (isLoop(map, position, originalPath.subList(0, index))) {
                            mutex.withLock {
                                count++
                            }
                        }
                    }
                }
                results.awaitAll()
            }

            count
        }
    }
}
