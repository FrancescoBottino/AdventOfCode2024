import Direction2D.Diagonal.*
import Direction2D.Orthogonal.*
import java.util.*

data class Position2D(val rowIndex: Int, val colIndex: Int) {
    override fun toString(): String = "($rowIndex, $colIndex)"
}

open class Direction2D(val rowOffset: Int, val colOffset: Int) {
    sealed class Orthogonal(rowOffset: Int, colOffset: Int): Direction2D(rowOffset, colOffset) {
        abstract val char: Char

        fun next() = clockwise()

        fun clockwise(): Orthogonal {
            return all[Math.floorMod(all.indexOf(this) + 1, all.size)]
        }

        fun counterClockWise(): Orthogonal {
            return all[Math.floorMod(all.indexOf(this) - 1, all.size)]
        }

        data object UP: Orthogonal(-1, 0) {
            override val char: Char = '^'
        }
        data object RIGHT: Orthogonal(0, +1) {
            override val char: Char = '>'
        }
        data object DOWN: Orthogonal(+1, 0) {
            override val char: Char = 'v'
        }
        data object LEFT: Orthogonal(0, -1) {
            override val char: Char = '<'
        }

        override fun toString() = char.toString()

        companion object {
            val all get() = listOf(UP, RIGHT, DOWN, LEFT)
            operator fun get(char: Char) = all.single { it.char == char }
        }
    }

    sealed class Diagonal(rowOffset: Int, colOffset: Int): Direction2D(rowOffset, colOffset) {
        data object UP_LEFT: Diagonal(-1, -1)
        data object UP_RIGHT: Diagonal(-1, +1)
        data object DOWN_RIGHT: Diagonal(+1, +1)
        data object DOWN_LEFT: Diagonal(+1, -1)

        companion object {
            val all get() = listOf(UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT)
        }
    }

    companion object {
        val all get() = listOf(UP_LEFT, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Direction2D) return false

        if (rowOffset != other.rowOffset) return false
        if (colOffset != other.colOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rowOffset
        result = 31 * result + colOffset
        return result
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

fun Position2D.orthogonalNeighbors(): List<Position2D> {
    return Direction2D.Orthogonal.all.map { this + it }
}

fun Position2D.isValid(size: Size2D): Boolean {
    return rowIndex in size.rowIndexes && colIndex in size.colIndexes
}

fun Position2D.wrapAround(size: Size2D): Position2D {
    return Position2D(
        rowIndex = Math.floorMod(this.rowIndex, size.rows),
        colIndex = Math.floorMod(this.colIndex, size.cols),
    )
}

infix fun Position2D.directionTo(other: Position2D): Direction2D {
    return Direction2D(other.rowIndex - this.rowIndex, other.colIndex - this.colIndex)
}

infix fun Position2D.orthogonalDirectionTo(other: Position2D): Direction2D.Orthogonal? {
    val rowOffset = other.rowIndex - this.rowIndex
    val colOffset = other.colIndex - this.colIndex

    return Direction2D.Orthogonal.all.singleOrNull { rowOffset == it.rowOffset && colOffset == it.colOffset }
}

fun List<Position2D>.asDirections(): List<Direction2D.Orthogonal> {
    return this.zipWithNext().map { (from, to) -> from.orthogonalDirectionTo(to)!! }
}

operator fun Direction2D.times(amount: Int) = Direction2D(rowOffset * amount, colOffset * amount)

fun Size2D.allPositions(): Set<Position2D> {
    return rowIndexes.flatMap { rowIndex -> colIndexes.map { colIndex -> Position2D(rowIndex, colIndex) } }.toSet()
}

fun getShortestPathsToAllOtherNodes(
    start: Position2D,
    getReachableFrom: (Position2D) -> List<Position2D> = Position2D::orthogonalNeighbors,
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
    getReachableFrom: (Position2D) -> List<Position2D> = Position2D::orthogonalNeighbors,
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