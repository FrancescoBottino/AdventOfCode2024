import kotlin.math.abs

fun manhattanDistance(a: Position2D, b: Position2D): Int {
    return abs(a.rowIndex - b.rowIndex) + abs(a.colIndex - b.colIndex)
}

fun aStar(
    start: Position2D,
    end: Position2D,
    getReachableFrom: (Position2D) -> List<Position2D> = Position2D::orthogonalNeighbors,
    heuristic: (Position2D, Position2D) -> Int = ::manhattanDistance,
    isValid: (Position2D) -> Boolean,
): AStarResult? {
    val openVertices = mutableSetOf(start)
    val closedVertices = mutableSetOf<Position2D>()
    val costFromStart = mutableMapOf(start to 0)
    val estimatedTotalCost = mutableMapOf(start to heuristic(start, end))
    val cameFrom = mutableMapOf<Position2D, Position2D>()

    while (openVertices.size > 0) {
        val currentPos = openVertices.minBy { estimatedTotalCost.getValue(it) }

        if (currentPos == end) {
            return AStarResult(start, end, cameFrom, costFromStart)
        }

        openVertices.remove(currentPos)
        closedVertices.add(currentPos)

        getReachableFrom(currentPos)
            .filter { isValid(it) }
            .filter { it !in closedVertices }
            .forEach { neighbour ->
                val score = costFromStart.getValue(currentPos) + heuristic(currentPos, neighbour)
                if (score < costFromStart.getOrDefault(neighbour, Int.MAX_VALUE)) {
                    if (!openVertices.contains(neighbour)) {
                        openVertices.add(neighbour)
                    }
                    cameFrom[neighbour] = currentPos
                    costFromStart[neighbour] = score
                    estimatedTotalCost[neighbour] = score + heuristic(neighbour, end)
                }
            }
    }

    return null
}

data class AStarResult(
    val start: Position2D,
    val end: Position2D,
    val cameFrom: Map<Position2D, Position2D>,
    val costs: Map<Position2D, Int>,
)

fun AStarResult.getTotalCost(): Int {
    return costs.getValue(end)
}

data class AStarStep(
    val position: Position2D,
    val cumulativeCost: Int,
)

fun AStarResult.generatePath(): List<AStarStep> {
    val totalCost = getTotalCost()
    val path = mutableListOf(AStarStep(end, totalCost))

    var currentPosition = end
    var currentDistance: Int

    while (cameFrom.containsKey(currentPosition)) {
        currentPosition = cameFrom.getValue(currentPosition)
        currentDistance = costs[currentPosition]!!
        path.add(0, AStarStep(currentPosition, currentDistance))
    }

    return path.toList()
}