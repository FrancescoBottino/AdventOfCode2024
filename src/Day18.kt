object Day18 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day18_test")
        val input = readInput("Day18")

        println("part 1 test")
        printTimedResult(expectedValue = 22) {
            part1(testInput, Size2D(7, 7), 12)
        }

        println("part 1")
        printTimedResult(expectedValue = 270) {
            part1(input, Size2D(71, 71), 1024)
        }

        println("part 2 test")
        printTimedResult(expectedValue = "6,1") {
            part2(testInput, Size2D(7, 7), 12)
        }

        println("part 2")
        printTimedResult(expectedValue = "51,40") {
            part2(input, Size2D(71, 71), 1024)
        }
    }

    private fun parseInput(input: String): List<Position2D> {
        return input.split("\n").map { coordinateString ->
            coordinateString
                .split(",", limit = 2)
                .let { (x, y) -> Position2D(y.toInt(), x.toInt()) }
        }
    }

    private fun Size2D.toString(cell: (Position2D) -> Char): String {
        return buildString {
            rowIndexes.forEach { rowIndex ->
                if(rowIndex != 0) {
                    append("\n")
                }
                colIndexes.forEach { colIndex ->
                    val position = Position2D(rowIndex, colIndex)

                    append(cell(position))
                }
            }
        }
    }

    private fun part1(input: String, size: Size2D, bytesNum: Int): Int {
        val obstacles = parseInput(input).take(bytesNum).toSet()

        val start = Position2D(0,0)
        val end = Position2D(size.rowIndexes.last, size.colIndexes.last)

        size.toString {
            when(it) {
                start -> 'S'
                end -> 'E'
                in obstacles -> '#'
                else -> '.'
            }
        }.println()

        return aStar(
            start = start,
            end = end,
            isValid = { position -> position.isValid(size) && position !in obstacles }
        )?.getTotalCost() ?: throw RuntimeException("No path between start $start and end $end")
    }

    private fun part2(input: String, size: Size2D, bytesNum: Int): String {
        val allBytes = parseInput(input)
        val start = Position2D(0,0)
        val end = Position2D(size.rowIndexes.last, size.colIndexes.last)

        var hi = allBytes.size
        var lo = bytesNum

        var lastViableByteToAdd: Position2D? = null

        while (hi >= lo) {
            val guess = lo + (hi - lo) / 2

            val obstacles = allBytes.take(guess).toSet()

            val pathFound = aStar(
                start = start,
                end = end,
                isValid = { position -> position.isValid(size) && position !in obstacles }
            ) != null

            if(pathFound) {
                lo = guess + 1
                lastViableByteToAdd = allBytes[guess]
            } else {
                hi = guess -1
            }
        }

        return lastViableByteToAdd?.let { "${it.colIndex},${it.rowIndex}" }
            ?: throw RuntimeException("No byte blocks the path")
    }
}