object Day08 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day08_test")
        val input = readInput("Day08")

        println("Part 1 test")
        printTimedResult(expectedValue = 14) {
            part1(testInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 289) {
            part1(input)
        }

        println("Part 2 test")
        printTimedResult(expectedValue = 34) {
            part2(testInput)
        }

        println("Part 2")
        printTimedResult(expectedValue = 1030) {
            part2(input)
        }
    }

    private fun parseInput(input: String): AntennasMap {
        val grid = input.lines().map { line -> line.toList() }

        val size = Size2D(grid.size, grid.first().size)
        val antennas = grid.mapIndexed { rowIndex, row ->
            row.mapIndexedNotNull { colIndex, char ->
                val position = Position2D(rowIndex, colIndex)

                if(char.isLetter() || char.isDigit()) {
                    char to position
                } else null
            }
        }.flatten().groupBy({ it.first }, { it.second })

        return AntennasMap(antennas, size)
    }

    private data class AntennasMap(
        val antennas: Map<Char, List<Position2D>>,
        val size: Size2D,
    )

    private fun <T> List<T>.forEachPair(operation: (T, T) -> Unit) {
        for (i in 0..<lastIndex) {
            for (j in (i + 1)..lastIndex) {
                operation(get(i), get(j))
            }
        }
    }

    private fun getValidAntinodes(antennaPositions: List<Position2D>, size: Size2D): List<Position2D> {
        val antinodes = mutableListOf<Position2D>()

        antennaPositions.forEachPair { a, b ->
            val p = a + (b directionTo a)
            if(p.isValid(size))
                antinodes.add(p)

            val q = b + (a directionTo b)
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

    private fun getValidAntinodesWithResonance(antennaPositions: List<Position2D>, size: Size2D): List<Position2D> {
        val antinodes = mutableListOf<Position2D>()

        antennaPositions.forEachPair { a, b ->
            var p = a
            val pDirection = (b directionTo a)
            while(p.isValid(size)) {
                antinodes.add(p)
                p += pDirection
            }

            var q = b
            val qDirection = (a directionTo b)
            while(q.isValid(size)) {
                antinodes.add(q)
                q += qDirection
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
