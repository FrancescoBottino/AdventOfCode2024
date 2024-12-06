object Day04 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day04_test")
        val input = readInput("Day04")

        println("Part 1 test")
        printTimedResult(18) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(2390) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(9) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(1809) {
            part2(input)
        }
    }

    private fun parseInput(input: String): List<List<Char>> {
        return input.lines().map { it.toList() }
    }

    private data class Position(val rowIndex: Int, val colIndex: Int)

    private sealed class Direction(val rowOffset: Int, val colOffset: Int) {
        data object UP_LEFT: Direction(-1, -1)
        data object UP: Direction(-1, 0)
        data object UP_RIGHT: Direction(-1, +1)
        data object RIGHT: Direction(0, +1)
        data object DOWN_RIGHT: Direction(+1, +1)
        data object DOWN: Direction(+1, 0)
        data object DOWN_LEFT: Direction(+1, -1)
        data object LEFT: Direction(0, -1)

        companion object {
            val all = setOf(UP_LEFT, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT)
        }
    }

    private operator fun Position.plus(direction: Direction): Position {
        return Position(rowIndex + direction.rowOffset, colIndex + direction.colOffset)
    }

    private fun Position.isValid(grid: List<List<*>>): Boolean {
        return rowIndex in grid.indices && colIndex in grid.first().indices
    }

    private operator fun List<List<Char>>.get(position: Position): Char {
        return this[position.rowIndex][position.colIndex]
    }

    private fun isValidWord(grid: List<List<Char>>, position: Position, direction: Direction): Boolean {
        val wordToFind = "XMAS"

        var currentPosition = position

        if(!currentPosition.isValid(grid)) return false

        wordToFind.forEachIndexed { charIndex, char ->
            if(grid[currentPosition] == char) {
                if(charIndex != wordToFind.lastIndex) {
                    currentPosition += direction
                    if(!currentPosition.isValid(grid)) return false
                }
            } else {
                return false
            }
        }

        return true
    }

    private fun isValidCross(grid: List<List<Char>>, position: Position): Boolean {
        val crossPoints = listOf(
            Direction.UP_LEFT to Direction.DOWN_RIGHT,
            Direction.UP_RIGHT to Direction.DOWN_LEFT,
        )

        return crossPoints
            .map { (a, b) -> (position + a) to (position + b) }
            .all { (a, b) ->
                a.isValid(grid) &&
                        b.isValid(grid) &&
                        ((grid[a] == 'M' && grid[b] == 'S') || grid[b] == 'M' && grid[a] == 'S')
            }
    }

    private fun part1(input: String): Int {
        val grid = parseInput(input)

        var count = 0

        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, char ->
                val position = Position(rowIndex, colIndex)
                if(char == 'X') {
                    count += Direction.all.count { direction ->
                        isValidWord(grid, position, direction)
                    }
                }
            }
        }

        return count
    }

    private fun part2(input: String): Int {
        val grid = parseInput(input)

        var count = 0

        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, char ->
                val position = Position(rowIndex, colIndex)
                if(char == 'A') {
                    if(isValidCross(grid, position)) {
                        count ++
                    }
                }
            }
        }

        return count
    }
}
