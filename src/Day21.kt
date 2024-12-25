object Day21 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day21_test")
        val input = readInput("Day21")

        println("part 1 test")
        printTimedResult(expectedValue = 126384) {
            simulate(testInput, 3)
        }

        println("part 1")
        printTimedResult(expectedValue = 156714) {
            simulate(input, 3)
        }

        println("part 2")
        printTimedResult(expectedValue = 191139369248202) {
            simulate(input, 26)
        }
    }

    private sealed class Keypad(
        buttons: List<String>
    ) {
        val charAtPosition: Map<Position2D, Char> = buttons
            .mapIndexed { rowIndex, row ->
                row.mapIndexedNotNull { colIndex, char ->
                    if(char != ' ')
                        Position2D(rowIndex, colIndex) to char
                    else
                        null
                }
            }
            .flatten()
            .toMap()

        val positionOfChar: Map<Char, Position2D> = charAtPosition.inverted()

        val shortestPaths: Map<Position2D, Map<Position2D, Set<List<Char>>>> = buildShortestPathsMap(
            nodesToCheck = charAtPosition.keys,
            isValid = { it in charAtPosition.keys }
        ).mapValues { (start, reachableNodes) ->
            reachableNodes.mapValues { (end, paths) ->
                paths.map { path -> path.asDirections().map { it.char } }.toSet()
            }
        }

        fun getPath(from: Char, to: Char): Set<List<Char>> {
            return if(from != to)
                shortestPaths[positionOfChar[from]!!]!![positionOfChar[to]!!]!!
            else
                setOf(emptyList())
        }
    }

    private class DoorKeypad: Keypad(
        """
            789
            456
            123
             0A
        """.trimIndent().lines()
    )
    private class RobotKeypad: Keypad(
        """
             ^A
            <v>
        """.trimIndent().lines()
    )

    private fun shortestPathLengthForCode(
        code: List<Char>,
        maxDepth: Int,
        doorKeypad: DoorKeypad,
        robotKeypad: RobotKeypad,
        level: Int = 0,
        cache: MutableMap<Pair<List<Char>, Int>, Long> = mutableMapOf(),
    ): Long {
        return cache.getOrPut(code to level) {
            if(level == maxDepth) {
                code.size.toLong()
            } else {
                val keypad = if(level == 0) doorKeypad else robotKeypad

                code.let { listOf('A') + it }
                    .zipWithNext()
                    .map { (from, to) -> keypad.getPath(from, to).map { it + 'A' }.toSet() }
                    .sumOf { paths ->
                        paths.minOf { path ->
                            shortestPathLengthForCode(
                                code = path,
                                maxDepth = maxDepth,
                                doorKeypad = doorKeypad,
                                robotKeypad = robotKeypad,
                                level = level + 1,
                                cache = cache
                            )
                        }
                    }
            }
        }
    }

    private fun simulate(input: String, robots: Int): Long {
        val doorKeypad = DoorKeypad()
        val robotKeypad = RobotKeypad()

        return input.lines().sumOf { code ->
            val pathLength = shortestPathLengthForCode(
                code = code.toList(),
                maxDepth = robots,
                doorKeypad = doorKeypad,
                robotKeypad = robotKeypad,
            )

            code.filter { it.isDigit() }.toLong() * pathLength
        }
    }
}