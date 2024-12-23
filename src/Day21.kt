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
        printTimedResult(expectedValue = null) {
            simulate(input, 25)
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

        val shortestPaths: Map<Position2D, Map<Position2D, Set<List<RobotAction>>>> = buildShortestPathsMap(
            nodesToCheck = charAtPosition.keys,
            isValid = { it in charAtPosition.keys }
        ).mapValues { (start, reachableNodes) ->
            reachableNodes.mapValues { (end, paths) ->
                paths.map { path -> path.tryConvertToOrthogonalDirections().map { RobotAction.Move(it) } }.toSet()
            }
        }
    }

    private class DoorKeypad: Keypad(listOf("789", "456", "123", " 0A"))
    private class RobotKeypad: Keypad(listOf(" ^A", "<v>"))

    private sealed class RobotAction {
        abstract val char: Char

        data class Move(val direction: Direction2D.Orthogonal): RobotAction() {
            override val char: Char
                get() = direction.char
        }
        data object Activate: RobotAction() {
            override val char: Char
                get() = 'A'
        }
    }

    private fun findAllKeypadStepsCombinations(
        keyToPress: List<Char>,
        keypad: Keypad,
    ): Set<List<RobotAction>> {
        var currentPosition = keypad.positionOfChar['A']!!
        var convertedRobotActions: List<List<RobotAction>> = emptyList()

        keyToPress.forEach { char ->
            val destinationPosition = keypad.positionOfChar[char]!!

            if(currentPosition == destinationPosition) {
                convertedRobotActions = convertedRobotActions.map { it + RobotAction.Activate }
                return@forEach
            }

            convertedRobotActions = keypad.shortestPaths[currentPosition]!![destinationPosition]!!
                .map { path -> path + RobotAction.Activate }
                .flatMap { newPath ->
                    convertedRobotActions
                        .takeIf { it.isNotEmpty() }
                        ?.let { previousPaths -> previousPaths.map { previousPath -> previousPath + newPath } }
                        ?: listOf(newPath)
                }

            currentPosition = destinationPosition
        }

        return convertedRobotActions.toSet()
    }

    private fun findAllPathsToTypeCode(
        code: String, /* 029A, 980A, 179A, 456A, 379A */
        robots: Int, /* 3 to 25*/
        doorKeypad: DoorKeypad,
        robotKeypad: RobotKeypad,
    ): Set<List<RobotAction>> {
        return if(robots == 1) {
            findAllKeypadStepsCombinations(code.toList(), doorKeypad)
                .also { println("robots: $robots, size is: ${it.size}") }
        } else {
            findAllPathsToTypeCode(code, robots - 1, doorKeypad, robotKeypad)
                .flatMap { actions -> findAllKeypadStepsCombinations(actions.map { it.char }, robotKeypad) }
                .toSet()
                .also { println("robots: $robots, sizes are: ${it.groupingBy { it.size }.eachCount().entries.joinToString { "size ${it.key} repeats ${it.value}" }}") }
        }
    }

    private fun simulate(input: String, robots: Int): Int {
        val doorKeypad = DoorKeypad()
        val robotKeypad = RobotKeypad()

        return input.lines().sumOf { code ->
            val pathLength = findAllPathsToTypeCode(
                code = code,
                robots = robots,
                doorKeypad = doorKeypad,
                robotKeypad = robotKeypad,
            ).minOf { it.size }

            code.filter { it.isDigit() }.toInt() * pathLength
        }
    }
}