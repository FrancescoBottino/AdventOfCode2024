object Day10 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day10_test")
        val input = readInput("Day10")

        println("Part 1 test")
        printTimedResult(36) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(798) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(81) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(1816) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Topography {
        return input.lines()
            .map { line -> line.map { it.digitToInt() } }
            .let { Topography(Size(it.size, it.first().size), it) }
    }

    private data class Topography(
        val size: Size,
        val map: List<List<Int>>
    )

    private data class Position(val rowIndex: Int, val colIndex: Int)

    private data class Size(val rows: Int, val cols: Int) {
        val rowIndexes get() = 0 until rows
        val colIndexes get() = 0 until cols
    }

    private sealed class Direction(val rowOffset: Int, val colOffset: Int) {
        data object UP: Direction(-1, 0)
        data object RIGHT: Direction(0, +1)
        data object DOWN: Direction(+1, 0)
        data object LEFT: Direction(0, -1)

        companion object {
            val all get() = listOf(UP, RIGHT, DOWN, LEFT)
        }
    }

    private operator fun Position.plus(direction: Direction): Position {
        return Position(rowIndex + direction.rowOffset, colIndex + direction.colOffset)
    }

    private fun Position.isValid(size: Size): Boolean {
        return rowIndex in size.rowIndexes && colIndex in size.colIndexes
    }

    private operator fun <T> List<List<T>>.get(position: Position): T {
        return this[position.rowIndex][position.colIndex]
    }

    private fun Topography.allTrailheads(): List<Position> {
        val trailheads = mutableListOf<Position>()

        map.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, height ->
                if(height == 0) trailheads.add(Position(rowIndex, colIndex))
            }
        }

        return trailheads
    }

    private fun traverseTrail(trailhead: Position, topography: Topography, onVisited: (Position) -> Unit) {
        val positionsToVisit = mutableListOf(trailhead)

        while(positionsToVisit.isNotEmpty()) {
            val current = positionsToVisit.removeFirst()
            onVisited(current)

            val validNext = Direction.all
                .map { direction -> current + direction }
                .filter { newPosition ->
                    newPosition.isValid(topography.size) &&
                    topography.map[newPosition] == (topography.map[current] + 1)
                }

            positionsToVisit.addAll(validNext)
        }
    }

    private fun getScore(trailhead: Position, topography: Topography): Int {
        val visitedPositions = mutableSetOf<Position>()
        traverseTrail(trailhead, topography) { visitedPositions += it }
        return visitedPositions.count { topography.map[it] == 9 }
    }

    private fun getRating(trailhead: Position, topography: Topography): Int {
        val visitedPositions = mutableListOf<Position>()
        traverseTrail(trailhead, topography) { visitedPositions += it }
        return visitedPositions.count { topography.map[it] == 9 }
    }

    private fun part1(input: String): Int {
        val topography = parseInput(input)
        val trailheads = topography.allTrailheads()

        return trailheads.sumOf { getScore(it, topography) }
    }

    private fun part2(input: String): Int {
        val topography = parseInput(input)
        val trailheads = topography.allTrailheads()

        return trailheads.sumOf { getRating(it, topography) }
    }
}
