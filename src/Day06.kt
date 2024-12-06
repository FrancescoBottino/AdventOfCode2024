import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(DelicateCoroutinesApi::class)
object Day06 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day06_test")
        val input = readInput("Day06")

        println("Part 1 test")
        printTimedResult(41) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(4758) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(6) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(1670) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Map {
        val grid = input.lines().map { line -> line.toList() }
        val size = Size(grid.size, grid[0].size)
        val obstacles = mutableSetOf<Position>()
        lateinit var guardPosition: Position
        lateinit var guardDirection: Direction

        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, char ->
                val position = Position(rowIndex, colIndex)

                when (char) {
                    '#' -> obstacles.add(position)
                    '^' -> {
                        guardPosition = position
                        guardDirection = Direction.UP
                    }
                    '>' -> {
                        guardPosition = position
                        guardDirection = Direction.RIGHT
                    }
                    'v' -> {
                        guardPosition = position
                        guardDirection = Direction.DOWN
                    }
                    '<' -> {
                        guardPosition = position
                        guardDirection = Direction.LEFT
                    }
                }
            }
        }

        return Map(
            obstacles = obstacles,
            guardInitialPosition = guardPosition,
            guardInitialDirection = guardDirection,
            size = size,
        )
    }

    private data class Map(
        val obstacles: Set<Position>,
        val guardInitialPosition: Position,
        val guardInitialDirection: Direction,
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

    private fun getGuardPath(map: Map): Set<Position> {
        val guardTraversedPositions = mutableSetOf(map.guardInitialPosition)
        var currentGuardPosition: Position = map.guardInitialPosition
        var currentGuardDirection: Direction = map.guardInitialDirection

        do {
            var nextGuardPosition = currentGuardPosition + currentGuardDirection

            while(nextGuardPosition in map.obstacles) {
                currentGuardDirection = currentGuardDirection.next()
                nextGuardPosition = currentGuardPosition + currentGuardDirection
            }

            val isValid = nextGuardPosition.isValid(map.size)

            if(isValid)
                guardTraversedPositions.add(nextGuardPosition)

            currentGuardPosition = nextGuardPosition
        } while(isValid)

        return guardTraversedPositions
    }

    private fun part1(input: String): Int {
        val map = parseInput(input)
        return getGuardPath(map).size
    }

    private fun isLoop(map: Map, newObstruction: Position): Boolean {
        val guardTraversedPositions = mutableListOf(map.guardInitialPosition to map.guardInitialDirection)
        var currentGuardPosition: Position = map.guardInitialPosition
        var currentGuardDirection: Direction = map.guardInitialDirection

        do {
            var nextGuardPosition = currentGuardPosition + currentGuardDirection

            while(nextGuardPosition in (map.obstacles + newObstruction)) {
                currentGuardDirection = currentGuardDirection.next()
                nextGuardPosition = currentGuardPosition + currentGuardDirection
            }

            val isValid = nextGuardPosition.isValid(map.size)

            if(isValid) {
                if((nextGuardPosition to currentGuardDirection) in guardTraversedPositions)
                    return true

                guardTraversedPositions.add(nextGuardPosition to currentGuardDirection)
            }

            currentGuardPosition = nextGuardPosition
        } while(isValid)

        return false
    }

    //Bruteforced, TODO optimize
    private fun part2(input: String): Int {
        val map = parseInput(input)
        val originalPath = getGuardPath(map)

        return runBlocking {
            var count = 0
            val mutex = Mutex()

            coroutineScope {
                val results = originalPath.map { newObstruction ->
                    async(Dispatchers.Default) {
                        if (isLoop(map, newObstruction)) {
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
