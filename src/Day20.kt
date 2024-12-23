object Day20 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day20_test")
        val input = readInput("Day20")

        println("part 1 test")
        printTimedResult(expectedValue = 44) {
            part1(testInput, minimumTimeSaved = 1)
        }

        println("part 1")
        printTimedResult(expectedValue = 1378) {
            part1(input, minimumTimeSaved = 100)
        }

        TODO("Improve efficiency")

        println("part 2 test")
        printTimedResult(expectedValue = null) {
            part2(testInput)
        }

        println("part 2")
        printTimedResult(expectedValue = null) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Race {
        val rows = input.lines()
        val size = Size2D(rows.size, rows[0].length)
        lateinit var start: Position2D
        lateinit var end: Position2D

        val map = rows.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, char ->
                val position = Position2D(rowIndex, colIndex)

                when(char) {
                    'S' -> start = position
                    'E' -> end = position
                }

                position to char
            }
        }.flatten().toMap()

        return Race(
            map = map,
            size = size,
            start = start,
            end = end
        )
    }

    private data class Race(
        val map: Map<Position2D, Char>,
        val size : Size2D,
        val start: Position2D,
        val end: Position2D,
    )

    private fun findShortcuts(
        path: List<AStarStep>,
        map: Map<Position2D, Char>,
    ): Map<Position2D, Int> {
        val distancesFromStart: Map<Position2D, Int> = path.associate { (position, cumulativeCost) -> position to cumulativeCost }

        val shortcuts = mutableMapOf<Position2D, Int>()

        path.forEach stepLoop@ { (stepPosition, timeTaken) ->
            Direction2D.Orthogonal.all.forEach directionLoop@ { direction ->
                val wallToRemove = stepPosition + direction

                if(map[wallToRemove] != '#') {
                    return@directionLoop
                }

                val positionOnPathReached = wallToRemove + direction

                if(!distancesFromStart.keys.contains(positionOnPathReached)) {
                    return@directionLoop
                }

                val distanceAtCheatPosition = distancesFromStart[positionOnPathReached]!!

                if(distanceAtCheatPosition < timeTaken) {
                    return@directionLoop
                }

                val timeSaved = distancesFromStart[positionOnPathReached]!! - timeTaken - 2

                shortcuts[wallToRemove] = timeSaved
            }
        }

        return shortcuts
    }

    private fun part1(input: String, minimumTimeSaved: Int): Int {
        val race = parseInput(input)

        val path = aStar(
            start = race.start,
            end = race.end,
            isValid = { position -> position.isValid(race.size) && race.map[position] != '#' },
        )?.generatePath()
            ?: throw RuntimeException("No path found")

        val shortcuts = findShortcuts(
            path = path,
            map = race.map
        )

        return shortcuts.count { it.value >= minimumTimeSaved }
    }

    private fun part2(input: String): Long {
        TODO()
    }
}