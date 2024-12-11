object Day11 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput = readInput("Day11_test")
        val input = readInput("Day11")

        println("Part 1 test")
        printTimedResult(55312) {
            countStonesAfterBlinks(stones = parseInput(testInput), blinks = 25)
        }

        println("Part 1")
        printTimedResult(212655) {
            countStonesAfterBlinks(stones = parseInput(input), blinks = 25)
        }

        println("Part 2")
        printTimedResult(253582809724830) {
            countStonesAfterBlinks(stones = parseInput(input), blinks = 75)
        }
    }

    private fun parseInput(input: String): List<Long> {
        return input.trim().split(" ").map(String::toLong)
    }

    private fun countStonesAfterBlinks(stones: List<Long>, blinks: Int): Long {
        val morphMemo = mutableMapOf<Long, List<Long>>()
        val resultMemo = mutableMapOf<Pair<Long, Int>, Long>()

        fun blinkRecursive(stone: Long, remainingBlinks: Int): Long {
            if(remainingBlinks == 0) return 1

            resultMemo[stone to remainingBlinks]?.run { return this }

            return (morphMemo[stone] ?: getStoneMorph(stone).also { morphMemo[stone] = it })
                .sumOf { stoneMorph -> blinkRecursive(stoneMorph, remainingBlinks - 1) }
                .also { amount -> resultMemo[stone to remainingBlinks] = amount }
        }

        return stones.sumOf { stone -> blinkRecursive(stone, blinks) }
    }

    private fun getStoneMorph(stone: Long): List<Long> {
        if(stone == 0L) {
            return listOf(1L)
        }

        val stoneString = stone.toString()
        if(stoneString.length % 2 == 0) {
            return listOf(
                stoneString.substring(0, stoneString.length / 2).toLong(),
                stoneString.substring(stoneString.length / 2, stoneString.length).toLong()
            )
        }

        return listOf(stone * 2024L)
    }
}
