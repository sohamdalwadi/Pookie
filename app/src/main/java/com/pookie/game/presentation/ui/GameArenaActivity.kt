package com.pookie.game.presentation.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.pookie.game.R
import com.pookie.game.di.ServiceLocator
import com.pookie.game.domain.engine.QuizAnswerMove
import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.model.Room
import com.pookie.game.presentation.common.QrCodeGenerator
import com.pookie.game.presentation.common.SoundManager
import com.pookie.game.presentation.ui.views.CouplesQuizView
import com.pookie.game.presentation.ui.views.DotsAndBoxesBoardView
import com.pookie.game.presentation.ui.views.DualPvpHudView
import com.pookie.game.presentation.ui.views.SoulSyncView
import com.pookie.game.presentation.ui.views.TicTacToeBoardView
import com.pookie.game.presentation.viewmodel.GameViewEffect
import com.pookie.game.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameArenaActivity : AppCompatActivity() {

    private val soundManager by lazy { SoundManager(ServiceLocator.preferencesRepository) }
    private val viewModel by lazy {
        GameViewModel(
            getGameStateUseCase = ServiceLocator.getGameStateUseCase,
            makeMoveUseCase = ServiceLocator.makeMoveUseCase,
            sendReactionUseCase = ServiceLocator.sendReactionUseCase,
            requestRematchUseCase = ServiceLocator.requestRematchUseCase,
            submitVerdictUseCase = ServiceLocator.submitVerdictUseCase,
            allowEvaUseCase = ServiceLocator.allowEvaUseCase,
            preferencesRepository = ServiceLocator.preferencesRepository,
            soundManager = soundManager
        )
    }

    private val gson = Gson()
    private var rematchDialog: AlertDialog? = null
    private var disconnectDialog: AlertDialog? = null
    private var evaPromptDialog: AlertDialog? = null
    private var waitingTimerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_arena)

        val roomJson = intent.getStringExtra("ROOM_JSON")
        if (roomJson.isNullOrBlank()) {
            Toast.makeText(this, "Room data missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val initialRoom = gson.fromJson(roomJson, Room::class.java)
        val isSolo = intent.getBooleanExtra("IS_SOLO_TEST_MODE", false)
        viewModel.isSoloTestMode = isSolo

        val effectiveRoom = if (isSolo && initialRoom.partner == null) {
            val botGender = if (initialRoom.host.gender == com.pookie.game.domain.model.Gender.MALE)
                com.pookie.game.domain.model.Gender.FEMALE else com.pookie.game.domain.model.Gender.MALE
            initialRoom.copy(
                status = com.pookie.game.domain.model.GameStatus.IN_PROGRESS,
                partner = com.pookie.game.domain.model.Player(
                    id = "P_PartnerBot",
                    name = "Pookie Bot",
                    score = 0,
                    gender = botGender
                )
            )
        } else {
            initialRoom
        }

        val btnLeaveArena = findViewById<ImageView>(R.id.btnLeaveArena)
        val textArenaRoomCode = findViewById<TextView>(R.id.textArenaRoomCode)
        val btnCopyArenaCode = findViewById<ImageView>(R.id.btnCopyArenaCode)
        val btnShowArenaQr = findViewById<ImageView>(R.id.btnShowArenaQr)
        val btnArenaRematch = findViewById<ImageView>(R.id.btnArenaRematch)
        val btnBotModeSelector = findViewById<TextView>(R.id.btnBotModeSelector)

        if (viewModel.isSoloTestMode) {
            btnBotModeSelector.visibility = View.VISIBLE
            btnBotModeSelector.text = "🤖 Bot: ${com.pookie.game.domain.engine.SoloBotEngine.currentMode.displayName}"
            btnBotModeSelector.setOnClickListener {
                val modes = com.pookie.game.domain.engine.BotBehaviorMode.values()
                val items = modes.map { it.displayName }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Select Solo Bot Behavior")
                    .setItems(items) { _, which ->
                        val selected = modes[which]
                        com.pookie.game.domain.engine.SoloBotEngine.currentMode = selected
                        btnBotModeSelector.text = "🤖 Bot: ${selected.displayName}"
                        Toast.makeText(this, "Bot Mode: ${selected.displayName}", Toast.LENGTH_SHORT).show()
                        viewModel.triggerBotTurnIfNeeded()
                    }
                    .show()
            }
        }

        val dualPvpHud = findViewById<DualPvpHudView>(R.id.dualPvpHud)
        val ticTacToeView = findViewById<TicTacToeBoardView>(R.id.ticTacToeView)
        val dotsAndBoxesView = findViewById<DotsAndBoxesBoardView>(R.id.dotsAndBoxesView)
        val couplesQuizView = findViewById<CouplesQuizView>(R.id.couplesQuizView)
        val soulSyncView = findViewById<SoulSyncView>(R.id.soulSyncView)

        val layoutWaitingForPartner = findViewById<View>(R.id.layoutWaitingForPartner)
        val textWaitingRoomCode = findViewById<TextView>(R.id.textWaitingRoomCode)
        val btnWaitingCopyCode = findViewById<Button>(R.id.btnWaitingCopyCode)
        val btnWaitingShowQr = findViewById<Button>(R.id.btnWaitingShowQr)
        val textWaitingTimer = findViewById<TextView>(R.id.textWaitingTimer)
        val btnPlayWithEva = findViewById<Button>(R.id.btnPlayWithEva)

        textWaitingRoomCode.text = initialRoom.roomCode

        btnWaitingCopyCode.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Room Code", initialRoom.roomCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.code_copied, Toast.LENGTH_SHORT).show()
        }

        btnWaitingShowQr.setOnClickListener {
            showQrDialog(initialRoom.roomCode)
        }

        btnPlayWithEva.setOnClickListener {
            showEvaConfirmationDialog()
        }

        val btnReactionHeart = findViewById<ImageView>(R.id.btnReactionHeart)
        val btnReactionThumbsUp = findViewById<ImageView>(R.id.btnReactionThumbsUp)
        val btnNudge = findViewById<ImageView>(R.id.btnNudge)
        val btnReactionThumbsDown = findViewById<ImageView>(R.id.btnReactionThumbsDown)
        val btnReactionLightning = findViewById<ImageView>(R.id.btnReactionLightning)

        textArenaRoomCode.text = initialRoom.roomCode

        // Top bar buttons
        btnLeaveArena.setOnClickListener {
            finish()
        }

        btnCopyArenaCode.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Room Code", initialRoom.roomCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.code_copied, Toast.LENGTH_SHORT).show()
        }

        btnShowArenaQr.setOnClickListener {
            showQrDialog(initialRoom.roomCode)
        }

        btnArenaRematch.setOnClickListener {
            viewModel.requestRematch()
        }

        // 10-Second Turn Timeout Hook
        dualPvpHud.onTurnTimeout = {
            viewModel.passTurnOnTimeout()
        }

        // Live Reaction buttons & Middle Nudge Button (5-second cooldown with grayed out state)
        var isReactionCooldown = false
        val reactionButtons = listOf(
            btnReactionHeart,
            btnReactionThumbsUp,
            btnNudge,
            btnReactionThumbsDown,
            btnReactionLightning
        )

        fun triggerReactionWithCooldown(reactionType: ReactionType) {
            if (isReactionCooldown) return
            isReactionCooldown = true

            // Gray out reaction buttons during 5s cooldown
            reactionButtons.forEach { btn ->
                btn.isEnabled = false
                btn.animate().alpha(0.35f).setDuration(150).start()
            }

            if (reactionType == ReactionType.NUDGE) {
                btnNudge.animate().scaleX(1.3f).scaleY(1.3f).setDuration(120).withEndAction {
                    btnNudge.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                }.start()

                // Self vibrate immediately on tap safely
                try {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(120, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(120)
                    }
                } catch (_: Exception) {}
            }

            viewModel.sendReaction(reactionType)

            lifecycleScope.launch {
                kotlinx.coroutines.delay(5000L)
                reactionButtons.forEach { btn ->
                    btn.isEnabled = true
                    btn.animate().alpha(1.0f).setDuration(200).start()
                }
                isReactionCooldown = false
            }
        }

        btnReactionHeart.setOnClickListener { triggerReactionWithCooldown(ReactionType.HEART) }
        btnReactionThumbsUp.setOnClickListener { triggerReactionWithCooldown(ReactionType.THUMBS_UP) }
        btnNudge.setOnClickListener { triggerReactionWithCooldown(ReactionType.NUDGE) }
        btnReactionThumbsDown.setOnClickListener { triggerReactionWithCooldown(ReactionType.THUMBS_DOWN) }
        btnReactionLightning.setOnClickListener { triggerReactionWithCooldown(ReactionType.LIGHTNING) }

        // Setup Board Views according to Game Type
        when (initialRoom.gameType) {
            GameType.TIC_TAC_TOE -> {
                ticTacToeView.visibility = View.VISIBLE
                ticTacToeView.setOnCellClickListener { cellIndex ->
                    viewModel.makeMove(mapOf("cellIndex" to cellIndex))
                }
            }
            GameType.DOTS_AND_BOXES -> {
                dotsAndBoxesView.visibility = View.VISIBLE
                dotsAndBoxesView.setOnLineClickListener { lineKey ->
                    viewModel.makeMove(lineKey)
                }
            }
            GameType.COUPLES_QUIZ -> {
                couplesQuizView.visibility = View.VISIBLE
                couplesQuizView.setOnOptionSelectedListener { optionIndex ->
                    val currentState = viewModel.quizState.value
                    viewModel.makeMove(
                        QuizAnswerMove(currentState.currentQuestionIndex, optionIndex)
                    )
                }
            }
            GameType.SOUL_SYNC -> {
                soulSyncView.visibility = View.VISIBLE
                soulSyncView.setOnSubmitAnswerListener { text ->
                    viewModel.submitSoulSyncAnswer(text)
                }
                soulSyncView.setOnSubmitVerdictListener { verdict ->
                    viewModel.submitSoulSyncVerdict(verdict)
                }
            }
        }

        var previousPartner: com.pookie.game.domain.model.Player? = initialRoom.partner

        // Observe Room State
        lifecycleScope.launch {
            viewModel.roomState.collect { room ->
                if (room != null) {
                    textArenaRoomCode.text = room.roomCode
                    textWaitingRoomCode.text = room.roomCode

                    val isWaiting = room.partner == null
                    layoutWaitingForPartner.visibility = if (isWaiting) View.VISIBLE else View.GONE

                    if (isWaiting) {
                        if (waitingTimerJob == null || waitingTimerJob?.isActive != true) {
                            startWaitingTimer(textWaitingTimer)
                        }
                    } else {
                        waitingTimerJob?.cancel()
                        waitingTimerJob = null
                        evaPromptDialog?.dismiss()
                    }

                    if (previousPartner == null && room.partner != null) {
                        soundManager.playReactionSound()
                        Toast.makeText(this@GameArenaActivity, "${room.partner.name} joined! Game starting!", Toast.LENGTH_SHORT).show()
                    }
                    previousPartner = room.partner

                    reactionButtons.forEach { btn ->
                        btn.isEnabled = !isWaiting && !isReactionCooldown
                        btn.alpha = if (!isWaiting && !isReactionCooldown) 1.0f else 0.35f
                    }
                    btnArenaRematch.isEnabled = !isWaiting

                    val myId = if (viewModel.isSoloTestMode) room.host.id else viewModel.myProfile.myPlayerId
                    dualPvpHud.updatePlayers(
                        player1 = room.host,
                        player2 = room.partner,
                        currentTurnPlayerId = room.currentTurn,
                        myPlayerId = myId
                    )

                    if (!isWaiting) {
                        val isMyTurn = room.isMyTurn(myId)
                        when (room.gameType) {
                            GameType.TIC_TAC_TOE -> {
                                ticTacToeView.visibility = View.VISIBLE
                                val state = viewModel.ticTacToeState.value
                                ticTacToeView.setBoard(state.board, state.winningLine, interactive = isMyTurn)
                            }
                            GameType.DOTS_AND_BOXES -> {
                                dotsAndBoxesView.visibility = View.VISIBLE
                                val state = viewModel.dotsAndBoxesState.value
                                dotsAndBoxesView.setState(
                                    lines = state.claimedLines,
                                    boxes = state.claimedBoxes,
                                    boxNumbers = state.boxNumbers,
                                    hostId = room.host.id,
                                    interactive = isMyTurn,
                                    gridSize = state.gridSize
                                )
                            }
                            GameType.COUPLES_QUIZ -> {
                                couplesQuizView.visibility = View.VISIBLE
                                val state = viewModel.quizState.value
                                couplesQuizView.render(
                                    quizState = state,
                                    myPlayerId = myId,
                                    partnerId = room.getOpponent(myId)?.id,
                                    interactive = isMyTurn
                                )
                            }
                            GameType.SOUL_SYNC -> {
                                soulSyncView.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        ticTacToeView.visibility = View.GONE
                        dotsAndBoxesView.visibility = View.GONE
                        couplesQuizView.visibility = View.GONE
                        soulSyncView.visibility = View.GONE
                    }
                }
            }
        }

        // Observe TicTacToe state
        lifecycleScope.launch {
            viewModel.ticTacToeState.collect { state ->
                val currentRoom = viewModel.roomState.value ?: return@collect
                if (currentRoom.partner == null) return@collect
                val myId = if (viewModel.isSoloTestMode) currentRoom.host.id else viewModel.myProfile.myPlayerId
                val isMyTurn = currentRoom.isMyTurn(myId)
                ticTacToeView.setBoard(state.board, state.winningLine, interactive = isMyTurn)
            }
        }

        // Observe Dots & Boxes state
        lifecycleScope.launch {
            viewModel.dotsAndBoxesState.collect { state ->
                val currentRoom = viewModel.roomState.value ?: return@collect
                if (currentRoom.partner == null) return@collect
                val myId = if (viewModel.isSoloTestMode) currentRoom.host.id else viewModel.myProfile.myPlayerId
                val isMyTurn = currentRoom.isMyTurn(myId)
                dotsAndBoxesView.setState(
                    lines = state.claimedLines,
                    boxes = state.claimedBoxes,
                    boxNumbers = state.boxNumbers,
                    hostId = currentRoom.host.id,
                    interactive = isMyTurn,
                    gridSize = state.gridSize
                )
            }
        }

        // Observe Quiz state
        lifecycleScope.launch {
            viewModel.quizState.collect { state ->
                val currentRoom = viewModel.roomState.value ?: return@collect
                if (currentRoom.partner == null) return@collect
                val myId = if (viewModel.isSoloTestMode) currentRoom.host.id else viewModel.myProfile.myPlayerId
                val isMyTurn = currentRoom.isMyTurn(myId)
                couplesQuizView.render(
                    quizState = state,
                    myPlayerId = myId,
                    partnerId = currentRoom.getOpponent(myId)?.id,
                    interactive = isMyTurn
                )
            }
        }

        // Observe SoulSync state
        lifecycleScope.launch {
            viewModel.soulSyncState.collect { state ->
                val currentRoom = viewModel.roomState.value ?: return@collect
                if (currentRoom.partner == null) return@collect
                val isHost = if (viewModel.isSoloTestMode) true else (currentRoom.host.id == viewModel.myProfile.myPlayerId)
                soulSyncView.render(
                    state = state,
                    isHost = isHost,
                    hostName = currentRoom.host.name,
                    partnerName = currentRoom.partner?.name ?: "Partner"
                )
            }
        }

        // Observe Effects
        lifecycleScope.launch {
            viewModel.viewEffect.collect { effect ->
                when (effect) {
                    is GameViewEffect.ShowToast -> {
                        Toast.makeText(this@GameArenaActivity, effect.message, Toast.LENGTH_SHORT).show()
                    }
                    is GameViewEffect.PlayReactionAnimation -> {
                        dualPvpHud.triggerReaction(effect.isPlayer1, effect.reactionType)
                        if (effect.reactionType == ReactionType.NUDGE) {
                            try {
                                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(longArrayOf(0, 150, 100, 150), -1)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                    is GameViewEffect.ShowRematchDialog -> {
                        showRematchDialog(effect.isVictory, effect.isDraw, effect.summary)
                    }
                    is GameViewEffect.ShowConnectionLostDialog -> {
                        if (effect.isDisconnected) {
                            if (disconnectDialog == null || !disconnectDialog!!.isShowing) {
                                disconnectDialog = AlertDialog.Builder(this@GameArenaActivity)
                                    .setTitle("Connection Lost")
                                    .setMessage(effect.message)
                                    .setCancelable(true)
                                    .setPositiveButton("Dismiss", null)
                                    .show()
                            }
                        } else {
                            disconnectDialog?.dismiss()
                            disconnectDialog = null
                            Toast.makeText(this@GameArenaActivity, "Connected back to partner!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Initialize ViewModel with room
        viewModel.initializeRoom(effectiveRoom)
    }

    private fun showQrDialog(roomCode: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null)
        val textQrRoomCode = dialogView.findViewById<TextView>(R.id.textQrRoomCode)
        val imgQrCode = dialogView.findViewById<ImageView>(R.id.imgQrCode)
        val btnCloseQr = dialogView.findViewById<Button>(R.id.btnCloseQr)

        textQrRoomCode.text = roomCode

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        lifecycleScope.launch {
            val bitmap = QrCodeGenerator.generateQrBitmap(roomCode)
            imgQrCode.setImageBitmap(bitmap)
        }

        btnCloseQr.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRematchDialog(isVictory: Boolean, isDraw: Boolean, summary: String) {
        if (rematchDialog?.isShowing == true) return

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rematch, null)
        val imgMatchResult = dialogView.findViewById<ImageView>(R.id.imgMatchResult)
        val textMatchResultTitle = dialogView.findViewById<TextView>(R.id.textMatchResultTitle)
        val textScrapbookPlayers = dialogView.findViewById<TextView>(R.id.textScrapbookPlayers)
        val textScrapbookSummary = dialogView.findViewById<TextView>(R.id.textScrapbookSummary)
        val btnDialogRematch = dialogView.findViewById<Button>(R.id.btnDialogRematch)
        val btnDialogLeave = dialogView.findViewById<Button>(R.id.btnDialogLeave)

        textMatchResultTitle.text = when {
            isDraw -> getString(R.string.result_draw)
            isVictory -> getString(R.string.result_victory)
            else -> getString(R.string.result_defeat)
        }

        imgMatchResult.setImageResource(
            if (isVictory) R.drawable.ic_trophy else R.drawable.ic_reaction_heart
        )

        val room = viewModel.roomState.value
        val hostName = room?.host?.name ?: "Player 1"
        val partnerName = room?.partner?.name ?: "Player 2"
        textScrapbookPlayers.text = "$hostName  &  $partnerName"
        textScrapbookSummary.text = summary

        // Dynamically award couple vibe tag based on game activity
        if (room != null) {
            val currentProfile = ServiceLocator.preferencesRepository.getCoupleProfile()
            val matchScore = if (room.gameType == GameType.COUPLES_QUIZ) viewModel.quizState.value.matchScore else 1
            val newTag = com.pookie.game.domain.engine.VibeTagEvaluator.evaluateTag(
                gameType = room.gameType,
                isVictory = isVictory,
                isDraw = isDraw,
                matchScore = matchScore,
                currentTag = currentProfile.vibeTag
            )
            if (newTag != currentProfile.vibeTag) {
                ServiceLocator.preferencesRepository.saveCoupleProfile(currentProfile.copy(vibeTag = newTag))
                Toast.makeText(this, "New Couple Tag Earned: $newTag", Toast.LENGTH_SHORT).show()
            }
        }

        rematchDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnDialogRematch.setOnClickListener {
            viewModel.requestRematch()
            rematchDialog?.dismiss()
        }

        btnDialogLeave.setOnClickListener {
            rematchDialog?.dismiss()
            finish()
        }

        rematchDialog?.show()
    }

    private fun showEvaConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Play with Eva")
            .setMessage("Would you like Eva (AI) to join the room and play with you?")
            .setPositiveButton("Allow Eva") { _, _ ->
                viewModel.allowEva()
            }
            .setNegativeButton("Keep Waiting", null)
            .show()
    }

    private fun showEvaPromptDialog(textWaitingTimer: TextView) {
        if (evaPromptDialog?.isShowing == true) return
        evaPromptDialog = AlertDialog.Builder(this)
            .setTitle("Partner has not joined")
            .setMessage("Your partner has not joined yet after 2 minutes. Would you like Eva (AI) to join the room and play with you?")
            .setPositiveButton("Allow Eva") { _, _ ->
                viewModel.allowEva()
            }
            .setNegativeButton("Wait Longer") { _, _ ->
                startWaitingTimer(textWaitingTimer)
            }
            .setCancelable(true)
            .show()
    }

    private fun startWaitingTimer(textWaitingTimer: TextView) {
        waitingTimerJob?.cancel()
        waitingTimerJob = lifecycleScope.launch {
            var remainingSeconds = 120
            while (remainingSeconds > 0) {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                textWaitingTimer.text = String.format("Auto-prompt in: %02d:%02d", minutes, seconds)
                delay(1000L)
                remainingSeconds--
            }
            textWaitingTimer.text = "Partner missing"
            if (viewModel.roomState.value?.partner == null && !isFinishing && !isDestroyed) {
                showEvaPromptDialog(textWaitingTimer)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        waitingTimerJob?.cancel()
        evaPromptDialog?.dismiss()
        rematchDialog?.dismiss()
        disconnectDialog?.dismiss()
    }
}
