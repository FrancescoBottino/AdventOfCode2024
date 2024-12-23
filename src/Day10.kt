object Day10 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day10_test")
        val input = readInput("Day10")

        println("Part 1 test")
        printTimedResult(expectedValue = 36) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 798) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(expectedValue = 81) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(expectedValue = 1816) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Topography {
        return input.lines()
            .map { line -> line.map { it.digitToInt() } }
            .let { Topography(Size2D(it.size, it.first().size), it) }
    }

    private data class Topography(
        val size: Size2D,
        val map: List<List<Int>>
    )

    private operator fun Position2D.plus(direction: Direction2D.Orthogonal): Position2D {
        return Position2D(rowIndex + direction.rowOffset, colIndex + direction.colOffset)
    }

    private operator fun <T> List<List<T>>.get(position: Position2D): T {
        return this[position.rowIndex][position.colIndex]
    }

    private fun Topography.allTrailheads(): List<Position2D> {
        val trailheads = mutableListOf<Position2D>()

        map.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, height ->
                if(height == 0) trailheads.add(Position2D(rowIndex, colIndex))
            }
        }

        return trailheads
    }

    private fun traverseTrail(trailhead: Position2D, topography: Topography, onVisited: (Position2D) -> Unit) {
        val positionsToVisit = mutableListOf(trailhead)

        while(positionsToVisit.isNotEmpty()) {
            val current = positionsToVisit.removeFirst()
            onVisited(current)

            val validNext = current.orthogonalNeighbors()
                .filter { newPosition ->
                    newPosition.isValid(topography.size) &&
                    topography.map[newPosition] == (topography.map[current] + 1)
                }

            positionsToVisit.addAll(validNext)
        }
    }

    private fun getScore(trailhead: Position2D, topography: Topography): Int {
        val visitedPositions = mutableSetOf<Position2D>()
        traverseTrail(trailhead, topography) { visitedPositions += it }
        return visitedPositions.count { topography.map[it] == 9 }
    }

    private fun getRating(trailhead: Position2D, topography: Topography): Int {
        val visitedPositions = mutableListOf<Position2D>()
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
