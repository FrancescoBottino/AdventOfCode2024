import java.util.*

fun main() {
    val testInput = readInput("Day05_test")
    val input = readInput("Day05")

    println("Part 1 test")
    printTimedResult(143) {
        part1(testInput)
    }

    println("Part 1")
    printTimedResult(5964) {
        part1(input)
    }

    println("Part 2 test")
    printTimedResult(123) {
        part2(testInput)
    }

    println("Part 2")
    printTimedResult(4719) {
        part2(input)
    }
}

private fun parseInput(input: String): SafetyManual {
    return input.split("\n\n", limit = 2).let { sections ->
        SafetyManual(
            sections[0].split("\n").map { ruleString ->
                ruleString.split("|", limit = 2)
                    .let { PageOrderingRule(it[0].toInt(), it[1].toInt()) }
            },
            sections[1].split("\n").map { updateString ->
                updateString.split(",")
                    .map { it.toInt() }
                    .let { Update(it) }
            },
        )
    }
}

private data class SafetyManual(
    val rules: List<PageOrderingRule>,
    val updates: List<Update>
) {
    val trailingMap = rules.groupBy({ it.leading }, { it.trailing }).mapValues { it.value.toSet() }
    val leadingMap = rules.groupBy({ it.trailing }, { it.leading }).mapValues { it.value.toSet() }
}

private data class PageOrderingRule(
    val leading: Int,
    val trailing: Int,
)

private data class Update(
    val pages: List<Int>
)

private fun <T> List<T>.swapAt(a: Int, b: Int): List<T> {
    return toMutableList().also { Collections.swap(it, a, b) }.toList()
}

private fun List<Int>.getIncorrectPair(trailingMap: Map<Int, Set<Int>>, leadingMap: Map<Int, Set<Int>>): Pair<IndexedValue<Int>, IndexedValue<Int>>? {
    val indexedPages = withIndex().toList()

    indexedPages.forEach { indexedPage ->
        val (index, page) = indexedPage

        val allPossibleTrails = trailingMap[page]
        val allPossibleLeads = leadingMap[page]

        val leadingPages = indexedPages.subList(0, index)
        val trailingPages = indexedPages.subList(index + 1, size)

        //Only one check (on leading pages or trailing pages) is enough, but I'll leave both just for completion’s sake
        val error = leadingPages.firstOrNull { leadingPage ->
            (allPossibleLeads?.let { leadingPage.value !in it } ?: false) || (allPossibleTrails?.let { leadingPage.value in it } ?: false)
        } ?: trailingPages.firstOrNull { trailingPage ->
            (allPossibleLeads?.let { trailingPage.value in it } ?: false) || (allPossibleTrails?.let { trailingPage.value !in it } ?: false)
        }

        error?.let { return indexedPage to it }
    }

    return null
}

private fun List<Int>.getValueIfIncorrect(trailingMap: Map<Int, Set<Int>>, leadingMap: Map<Int, Set<Int>>): Int? {
    var updatedPages = this

    var incorrectPair = updatedPages.getIncorrectPair(trailingMap, leadingMap)
    while(incorrectPair != null) {
        updatedPages = updatedPages.swapAt(incorrectPair.first.index, incorrectPair.second.index)
        incorrectPair = updatedPages.getIncorrectPair(trailingMap, leadingMap)
    }

    if(updatedPages == this)
        return null
    else
        return updatedPages[updatedPages.indices.last / 2]
}

private fun part1(input: String): Int {
    val safetyManuel = parseInput(input)
    return safetyManuel.updates
        .filter { it.pages.getIncorrectPair(safetyManuel.trailingMap, safetyManuel.leadingMap) == null }
        .sumOf { update -> update.pages[update.pages.indices.last / 2] }
}

private fun part2(input: String): Int {
    val safetyManuel = parseInput(input)
    return safetyManuel.updates
        .mapNotNull { it.pages.getValueIfIncorrect(safetyManuel.trailingMap, safetyManuel.leadingMap) }
        .sum()
}
