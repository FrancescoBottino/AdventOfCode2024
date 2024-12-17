object Day16 {
    @JvmStatic
    fun main(args: Array<String>) {
        val smallTestInput = readInput("Day16_test_small")
        val bigTestInput = readInput("Day16_test_big")
        val input = readInput("Day16")

        //TODO optimize maze exploration

        println("Part 1 test small")
        printTimedResult(expectedValue = 7036) {
            part1(smallTestInput)
        }

        println("Part 1 test big")
        printTimedResult(expectedValue = 11048) {
            part1(bigTestInput)
        }

        println("Part 1")
        printTimedResult(expectedValue = 98520) {
            part1(input)
        }

        println("part 2 test small")
        printTimedResult(expectedValue = 45) {
            part2(smallTestInput)
        }

        println("Part 2 test big")
        printTimedResult(expectedValue = 64) {
            part2(bigTestInput)
        }

        println("Part 2")
        printTimedResult(expectedValue = null) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Maze {
        lateinit var startPosition: Position2D
        lateinit var endPosition: Position2D

        val size = Size2D(input.lines().size, input.lines().first().length)

        return input.lines().mapIndexed { rowIndex, row ->
            row.mapIndexedNotNull { colIndex, char ->
                Position2D(rowIndex, colIndex)
                    .takeIf { char != '#' }
                    ?.also {
                        if(char == 'S') startPosition = it
                        if(char == 'E') endPosition = it
                    }
            }
        }.flatten().toSet().let {
            Maze(it, startPosition, endPosition, size)
        }
    }

    private data class Maze(
        val positions: Set<Position2D>,
        val start: Position2D,
        val end: Position2D,
        val size: Size2D,
    )

    private data class Position2D(val rowIndex: Int, val colIndex: Int)

    private sealed class Direction2D private constructor (val rowOffset: Int, val colOffset: Int) {
        data object UP: Direction2D(-1, 0)
        data object RIGHT: Direction2D(0, +1)
        data object DOWN: Direction2D(+1, 0)
        data object LEFT: Direction2D(0, -1)

        companion object {
            val all get() = listOf(UP, RIGHT, DOWN, LEFT)
        }
    }

    private data class Size2D(
        val rowCount: Int,
        val colCount: Int,
    )

    private operator fun Position2D.plus(other: Direction2D): Position2D = Position2D(
        this.rowIndex + other.rowOffset,
        this.colIndex + other.colOffset
    )

    private fun Direction2D.clockwise(): Direction2D {
        return Direction2D.all[Math.floorMod(Direction2D.all.indexOf(this) + 1, Direction2D.all.size)]
    }

    private fun Direction2D.counterClockWise(): Direction2D {
        return Direction2D.all[Math.floorMod(Direction2D.all.indexOf(this) - 1, Direction2D.all.size)]
    }

    private data class MazeRunNode(
        val position: Position2D,
        val direction: Direction2D,
    )

    private sealed class MazeRunStep(
        val cost: Long,
    ) {
        data object Run: MazeRunStep(1)
        data object ClockwiseTurn: MazeRunStep(1000)
        data object CounterClockwiseTurn: MazeRunStep(1000)
    }

    private operator fun MazeRunNode.plus(step: MazeRunStep): MazeRunNode {
        return MazeRunNode(
            position = when (step) {
                is MazeRunStep.Run -> position + direction
                else -> position
            },
            direction = when (step) {
                is MazeRunStep.ClockwiseTurn -> direction.clockwise()
                is MazeRunStep.CounterClockwiseTurn -> direction.counterClockWise()
                else -> direction
            }
        )
    }

    private data class MazeRunNodeState(
        val previousNodes: Set<MazeRunNode>,
        val cumulativeScore: Long,
    )

    private data class MazeExploration(
        val nodesStates: Map<MazeRunNode, MazeRunNodeState>,
        val startingNode: MazeRunNode,
        val endingNodes: Set<MazeRunNode>
    )

    private fun getPossibleSteps(maze: Maze, state: MazeRunNode): List<MazeRunStep> {
        return listOfNotNull(
            MazeRunStep.Run.takeIf { maze.positions.contains(state.position + state.direction) },
            MazeRunStep.ClockwiseTurn,
            MazeRunStep.CounterClockwiseTurn
        )
    }

    private fun Maze.exploreMaze(): MazeExploration {
        val possibleStepsMemo = mutableMapOf<MazeRunNode, List<MazeRunStep>>()
        val toVisit = mutableListOf<MazeRunNode>()
        val visited = mutableMapOf<MazeRunNode, MazeRunNodeState>()

        val start = MazeRunNode(this.start, Direction2D.RIGHT)

        toVisit.add(start)

        while(toVisit.isNotEmpty()) {
            val currentNode = toVisit.removeLast()
            val currentState = visited[currentNode]

            val currentScore = currentState?.cumulativeScore ?: 0L

            val possibleSteps = possibleStepsMemo[currentNode] ?: getPossibleSteps(this, currentNode).also { possibleStepsMemo[currentNode] = it }

            for(step in possibleSteps) {
                val newNode = currentNode + step
                val newScore = currentScore + step.cost

                val newNodeState = visited[newNode]

                if(newNodeState == null || newScore < newNodeState.cumulativeScore) {
                    toVisit.add(newNode)
                    visited[newNode] = MazeRunNodeState(previousNodes = setOf(currentNode), cumulativeScore = newScore)
                } else if(newScore == newNodeState.cumulativeScore) {
                    toVisit.add(newNode)
                    visited[newNode] = newNodeState.copy(previousNodes = newNodeState.previousNodes.toMutableSet().apply { add(currentNode) })
                }
            }
        }

        val endStates = visited.keys.filter { it.position == this.end }.toSet()

        return MazeExploration(
            nodesStates = visited,
            startingNode = start,
            endingNodes = endStates,
        )
    }

    private fun MazeExploration.findBestScore(): Long {
        return nodesStates.filterKeys { it in endingNodes }.values.minOf { it.cumulativeScore }
    }

    private fun part1(input: String): Long {
        return parseInput(input)
            .exploreMaze()
            .findBestScore()
    }

    private fun MazeExploration.getWinningPaths(): List<List<Position2D>> {
        val endStates = nodesStates.filterKeys { it in endingNodes }
        val minScore = endStates.values.minOf { it.cumulativeScore }
        val winningPathEnds = endStates.filterValues { it.cumulativeScore == minScore }.keys

        return winningPathEnds.map { endNode -> extractPathsTo(endNode).map { it + endNode } }.flatten().map { steps -> steps.map { it.position } }
    }

    private fun MazeExploration.extractPathsTo(targetNode: MazeRunNode): List<List<MazeRunNode>> {
        if(targetNode == startingNode) {
            return listOf(listOf(targetNode))
        }

        return buildList {
            nodesStates[targetNode]!!.previousNodes.forEach { previousNode ->
                addAll(extractPathsTo(previousNode).map { it + previousNode })
            }
        }
    }

    private fun part2(input: String): Long {
        lateinit var maze: Maze
        lateinit var exploration: MazeExploration
        return parseInput(input)
            .also { maze = it }
            .exploreMaze()
            .also { exploration = it }
            .getWinningPaths()
            .flatten()
            .toSet()
            .also { winningPath ->
                (0..<maze.size.rowCount).forEach { rowIndex ->
                    if(rowIndex != 0) print("\n")
                    (0..<maze.size.colCount).forEach { colIndex ->
                        val position = Position2D(rowIndex, colIndex)
                        when {
                            position !in maze.positions -> print('#')
                            position in winningPath -> print('O')
                            else -> print('.')
                        }

                    }
                }
            }
            .size
            .toLong()
    }
}
