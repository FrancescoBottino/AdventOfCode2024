import java.util.*

data class Position2D(val rowIndex: Int, val colIndex: Int)

sealed class Direction2D(val rowOffset: Int, val colOffset: Int) {
    data object UP: Direction2D(-1, 0)
    data object RIGHT: Direction2D(0, +1)
    data object DOWN: Direction2D(+1, 0)
    data object LEFT: Direction2D(0, -1)

    companion object {
        val all get() = listOf(UP, RIGHT, DOWN, LEFT)
    }
}

data class Size2D(val rows: Int, val cols: Int) {
    val rowIndexes get() = 0 until rows
    val colIndexes get() = 0 until cols
}

operator fun Position2D.plus(other: Direction2D): Position2D = Position2D(
    this.rowIndex + other.rowOffset,
    this.colIndex + other.colOffset
)

fun Position2D.neighbors(): List<Position2D> {
    return Direction2D.all.map { this + it }
}

fun Position2D.isValid(size: Size2D): Boolean {
    return rowIndex in size.rowIndexes && colIndex in size.colIndexes
}

infix fun Position2D.directionTo(other: Position2D): Direction2D? {
    val rowOffset = other.rowIndex - this.rowIndex
    val colOffset = other.colIndex - this.colIndex

    return Direction2D.all.singleOrNull { rowOffset == it.rowOffset && colOffset == it.colOffset }
}

fun List<Position2D>.tryConvertToDirections(): List<Direction2D> {
    return this.drop(1).fold(emptyList<Direction2D>() to first()) { (directions, previous), current ->
        val updatedDirections = directions + previous.directionTo(current)!!
        updatedDirections to current
    }.first
}

operator fun Direction2D.Companion.get(char: Char) = when(char) {
    '^' -> Direction2D.UP
    'v' -> Direction2D.DOWN
    '<' -> Direction2D.LEFT
    '>' -> Direction2D.RIGHT
    else -> null
}

fun Direction2D.char() = when(this) {
    Direction2D.UP -> '^'
    Direction2D.DOWN -> 'v'
    Direction2D.LEFT -> '<'
    Direction2D.RIGHT -> '>'
}

fun getShortestPathsToAllOtherNodes(
    start: Position2D,
    getReachableFrom: (Position2D) -> List<Position2D> = Position2D::neighbors,
    isValid: (Position2D) -> Boolean,
): Map<Position2D, Set<List<Position2D>>> {
    val distance = mutableMapOf<Position2D, Int>()
    val parents = mutableMapOf<Position2D, MutableList<Position2D>>()
    val queue: Queue<Position2D> = LinkedList()
    queue.add(start)
    distance[start] = 0

    while (queue.isNotEmpty()) {
        val current = queue.poll()

        for (newPosition in getReachableFrom(current).filter { isValid(it) }) {
            val distFromCurrentToNewPosition = distance[current]!! + 1
            val distAtNewPosition = distance[newPosition] ?: Int.MAX_VALUE

            when {
                distFromCurrentToNewPosition < distAtNewPosition -> {
                    distance[newPosition] = distFromCurrentToNewPosition
                    parents[newPosition] = mutableListOf(current)
                    queue.add(newPosition)
                }

                distFromCurrentToNewPosition == distAtNewPosition -> {
                    parents.computeIfAbsent(newPosition) { mutableListOf() }.add(current)
                }
            }
        }
    }

    fun buildPathsTo(end: Position2D): Set<List<Position2D>> {
        if(end == start) {
            return setOf(listOf(start))
        }

        return parents[end]!!
            .flatMap { parentOfEnd -> buildPathsTo(parentOfEnd).map { path -> path + end } }
            .toSet()
    }

    fun buildAllPaths(): Map<Position2D, Set<List<Position2D>>> {
        return parents.keys.associateWith { buildPathsTo(it) }
    }

    return buildAllPaths()
}

fun buildShortestPathsMap(
    nodesToCheck: Set<Position2D>,
    getReachableFrom: (Position2D) -> List<Position2D> = Position2D::neighbors,
    isValid: (Position2D) -> Boolean,
): Map<Position2D, Map<Position2D, Set<List<Position2D>>>> {
    return nodesToCheck.associateWith { position ->
        getShortestPathsToAllOtherNodes(
            start = position,
            getReachableFrom = getReachableFrom,
            isValid = isValid,
        )
    }
}