import Direction2D.Diagonal.*

object Day04 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day04_test")
        val input = readInput("Day04")

        println("Part 1 test")
        printTimedResult(expectedValue = 18) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 2390) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(expectedValue = 9) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(expectedValue = 1809) {
            part2(input)
        }
    }

    private fun parseInput(input: String): List<List<Char>> {
        return input.lines().map { it.toList() }
    }

    private operator fun List<List<Char>>.get(position: Position2D): Char {
        return this[position.rowIndex][position.colIndex]
    }

    private fun isValidWord(grid: List<List<Char>>, position: Position2D, direction: Direction2D): Boolean {
        val wordToFind = "XMAS"
        val size = Size2D(grid.size, grid.first().size)

        var currentPosition = position

        if(!currentPosition.isValid(size)) return false

        wordToFind.forEachIndexed { charIndex, char ->
            if(grid[currentPosition] == char) {
                if(charIndex != wordToFind.lastIndex) {
                    currentPosition += direction
                    if(!currentPosition.isValid(size)) return false
                }
            } else {
                return false
            }
        }

        return true
    }

    private fun isValidCross(grid: List<List<Char>>, position: Position2D): Boolean {
        val crossPoints = listOf(
            UP_LEFT to DOWN_RIGHT,
            UP_RIGHT to DOWN_LEFT,
        )
        val size = Size2D(grid.size, grid.first().size)

        return crossPoints
            .map { (a, b) -> (position + a) to (position + b) }
            .all { (a, b) ->
                a.isValid(size) &&
                b.isValid(size) &&
                ((grid[a] == 'M' && grid[b] == 'S') || grid[b] == 'M' && grid[a] == 'S')
            }
    }

    private fun part1(input: String): Int {
        val grid = parseInput(input)

        var count = 0

        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, char ->
                val position = Position2D(rowIndex, colIndex)
                if(char == 'X') {
                    count += Direction2D.all.count { direction ->
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
                val position = Position2D(rowIndex, colIndex)
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
