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
import com.pookie.game.presentation.ui.views.TicTacToeBoardView
import com.pookie.game.presentation.viewmodel.GameViewEffect
import com.pookie.game.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class GameArenaActivity : AppCompatActivity() {

    private val soundManager by lazy { SoundManager(ServiceLocator.preferencesRepository) }
    private val viewModel by lazy {
        GameViewModel(
            getGameStateUseCase = ServiceLocator.getGameStateUseCase,
            makeMoveUseCase = ServiceLocator.makeMoveUseCase,
            sendReactionUseCase = ServiceLocator.sendReactionUseCase,
            requestRematchUseCase = ServiceLocator.requestRematchUseCase,
            preferencesRepository = ServiceLocator.preferencesRepository,
            soundManager = soundManager
        )
    }

    private val gson = Gson()
    private var rematchDialog: AlertDialog? = null

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

        val btnLeaveArena = findViewById<ImageView>(R.id.btnLeaveArena)
        val textArenaRoomCode = findViewById<TextView>(R.id.textArenaRoomCode)
        val btnCopyArenaCode = findViewById<ImageView>(R.id.btnCopyArenaCode)
        val btnShowArenaQr = findViewById<ImageView>(R.id.btnShowArenaQr)
        val btnArenaRematch = findViewById<ImageView>(R.id.btnArenaRematch)

        val dualPvpHud = findViewById<DualPvpHudView>(R.id.dualPvpHud)
        val ticTacToeView = findViewById<TicTacToeBoardView>(R.id.ticTacToeView)
        val dotsAndBoxesView = findViewById<DotsAndBoxesBoardView>(R.id.dotsAndBoxesView)
        val couplesQuizView = findViewById<CouplesQuizView>(R.id.couplesQuizView)

        val btnReactionHeart = findViewById<ImageView>(R.id.btnReactionHeart)
        val btnReactionLightning = findViewById<ImageView>(R.id.btnReactionLightning)
        val btnReactionCrown = findViewById<ImageView>(R.id.btnReactionCrown)

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

        // Live Reaction buttons
        btnReactionHeart.setOnClickListener {
            viewModel.sendReaction(ReactionType.HEART)
        }
        btnReactionLightning.setOnClickListener {
            viewModel.sendReaction(ReactionType.LIGHTNING)
        }
        btnReactionCrown.setOnClickListener {
            viewModel.sendReaction(ReactionType.CROWN)
        }

        // Setup Board Views according to Game Type
        when (initialRoom.gameType) {
            GameType.TIC_TAC_TOE -> {
                ticTacToeView.visibility = View.VISIBLE
                ticTacToeView.setOnCellClickListener { cellIndex ->
                    viewModel.makeMove(cellIndex)
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
        }

        // Observe Room State
        lifecycleScope.launch {
            viewModel.roomState.collect { room ->
                if (room != null) {
                    textArenaRoomCode.text = room.roomCode
                    dualPvpHud.updatePlayers(
                        player1 = room.host,
                        player2 = room.partner,
                        currentTurnPlayerId = room.currentTurn,
                        myPlayerId = viewModel.myProfile.myPlayerId
                    )
                }
            }
        }

        // Observe TicTacToe state
        lifecycleScope.launch {
            viewModel.ticTacToeState.collect { state ->
                val currentRoom = viewModel.roomState.value
                val isMyTurn = currentRoom?.isMyTurn(viewModel.myProfile.myPlayerId) == true
                ticTacToeView.setBoard(state.board, state.winningLine, interactive = isMyTurn)
            }
        }

        // Observe Dots & Boxes state
        lifecycleScope.launch {
            viewModel.dotsAndBoxesState.collect { state ->
                val currentRoom = viewModel.roomState.value
                val isMyTurn = currentRoom?.isMyTurn(viewModel.myProfile.myPlayerId) == true
                dotsAndBoxesView.setState(
                    lines = state.claimedLines,
                    boxes = state.claimedBoxes,
                    hostId = currentRoom?.host?.id ?: "",
                    interactive = isMyTurn
                )
            }
        }

        // Observe Quiz state
        lifecycleScope.launch {
            viewModel.quizState.collect { state ->
                val currentRoom = viewModel.roomState.value
                val isMyTurn = currentRoom?.isMyTurn(viewModel.myProfile.myPlayerId) == true
                couplesQuizView.render(
                    quizState = state,
                    myPlayerId = viewModel.myProfile.myPlayerId,
                    partnerId = currentRoom?.getOpponent(viewModel.myProfile.myPlayerId)?.id,
                    interactive = isMyTurn
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
                    }
                    is GameViewEffect.ShowRematchDialog -> {
                        showRematchDialog(effect.isVictory, effect.isDraw, effect.summary)
                    }
                }
            }
        }

        // Initialize ViewModel with room
        viewModel.initializeRoom(initialRoom)
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
}
