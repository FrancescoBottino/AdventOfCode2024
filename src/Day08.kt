object Day08 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day08_test")
        val input = readInput("Day08")

        println("Part 1 test")
        printTimedResult(14) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(289) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(34) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(1030) {
            part2(input)
        }
    }

    private fun parseInput(input: String): AntennasMap {
        val grid = input.lines().map { line -> line.toList() }

        val size = Size(grid.size, grid.first().size)
        val antennas = grid.mapIndexed { rowIndex, row ->
            row.mapIndexedNotNull { colIndex, char ->
                val position = Position(rowIndex, colIndex)

                if(char.isLetter() || char.isDigit()) {
                    char to position
                } else null
            }
        }.flatten().groupBy({ it.first }, { it.second })

        return AntennasMap(antennas, size)
    }

    private data class AntennasMap(
        val antennas: Map<Char, List<Position>>,
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

    private fun <T> List<T>.forEachPair(operation: (T, T) -> Unit) {
        for (i in 0..<lastIndex) {
            for (j in (i + 1)..lastIndex) {
                operation(get(i), get(j))
            }
        }
    }

    private fun getValidAntinodes(antennaPositions: List<Position>, size: Size): List<Position> {
        val antinodes = mutableListOf<Position>()

        antennaPositions.forEachPair { a, b ->
            val rowDistance = a.rowIndex - b.rowIndex
            val colDistance = a.colIndex - b.colIndex

            val p = Position(a.rowIndex + rowDistance, a.colIndex + colDistance)
            val q = Position(b.rowIndex - rowDistance, b.colIndex - colDistance)

            if(p.isValid(size))
                antinodes.add(p)
            if(q.isValid(size))
                antinodes.add(q)
        }

        return antinodes
    }

    private fun part1(input: String): Int {
        val antennasMap = parseInput(input)

        return antennasMap.antennas.map { (_, antennaPositions) ->
            getValidAntinodes(antennaPositions, antennasMap.size)
        }.flatten().toSet().size
    }

    private fun getValidAntinodesWithResonance(antennaPositions: List<Position>, size: Size): List<Position> {
        val antinodes = mutableListOf<Position>()

        antennaPositions.forEachPair { a, b ->
            val rowDistance = a.rowIndex - b.rowIndex
            val colDistance = a.colIndex - b.colIndex

            var p = a
            while(p.isValid(size)) {
                antinodes.add(p)
                p = Position(p.rowIndex + rowDistance, p.colIndex + colDistance)
            }

            var q = b
            while(q.isValid(size)) {
                antinodes.add(q)
                q = Position(q.rowIndex - rowDistance, q.colIndex - colDistance)
            }
        }

        return antinodes
    }

    private fun part2(input: String): Int {
        val antennasMap = parseInput(input)

        return antennasMap.antennas.map { (_, antennaPositions) ->
            getValidAntinodesWithResonance(antennaPositions, antennasMap.size)
        }.flatten().toSet().size
    }
}
