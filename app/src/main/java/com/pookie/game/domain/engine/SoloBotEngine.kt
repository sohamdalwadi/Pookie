package com.pookie.game.domain.engine

enum class BotBehaviorMode(val displayName: String) {
    SMART("Smart AI"),
    SIMULATE_LOST_CONNECTION("Simulate Disconnection"),
    SIMULATE_TIMEOUT("Simulate Timeout (Idle)"),
    RANDOM_CHAOS("Random Drops & Play")
}

object SoloBotEngine {

    var currentMode: BotBehaviorMode = BotBehaviorMode.SMART

    // -------------------------------------------------------------
    // 1. TIC-TAC-TOE SMART BOT
    // -------------------------------------------------------------
    fun calculateTicTacToeMove(
        board: List<String?>,
        botSymbol: String = "O",
        humanSymbol: String = "X"
    ): Int {
        val winningCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )

        // 1. Check if Bot can win in 1 move
        for (combo in winningCombos) {
            val symbols = combo.map { board[it] }
            if (symbols.count { it == botSymbol } == 2 && symbols.count { it == null } == 1) {
                return combo[symbols.indexOf(null)]
            }
        }

        // 2. Check if Human can win in 1 move -> Block it
        for (combo in winningCombos) {
            val symbols = combo.map { board[it] }
            if (symbols.count { it == humanSymbol } == 2 && symbols.count { it == null } == 1) {
                return combo[symbols.indexOf(null)]
            }
        }

        // 3. Take Center (4)
        if (board[4] == null) return 4

        // 4. Take Opposite Corners or Any Corner (0, 2, 6, 8)
        val corners = listOf(0, 2, 6, 8).filter { board[it] == null }
        if (corners.isNotEmpty()) {
            return corners.random()
        }

        // 5. Take Any Available Edge
        val available = board.indices.filter { board[it] == null }
        return if (available.isNotEmpty()) available.random() else -1
    }

    // -------------------------------------------------------------
    // 2. DOTS & BOXES SMART BOT
    // -------------------------------------------------------------
    fun calculateDotsAndBoxesMove(
        claimedLines: Map<String, String>,
        claimedBoxes: Map<String, String>,
        gridSize: Int
    ): String {
        // Collect all potential lines in the grid
        val allHorizontal = mutableListOf<String>()
        for (r in 0..gridSize) {
            for (c in 0 until gridSize) {
                allHorizontal.add("H_${r}_${c}")
            }
        }
        val allVertical = mutableListOf<String>()
        for (r in 0 until gridSize) {
            for (c in 0..gridSize) {
                allVertical.add("V_${r}_${c}")
            }
        }
        val allLines = allHorizontal + allVertical
        val unclaimedLines = allLines.filter { claimedLines[it] == null }
        if (unclaimedLines.isEmpty()) return ""

        fun getLinesForBox(r: Int, c: Int): List<String> {
            return listOf("H_${r}_${c}", "H_${r + 1}_${c}", "V_${r}_${c}", "V_${r}_${c + 1}")
        }

        // 1. Look for a Box with exactly 3 lines claimed (completing move)
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val boxLines = getLinesForBox(r, c)
                val missing = boxLines.filter { claimedLines[it] == null }
                if (missing.size == 1) {
                    return missing.first() // Claim box immediately!
                }
            }
        }

        // 2. Look for "Safe" lines (lines that DO NOT result in any box having 3 lines claimed)
        val safeLines = unclaimedLines.filter { candidate ->
            val hypotheticalClaims = claimedLines + (candidate to "BOT")
            var createsThirdLine = false
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val boxLines = getLinesForBox(r, c)
                    val claimedCount = boxLines.count { hypotheticalClaims[it] != null }
                    if (claimedCount == 3) {
                        createsThirdLine = true
                        break
                    }
                }
                if (createsThirdLine) break
            }
            !createsThirdLine
        }

        if (safeLines.isNotEmpty()) {
            return safeLines.random()
        }

        // 3. If no completely safe line exists, pick any unclaimed line
        return unclaimedLines.random()
    }

    // -------------------------------------------------------------
    // 3. COUPLES QUIZ BOT
    // -------------------------------------------------------------
    fun calculateQuizMove(questionIndex: Int): Int {
        // High harmony rate in testing
        return (0..3).random()
    }

    // -------------------------------------------------------------
    // 4. SOULSYNC HONEST ANSWER & VERDICT BOT
    // -------------------------------------------------------------
    private val CUTE_HONEST_ANSWERS = listOf(
        "Late night drives with our favorite playlist",
        "Hot chocolate and cozy blankets on the couch",
        "When you laughed so hard your drink almost spilled",
        "Eating street tacos at midnight in the car",
        "The day we got caught in the summer rain together",
        "Trip to Japan to see cherry blossoms and eat ramen",
        "Your contagious laugh when you watch silly cat videos",
        "Binge watching crime documentaries on Sunday mornings",
        "Pizza with extra cheese and garlic crust",
        "When you held my hand for the first time unexpectedly"
    )

    fun calculateSoulSyncAnswer(): String {
        return CUTE_HONEST_ANSWERS.random()
    }

    fun calculateSoulSyncVerdict(): String {
        // 85% match rate for positive reinforcement, 15% miss to test deduction
        return if (Math.random() < 0.85) "MATCH" else "MISS"
    }
}
