object Day25 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day25_test")
        val input = readInput("Day25")

        println("part 1 test")
        printTimedResult(expectedValue = 3) {
            part1(testInput)
        }

        println("part 1")
        printTimedResult(expectedValue = 2993) {
            part1(input)
        }
    }

    private fun parseInput(input: String): List<Schematics> {
        return input.split("\n\n").map { schematics ->
            val grid = schematics.lines().map { line -> line.toList() }

            if(grid.first().all { it == '#' }) {
                val indexedRows = grid.withIndex()

                Schematics.Lock(
                    List(grid.first().size) { colIndex ->
                        indexedRows
                            .filter { (_, row) -> row[colIndex] == '#' }
                            .maxOf { (rowIndex, _) -> rowIndex }
                    }
                )
            } else if(grid.last().all { it == '#' }) {
                val indexedRows = grid.withIndex()

                Schematics.Key(
                    List(grid.first().size) { colIndex ->
                        indexedRows
                            .filter { (_, row) -> row[colIndex] == '#' }
                            .minOf { (rowIndex, _) -> rowIndex }
                            .let { 5 - it + 1 }
                    }
                )
            } else throw RuntimeException()
        }
    }

    private sealed interface Schematics {
        data class Key(val heights: List<Int>): Schematics
        data class Lock(val heights: List<Int>): Schematics
    }

    private fun Schematics.Key.fits(lock: Schematics.Lock): Boolean {
        return this.heights.zip(lock.heights).map { (a, b) -> a+b }.all { it in 0..5 }
    }

    private fun part1(input: String): Int {
        return parseInput(input).let {
            val keys = it.filterIsInstance<Schematics.Key>()
            val locks = it.filterIsInstance<Schematics.Lock>()

            keys.sumOf { key ->
                locks.count { lock ->
                    key.fits(lock)
                }
            }
        }
    }
}