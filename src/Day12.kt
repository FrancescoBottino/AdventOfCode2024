import Direction2D.Orthogonal.*

object Day12 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day12_test")
        val input = readInput("Day12")

        println("Part 1 test")
        printTimedResult(expectedValue = 1930) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 1573474) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(expectedValue = 1206) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(expectedValue = 966476) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Grid2D {
        return input.lines().map { it.toList() }.let { Grid2D(it.size2D, it) }
    }

    private data class Grid2D(
        val size: Size2D,
        val chars: List<List<Char>>,
    ) {
        operator fun get(position: Position2D): Char {
            return chars[position]
        }
    }

    private operator fun <T> List<List<T>>.get(position: Position2D): T {
        return this[position.rowIndex][position.colIndex]
    }

    private val <T> List<List<T>>.size2D: Size2D get() = Size2D(size, first().size)

    private data class Region2D(
        val char: Char,
        val positions: Set<Position2D>,
    ) {
        val area = positions.size
    }

    private fun getAllRegions(grid: Grid2D): List<Region2D> {
        val regions = mutableListOf<Region2D>()
        val visited = mutableSetOf<Position2D>()

        val allPositions = grid.size.allPositions()
        val available = allPositions.toMutableSet()

        while(available.isNotEmpty()) {
            val current = available.first()

            val newRegion = expandRegion(grid, current)
            regions += newRegion
            visited += newRegion.positions
            available -= newRegion.positions
        }

        return regions
    }

    private fun expandRegion(grid: Grid2D, initialPosition: Position2D): Region2D {
        val char = grid[initialPosition]

        val toVisit = mutableListOf(initialPosition)
        val regionPositions = mutableSetOf<Position2D>()

        while(toVisit.isNotEmpty()) {
            val current = toVisit.removeLast()
            regionPositions += current

            current.orthogonalNeighbors()
                .filter {
                    it.isValid(grid.size) &&
                    grid[it] == char &&
                    it !in regionPositions
                }
                .let { toVisit.addAll(it) }
        }

        return Region2D(char, regionPositions)
    }

    private fun Region2D.perimeter(grid: Grid2D): Int {
        return positions.sumOf { position ->
            position.orthogonalNeighbors()
                .count { !it.isValid(grid.size) || grid[it] != char }
        }
    }

    private fun Region2D.sidesLengthSum(grid: Grid2D): Int {
        val external = positions.filter { position ->
            position.orthogonalNeighbors()
                .any { !it.isValid(grid.size) || grid[it] != char }
        }

        val rows = external.map { it.rowIndex }.toSet()
        val cols = external.map { it.colIndex }.toSet()

        var sides = 0

        rows.forEach { rowIndex ->
            var lastTopSide: Position2D? = null
            var lastBotSide: Position2D? = null

            external
                .filter { it.rowIndex == rowIndex }
                .sortedBy { it.colIndex }
                .forEach { position ->
                    val hasTopSideBorder = (position + UP)
                        .let { !it.isValid(grid.size) || grid[it] != char }

                    if(hasTopSideBorder) {
                        if(lastTopSide != null) {
                            if((lastTopSide!!.colIndex + 1) == position.colIndex) {
                                lastTopSide = position
                            } else {
                                sides++
                                lastTopSide = position
                            }
                        } else {
                            lastTopSide = position
                        }
                    } else {
                        if(lastTopSide != null) {
                            sides++
                            lastTopSide = null
                        }
                    }

                    val hasBotSideBorder = (position + DOWN)
                        .let { !it.isValid(grid.size) || grid[it] != char }

                    if(hasBotSideBorder) {
                        if(lastBotSide != null) {
                            if((lastBotSide!!.colIndex + 1) == position.colIndex) {
                                lastBotSide = position
                            } else {
                                sides++
                                lastBotSide = position
                            }
                        } else {
                            lastBotSide = position
                        }
                    } else {
                        if(lastBotSide != null) {
                            sides++
                            lastBotSide = null
                        }
                    }
                }

            if(lastTopSide != null) {
                sides++
            }

            if(lastBotSide != null) {
                sides++
            }
        }

        cols.forEach { colIndex ->
            var lastLeftSide: Position2D? = null
            var lastRightSide: Position2D? = null

            external
                .filter { it.colIndex == colIndex }
                .sortedBy { it.rowIndex }
                .forEach { position ->
                    val hasLeftSideBorder = (position + LEFT)
                        .let { !it.isValid(grid.size) || grid[it] != char }

                    if(hasLeftSideBorder) {
                        if(lastLeftSide != null) {
                            if((lastLeftSide!!.rowIndex + 1) == position.rowIndex) {
                                lastLeftSide = position
                            } else {
                                sides++
                                lastLeftSide = position
                            }
                        } else {
                            lastLeftSide = position
                        }
                    } else {
                        if(lastLeftSide != null) {
                            sides++
                            lastLeftSide = null
                        }
                    }

                    val hasRightSideBorder = (position + RIGHT)
                        .let { !it.isValid(grid.size) || grid[it] != char }

                    if(hasRightSideBorder) {
                        if(lastRightSide != null) {
                            if((lastRightSide!!.rowIndex + 1) == position.rowIndex) {
                                lastRightSide = position
                            } else {
                                sides++
                                lastRightSide = position
                            }
                        } else {
                            lastRightSide = position
                        }
                    } else {
                        if(lastRightSide != null) {
                            sides++
                            lastRightSide = null
                        }
                    }
                }

            if(lastLeftSide != null) {
                sides++
            }

            if(lastRightSide != null) {
                sides++
            }
        }

        return sides
    }

    private fun part1(input: String): Long {
        val grid = parseInput(input)
        val regions = getAllRegions(grid)
        return regions.sumOf { (it.area * it.perimeter(grid)).toLong() }
    }

    private fun part2(input: String): Long {
        val grid = parseInput(input)
        val regions = getAllRegions(grid)
        return regions.sumOf { (it.area * it.sidesLengthSum(grid)).toLong() }
    }
}
