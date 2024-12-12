import kotlin.math.roundToInt

object Day09 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput1 = readInput("Day09_test1")
        val testInput2 = readInput("Day09_test2")
        val input = readInput("Day09")

        println("Part 1 test 1")
        printTimedResult(expectedValue = 60) {
            part1(testInput1)
        }

        println("Part 1 test 2")
        printTimedResult(expectedValue = 1928) {
            part1(testInput2)
        }

        println("Part 1")
        printTimedResult(expectedValue = 6341711060162) {
            part1(input)
        }

        println("Part 2 test 1")
        printTimedResult(expectedValue = 132) {
            part2(testInput1)
        }

        println("Part 2 test 2")
        printTimedResult(expectedValue = 2858) {
            part2(testInput2)
        }

        println("Part 2")
        printTimedResult(expectedValue = 6377400869326) {
            part2(input)
        }
    }

    private fun parseInput(input: String): List<MemoryCellBlock> {
        var nextIsFile = true
        var fileId = 0

        return input.map { char ->
            val size = char.digitToInt()
            val isFile = nextIsFile
            nextIsFile = !nextIsFile

            if(isFile) {
                MemoryCellBlock.File(size, fileId++)
            } else {
                MemoryCellBlock.FreeSpace(size)
            }
        }
    }

    private sealed interface MemoryCellBlock {
        val size: Int

        data class FreeSpace(override val size: Int): MemoryCellBlock
        data class File(override val size: Int, val id: Int): MemoryCellBlock
    }

    private fun List<MemoryCellBlock>.rearrangeCells(): List<MemoryCellBlock> {
        val memory = toMutableList()
        var firstFreeMemoryCellIndex: Int? = memory.indexOfFirst { it is MemoryCellBlock.FreeSpace }

        while(firstFreeMemoryCellIndex != null) {
            val freeSpace = memory[firstFreeMemoryCellIndex] as MemoryCellBlock.FreeSpace
            val file = memory[memory.lastIndex] as MemoryCellBlock.File

            when {
                freeSpace.size > file.size -> {
                    memory.removeLast()
                    memory[firstFreeMemoryCellIndex] = freeSpace.copy(size = freeSpace.size - file.size)
                    memory.add(firstFreeMemoryCellIndex, file)
                }
                freeSpace.size < file.size -> {
                    memory[memory.lastIndex] = file.copy(size = file.size - freeSpace.size)
                    memory[firstFreeMemoryCellIndex] = file.copy(size = freeSpace.size)
                }
                //freeSpaceBlock.size == fileBlock.size
                else -> {
                    memory.removeLast()
                    memory[firstFreeMemoryCellIndex] = file.copy(size = freeSpace.size)
                }
            }

            while(memory.last() is MemoryCellBlock.FreeSpace) {
                memory.removeLast()
            }

            firstFreeMemoryCellIndex = memory.subList(firstFreeMemoryCellIndex, memory.size)
                .indexOfFirst { it is MemoryCellBlock.FreeSpace }
                .takeIf { it != -1 }
                ?.let { it + firstFreeMemoryCellIndex!! }
        }

        return memory
    }

    private fun List<MemoryCellBlock>.rearrangeWholeBlocks(): List<MemoryCellBlock> {
        val memory = toMutableList()
        var currentFile = memory
            .indexOfLast { it is MemoryCellBlock.File }
            .takeIf { it != -1 }
            ?.let { IndexedValue(it, (memory[it] as MemoryCellBlock.File)) }

        while(currentFile != null) {
            val fileIndex = currentFile.index
            val file = currentFile.value

            val indexedFreeSpace = memory
                .subList(0, fileIndex)
                .indexOfFirst { it is MemoryCellBlock.FreeSpace && it.size >= file.size }
                .takeIf { it != -1 }
                ?.let { IndexedValue(it, (memory[it] as MemoryCellBlock.FreeSpace)) }

            if(indexedFreeSpace != null) {
                val (freeSpaceIndex, freeSpace) = indexedFreeSpace
                memory[freeSpaceIndex] = freeSpace.copy(size = freeSpace.size - file.size)
                memory[fileIndex] = MemoryCellBlock.FreeSpace(file.size)
                memory.add(freeSpaceIndex, file)
            }

            currentFile = memory
                .subList(0, fileIndex)
                .indexOfLast { it is MemoryCellBlock.File }
                .takeIf { it != -1 }
                ?.let { IndexedValue(it, (memory[it] as MemoryCellBlock.File)) }
        }

        return memory
    }

    private fun List<MemoryCellBlock>.checksum(): Long {
        return fold(0 to 0L) { (cumulativeIndex, total), block ->
            val blockTotal = when (block) {
                is MemoryCellBlock.FreeSpace -> 0
                is MemoryCellBlock.File -> {
                    val firstIndex = cumulativeIndex
                    val lastIndex = cumulativeIndex + block.size - 1
                    val indexesSum = ((block.size / 2f) * (firstIndex + lastIndex)).roundToInt()

                    block.id.toLong() * indexesSum
                }
            }

            (cumulativeIndex + block.size) to (total + blockTotal)
        }.second
    }

    private fun part1(input: String): Long {
        return parseInput(input)
            .rearrangeCells()
            .checksum()
    }

    private fun part2(input: String): Long {
        return parseInput(input)
            .rearrangeWholeBlocks()
            .checksum()
    }
}
