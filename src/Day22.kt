import kotlinx.coroutines.*
import kotlin.math.floor

object Day22 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInput1 = readInput("Day22_test1")
        val testInput2 = readInput("Day22_test2")
        val input = readInput("Day22")

        println("part 1 test")
        printTimedResult(expectedValue = 37327623) {
            part1(testInput1)
        }

        println("part 1")
        printTimedResult(expectedValue = 16039090236) {
            part1(input)
        }

        println("part 2 test")
        printTimedResult(expectedValue = 23) {
            part2(testInput2)
        }

        println("part 2")
        printTimedResult(expectedValue = 1808) {
            part2(input)
        }
    }

    @JvmInline
    private value class SecretNumber(val value: Long) {
        override fun toString(): String = value.toString()
    }

    private fun SecretNumber.nextSecretNumber(): SecretNumber {
        fun Long.mix(secret: Long): Long = this xor secret
        fun Long.prune(): Long = this % 16777216

        return this.value
            .let { (it * 64L).mix(it).prune() }
            .let { floor(it.toDouble() / 32.0).toLong().mix(it).prune() }
            .let { (it * 2048).mix(it).prune() }
            .let { SecretNumber(it) }
    }

    private fun SecretNumber.shiftSecretNumber(times: Int = 2000): SecretNumber {
        var current = this
        repeat(times) {
            current = current.nextSecretNumber()
        }
        return current
    }

    private fun part1(input: String, secretNumbersGenerated: Int = 2000): Long {
        return input.lines().sumOf {
            SecretNumber(it.toLong()).shiftSecretNumber(secretNumbersGenerated).value
        }
    }

    private fun SecretNumber.generateSecretNumberSequence(length: Int): List<SecretNumber> =
        generateSequence(this) { it.nextSecretNumber() }
            .take(length + 1)
            .toList()

    @JvmInline
    private value class Cost(val value: Int) {
        override fun toString(): String = value.toString()
    }

    private fun SecretNumber.extractCost(): Cost = Cost(this.value.toInt() % 10)

    @JvmInline
    private value class CostVariance(val value: Int) {
        override fun toString(): String = value.toString()
    }

    private fun List<Cost>.associateWithVariance(): List<Pair<Cost, CostVariance>> {
        val variances = this.zipWithNext().map { CostVariance(it.second.value - it.first.value) }
        return this.drop(1).zip(variances)
    }

    @JvmInline
    private value class VarianceSequence(val values: List<CostVariance>) {
        override fun toString(): String = values.toString()
    }

    private fun List<Pair<Cost, CostVariance>>.pairCostWithSequence(sequenceLength: Int): List<Pair<Cost, VarianceSequence>> {
        val (costs, variances) = this.unzip()
        return this.indices
            .drop(sequenceLength - 1)
            .map { index ->
                costs[index] to VarianceSequence(variances.slice((index - sequenceLength + 1) .. index))
            }
    }

    private fun List<Pair<Cost, VarianceSequence>>.getFirstCostForEachSequence(): Map<VarianceSequence, Cost> {
        val resultMap = mutableMapOf<VarianceSequence, Cost>()

        forEach { (cost, sequence) ->
            resultMap.putIfAbsent(sequence, cost)
        }

        return resultMap
    }

    private fun part2(input: String, secretNumbersGenerated: Int = 2000, sequenceLength: Int = 4): Int {
        return runBlocking {
            val buyers = coroutineScope {
                input.lines()
                    .map { initialSecretNumberString ->
                        async(Dispatchers.Default) {
                            SecretNumber(initialSecretNumberString.toLong())
                                .generateSecretNumberSequence(secretNumbersGenerated)
                                .map { it.extractCost() }
                                .associateWithVariance()
                                .pairCostWithSequence(sequenceLength)
                                .getFirstCostForEachSequence()
                        }
                    }
                    .awaitAll()
            }

            val allSequences = buyers.flatMap { it.keys }.toSet()

            coroutineScope {
                allSequences
                    .map { sequence ->
                        async(Dispatchers.Default) {
                            buyers.sumOf { buyer -> buyer[sequence]?.value ?: 0 }
                        }
                    }
                    .awaitAll()
                    .max()
            }
        }
    }
}