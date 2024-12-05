fun main() {
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

private fun updatePosition(currentPosition: Pair<Int, Int>, direction: Pair<Int, Int>): Pair<Int, Int> {
    return (currentPosition.first + direction.first) to (currentPosition.second + direction.second)
}

private fun isPositionValid(position: Pair<Int, Int>, grid: List<List<Char>>): Boolean {
    return position.first in grid.indices && position.second in grid.first().indices
}

private operator fun List<List<Char>>.get(position: Pair<Int, Int>): Char {
    return this[position.first][position.second]
}

private fun isValidWord(grid: List<List<Char>>, position: Pair<Int, Int>, direction: Pair<Int, Int>): Boolean {
    val wordToFind = "XMAS"

    var currentPosition: Pair<Int, Int> = position

    if(!isPositionValid(currentPosition, grid)) return false

    wordToFind.forEachIndexed { charIndex, char ->
        if(grid[currentPosition] == char) {
            if(charIndex != wordToFind.lastIndex) {
                currentPosition = updatePosition(currentPosition, direction)
                if(!isPositionValid(currentPosition, grid)) return false
            }
        } else {
            return false
        }
    }

    return true
}

private fun isValidCross(grid: List<List<Char>>, position: Pair<Int, Int>): Boolean {
    val crossPoints = listOf(
        (-1 to -1) to (+1 to +1),
        (-1 to +1) to (+1 to -1),
    )

    return crossPoints
        .map { (a, b) -> updatePosition(position, a) to updatePosition(position, b) }
        .all { (a, b) ->
            isPositionValid(a, grid) &&
            isPositionValid(b, grid) &&
            ((grid[a] == 'M' && grid[b] == 'S') || grid[b] == 'M' && grid[a] == 'S')
        }
}

private fun part1(input: String): Int {
    val grid = parseInput(input)

    val directions = listOf(
        0 to +1,
        0 to -1,
        +1 to 0,
        -1 to 0,
        +1 to +1,
        +1 to -1,
        -1 to +1,
        -1 to -1,
    )

    var count = 0

    grid.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { colIndex, char ->
            val position = rowIndex to colIndex
            if(char == 'X') {
                count += directions.count { direction ->
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
            val position = rowIndex to colIndex

            if(char == 'A') {
                if(isValidCross(grid, position)) {
                    count ++
                }
            }
        }
    }

    return count
}
