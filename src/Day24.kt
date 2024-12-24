object Day24 {
    @JvmStatic
    fun main(args: Array<String>) {
        val testInputSmall = readInput("Day24_testSmall")
        val testInputSmall2 = readInput("Day24_testSmall2")
        val testInputBig = readInput("Day24_testBig")
        val input = readInput("Day24")

        println("part 1 test small")
        printTimedResult(expectedValue = 4L) {
            part1(testInputSmall)
        }

        println("part 1 test big")
        printTimedResult(expectedValue = 2024L) {
            part1(testInputBig)
        }

        println("part 1")
        printTimedResult(expectedValue = 64755511006320L) {
            part1(input)
        }

        println("part 2 test")
        printTimedResult(expectedValue = "z00,z01,z02,z05") {
            part2(testInputSmall2)
        }

        println("part 2")
        printTimedResult(expectedValue = null) {
            part2(input)
        }
    }

    private fun parseInput(input: String): Circuit {
        return input.split("\n\n", limit = 2).let { (wires, gates) ->
            Circuit(
                wires.split("\n").associate { wireDefinitionString ->
                    wireDefinitionString.split(": ", limit = 2).let { (label, value) ->
                        label to value.toBinaryBoolean()
                    }
                },
                gates.split("\n").map { gateConnectionsString ->
                    gateConnectionsString.split(" -> ", limit = 2).let { (input, output) ->
                        input.split(" ", limit = 3).let { (a, gate, b) ->
                            GateConnections(a, b, gate.parseGate(), output)
                        }
                    }
                }
            )
        }
    }

    private fun String.toBinaryBoolean(): Boolean {
        return this.toInt().let {
            when(it) {
                1-> true
                0 -> false
                else -> throw RuntimeException()
            }
        }
    }

    private fun String.parseGate(): Gate {
        return when(this) {
            "AND" -> Gate.AND
            "OR" -> Gate.OR
            "XOR" -> Gate.XOR
            else -> throw RuntimeException()
        }
    }

    private data class Circuit(
        val initialWiresValues: Map<String, Boolean>,
        val gates: List<GateConnections>,
    )

    private data class GateConnections(
        val a: String,
        val b: String,
        val gate: Gate,
        val output: String
    )

    private sealed class Gate(
        val label: String,
    ) {
        abstract fun compute(a: Boolean, b: Boolean): Boolean
        data object OR: Gate("OR") {
            override fun compute(a: Boolean, b: Boolean): Boolean = a or b
        }
        data object AND: Gate("AND") {
            override fun compute(a: Boolean, b: Boolean): Boolean = a and b
        }
        data object XOR: Gate("XOR") {
            override fun compute(a: Boolean, b: Boolean): Boolean = a xor b
        }
    }

    private fun Circuit.run(): Map<String, Boolean> {
        val wires = initialWiresValues.toMutableMap()
        val gates = gates.toMutableSet()

        while(gates.isNotEmpty()) {
            gates
                .filter { it.a in wires.keys && it.b in wires.keys }
                .toSet()
                .onEach { gateConnection ->
                    val a = wires[gateConnection.a]!!
                    val b = wires[gateConnection.b]!!
                    val output = gateConnection.gate.compute(a, b)

                    wires[gateConnection.output] = output
                }
                .run gates@ { gates.removeAll(this@gates) }
        }

        return wires
    }

    private fun Map<String, Boolean>.extractWireValueOf(labelStart: Char): Long {
        return filter { it.key.startsWith(labelStart) }
            .entries
            .sortedByDescending { it.key }
            .map { if(it.value) 1 else 0 }
            .joinToString("","","")
            .toLong(2)
    }

    private fun part1(input: String): Long {
        return parseInput(input)
            .run()
            .extractWireValueOf('z')
    }

    private fun part2(input: String): String {
        TODO("Not yet implemented")
    }
}