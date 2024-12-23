object Day23 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day23_test")
        val input = readInput("Day23")

        println("part 1 test")
        printTimedResult(expectedValue = 7) {
            part1(testInput)
        }

        println("part 1")
        printTimedResult(expectedValue = 1215) {
            part1(input)
        }

        println("part 2 test")
        printTimedResult(expectedValue = "co,de,ka,ta") {
            part2(testInput)
        }

        println("part 2")
        printTimedResult(expectedValue = "bm,by,dv,ep,ia,ja,jb,ks,lv,ol,oy,uz,yt") {
            part2(input)
        }
    }

    private fun parseInput(input: String): Graph<String> {
        val edges = mutableMapOf<String, Set<String>>()

        input.lines().forEach { line ->
            val (a, b) = line.split("-", limit = 2)

            edges[a] = edges.getOrDefault(a, emptySet()) + b
            edges[b] = edges.getOrDefault(b, emptySet()) + a
        }

        return Graph(edges)
    }

    private data class Graph<T>(
        val edges: Map<T, Set<T>>
    ) {
        val nodes get() = edges.keys
    }

    private fun <T> Graph<T>.findTriangles(): Set<Set<T>> {
        val triangles = mutableSetOf<Set<T>>()

        for (node1 in edges.entries) {
            val (node1Label, node1Edges) = node1

            for (node2Label in node1Edges) {
                val node2Edges = edges[node2Label]!!
                val commonEdges = node1Edges.filter { it in node2Edges && it != node1Label && it != node2Label }
                triangles.addAll(commonEdges.map { setOf(node1Label, node2Label, it) })
            }
        }
        return triangles
    }

    private fun <T> Graph<T>.bronKerbosch(
        R: Set<T> = emptySet(),
        P: Set<T> = nodes,
        X: Set<T> = emptySet(),
    ): Set<Set<T>> {
        val R = R.toMutableSet()
        val P = P.toMutableSet()
        val X = X.toMutableSet()

        if (P.isEmpty() && X.isEmpty()) return setOf(R)

        val pivot = (P + X).maxByOrNull { edges[it]?.size ?: 0 } ?: return emptySet()
        val neighbors = edges.getOrDefault(pivot, emptyList()).toSet()
        val toExplore = (P - neighbors)

        return toExplore
            .flatMap { neighbor ->
                val neighborEdges = edges[neighbor].orEmpty()

                val cliques = bronKerbosch(
                    R = R + neighbor,
                    P = P.intersect(neighborEdges),
                    X = X.intersect(neighborEdges),
                )

                P -= neighbor
                X += neighbor

                cliques
            }
            .toSet()
    }

    private fun part1(input: String): Int {
        return parseInput(input)
            .findTriangles()
            .count { triangle ->
                triangle.any { node -> node.startsWith("t", ignoreCase = true) }
            }
    }

    private fun part2(input: String): String {
        return parseInput(input)
            .bronKerbosch()
            .maxBy { it.size }
            .sorted()
            .joinToString(",", "", "")
    }
}