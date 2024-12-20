import kotlin.math.abs

object Day20 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day20_test")
        val input = readInput("Day20")

        println("part 1 test")
        printTimedResult(expectedValue = 44) {
            part1(testInput, minimumTimeSaved = 1)
        }

        println("part 1")
        printTimedResult(expectedValue = 1378) {
            part1(input, minimumTimeSaved = 100)
        }

        println("part 2 test")
        printTimedResult(expectedValue = null) {
            part2(testInput)
        }

        println("part 2")
        printTimedResult(expectedValue = null) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Race {
        val rows = input.lines()
        val size = Size2D(rows.size, rows[0].length)
        lateinit var start: Position2D
        lateinit var end: Position2D

        val map = rows.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, char ->
                val position = Position2D(rowIndex, colIndex)

                when(char) {
                    'S' -> start = position
                    'E' -> end = position
                }

                position to char
            }
        }.flatten().toMap()

        return Race(
            map = map,
            size = size,
            start = start,
            end = end
        )
    }

    private data class Race(
        val map: Map<Position2D, Char>,
        val size : Size2D,
        val start: Position2D,
        val end: Position2D,
    )

    private data class Position2D(val rowIndex: Int, val colIndex: Int)

    private sealed class Direction2D private constructor (val rowOffset: Int, val colOffset: Int) {
        data object UP: Direction2D(-1, 0)
        data object RIGHT: Direction2D(0, +1)
        data object DOWN: Direction2D(+1, 0)
        data object LEFT: Direction2D(0, -1)

        companion object {
            val all get() = listOf(UP, RIGHT, DOWN, LEFT)
        }
    }

    private data class Size2D(val rows: Int, val cols: Int) {
        val rowIndexes get() = 0 until rows
        val colIndexes get() = 0 until cols
    }

    private operator fun Position2D.plus(other: Direction2D): Position2D = Position2D(
        this.rowIndex + other.rowOffset,
        this.colIndex + other.colOffset
    )

    private fun Position2D.isValid(size: Size2D): Boolean {
        return rowIndex in size.rowIndexes && colIndex in size.colIndexes
    }

    private fun aStar_getPath(size: Size2D, obstacles: Set<Position2D>, start: Position2D, end: Position2D): List<Pair<Position2D, Int>> {
        fun Position2D.getNeighbours(): List<Position2D> {
            return Direction2D.all.map { this + it }.filter { it.isValid(size) && it !in obstacles }
        }

        fun heuristicDistance(start: Position2D, end: Position2D): Int {
            val dx = abs(start.colIndex - end.colIndex)
            val dy = abs(start.rowIndex - end.rowIndex)
            return (dx + dy) + (-2) * minOf(dx, dy)
        }

        /**
         * Use the cameFrom values to Backtrack to the start position to generate the path
         */
        fun generatePath(end: Position2D, cameFrom: Map<Position2D, Position2D>, distanceToEnd: Int): List<Pair<Position2D, Int>> {
            val path = mutableListOf(end to distanceToEnd)
            var currentDistance = distanceToEnd
            var currentPosition = end
            while (cameFrom.containsKey(currentPosition)) {
                currentPosition = cameFrom.getValue(currentPosition)
                currentDistance --
                path.add(0, (currentPosition to currentDistance))
            }
            return path.toList()
        }

        val openVertices = mutableSetOf(start)
        val closedVertices = mutableSetOf<Position2D>()
        val costFromStart = mutableMapOf(start to 0)
        val estimatedTotalCost = mutableMapOf(start to heuristicDistance(start, end))

        val cameFrom = mutableMapOf<Position2D, Position2D>()

        while (openVertices.size > 0) {
            val currentPos = openVertices.minBy { estimatedTotalCost.getValue(it) }

            if (currentPos == end) {
                val distanceToEnd = estimatedTotalCost.getValue(end)
                val path = generatePath(currentPos, cameFrom, distanceToEnd)
                return path
            }

            openVertices.remove(currentPos)
            closedVertices.add(currentPos)

            currentPos.getNeighbours()
                .filter { it !in closedVertices }
                .forEach { neighbour ->
                    val score = costFromStart.getValue(currentPos) + 1
                    if (score < costFromStart.getOrDefault(neighbour, Int.MAX_VALUE)) {
                        if (!openVertices.contains(neighbour)) {
                            openVertices.add(neighbour)
                        }
                        cameFrom[neighbour] = currentPos
                        costFromStart[neighbour] = score
                        estimatedTotalCost[neighbour] = score + heuristicDistance(neighbour, end)
                    }
                }

        }

        throw RuntimeException()
    }

    private fun findShortcuts(
        path: List<Pair<Position2D, Int>>,
        map: Map<Position2D, Char>,
    ): Map<Position2D, Int> {
        val distancesFromStart: Map<Position2D, Int> = path.toMap()

        val shortcuts = mutableMapOf<Position2D, Int>()

        path.forEach stepLoop@ { (stepPosition, timeTaken) ->
            Direction2D.all.forEach directionLoop@ { direction ->
                val wallToRemove = stepPosition + direction

                if(map[wallToRemove] != '#') {
                    return@directionLoop
                }

                val positionOnPathReached = wallToRemove + direction

                if(!distancesFromStart.keys.contains(positionOnPathReached)) {
                    return@directionLoop
                }

                val distanceAtCheatPosition = distancesFromStart[positionOnPathReached]!!

                if(distanceAtCheatPosition < timeTaken) {
                    return@directionLoop
                }

                val timeSaved = distancesFromStart[positionOnPathReached]!! - timeTaken - 2

                shortcuts[wallToRemove] = timeSaved
            }
        }

        return shortcuts
    }

    private fun part1(input: String, minimumTimeSaved: Int): Int {
        val race = parseInput(input)

        val path = aStar_getPath(
            size = race.size,
            obstacles = race.map.filter { it.value == '#' }.keys,
            start = race.start,
            end = race.end
        )

        val shortcuts = findShortcuts(
            path = path,
            map = race.map
        )

        return shortcuts.count { it.value >= minimumTimeSaved }
    }

    private fun part2(input: String): Long {
        TODO()
    }
}