package games.tictactoe

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import games.media.SoundPlayer
import games.preferences.Settings
import kotlinx.coroutines.*
import java.util.*

/**
 * Cria a interface gráfica do jogo da velha. A Activity Implementa a interface [GameListener]
 * para tratar eventos do jogo como partida finalizada com vitória e tabuleiro cheio.
 * @see GameListener
 */
class TicTacToeActivity : AppCompatActivity(), GameListener {

    /**Marcador 'X'.*/
    private val LABEL_X: Char = 'X'
    /**Marcador 'O'.*/
    private val LABEL_O: Char = 'O'
    /**Chave nível de dificuldade.*/
    private val KEY_DIFFICULT_LEVEL: String = "difficulty_level"
    /**Chave auto-start.*/
    private val KEY_SOUND_EFFECT: String = "sound_effect"

    /**Jogador humano.*/
    private lateinit var humanPlayer: HumanPlayer
    /**Jogador simulado.*/
    private lateinit var bot: Bot
    /**Controlador do jogo.*/
    private lateinit var controller: GameController

    /**Tocador de efeitos sonoros.*/
    private lateinit var soundPlayer: SoundPlayer
    /**Configurações da Activity.*/
    private lateinit var settings: Settings
    /**Status de efeito sonoro nas jogadas.*/
    private var soundEffect: Boolean = false

    private lateinit var sbDifficultyLevel: SeekBar
    private lateinit var imvHuman: ImageView
    private lateinit var imvArrow1: ImageView
    private lateinit var imvBot: ImageView
    private lateinit var imvArrow2: ImageView
    private lateinit var imvCell00: ImageView
    private lateinit var imvCell01: ImageView
    private lateinit var imvCell02: ImageView
    private lateinit var imvCell10: ImageView
    private lateinit var imvCell11: ImageView
    private lateinit var imvCell12: ImageView
    private lateinit var imvCell20: ImageView
    private lateinit var imvCell21: ImageView
    private lateinit var imvCell22: ImageView
    private lateinit var tvHumanScore: TextView
    private lateinit var tvBotScore: TextView
    private lateinit var tvMatchNumber: TextView


//MAIN ACTIVITY EVENTS______________________________________________________________________________

    /**
     * Ao criar a Activity, obtém os objetos de interface gráfica do usuário e instancia os
     * jogadores e o controlador do jogo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac_toe)
        soundPlayer = SoundPlayer(this)
        settings =  Settings(this)
        //Instancia os jogadores e o controlador do jogo.
        humanPlayer = HumanPlayer(LABEL_X.code.toByte())
        bot = Bot(LABEL_O.code.toByte(), LABEL_X.code.toByte(), DifficultyLevel.INVINCIBLE)
        controller = GameController(humanPlayer, bot, arrayOf(this))
        //Obtém os componentes de interface gráfica de usuário de interesse.
        sbDifficultyLevel = findViewById<SeekBar>(R.id.sbDifficulty)
        imvHuman = findViewById<ImageView>(R.id.imvHuman)
        imvArrow1 = findViewById<ImageView>(R.id.imvArrow1)
        imvBot = findViewById<ImageView>(R.id.imvBot)
        imvArrow2 = findViewById<ImageView>(R.id.imvArrow2)
        imvCell00 = findViewById<ImageView>(R.id.imvCell00)
        imvCell01 = findViewById<ImageView>(R.id.imvCell01)
        imvCell02 = findViewById<ImageView>(R.id.imvCell02)
        imvCell10 = findViewById<ImageView>(R.id.imvCell10)
        imvCell11 = findViewById<ImageView>(R.id.imvCell11)
        imvCell12 = findViewById<ImageView>(R.id.imvCell12)
        imvCell20 = findViewById<ImageView>(R.id.imvCell20)
        imvCell21 = findViewById<ImageView>(R.id.imvCell21)
        imvCell22 = findViewById<ImageView>(R.id.imvCell22)
        tvHumanScore = findViewById<TextView>(R.id.tvHumanScore)
        tvBotScore = findViewById<TextView>(R.id.tvBotScore)
        tvMatchNumber = findViewById<TextView>(R.id.tvDrawNumber)
        loadSettings() //Recuperar as configurações dos componentes.
        //Atribui os tratadores de eventos.
        imvCell00.setOnClickListener{boardCellClick(CellPosition(0,0))}
        imvCell01.setOnClickListener{boardCellClick(CellPosition(0,1))}
        imvCell02.setOnClickListener{boardCellClick(CellPosition(0,2))}
        imvCell10.setOnClickListener{boardCellClick(CellPosition(1,0))}
        imvCell11.setOnClickListener{boardCellClick(CellPosition(1,1))}
        imvCell12.setOnClickListener{boardCellClick(CellPosition(1,2))}
        imvCell20.setOnClickListener{boardCellClick(CellPosition(2,0))}
        imvCell21.setOnClickListener{boardCellClick(CellPosition(2,1))}
        imvCell22.setOnClickListener{boardCellClick(CellPosition(2,2))}
        //Atribui o popup para clique longo.
        imvCell00.setOnLongClickListener{showPopupMenu(imvCell00)}
        imvCell01.setOnLongClickListener{showPopupMenu(imvCell01)}
        imvCell02.setOnLongClickListener{showPopupMenu(imvCell02)}
        imvCell10.setOnLongClickListener{showPopupMenu(imvCell10)}
        imvCell11.setOnLongClickListener{showPopupMenu(imvCell11)}
        imvCell12.setOnLongClickListener{showPopupMenu(imvCell12)}
        imvCell20.setOnLongClickListener{showPopupMenu(imvCell20)}
        imvCell21.setOnLongClickListener{showPopupMenu(imvCell21)}
        imvCell22.setOnLongClickListener{showPopupMenu(imvCell22)}
        sbDifficultyLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                difficultyLevelChange(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        //Inicia uma nova partida.
        startNewMatch()
    }


//SETTINGS__________________________________________________________________________________________

    /**
     * Carrega as configurações do aplicativo que estão salvas.
     */
    private fun loadSettings() {
        //Obtém o nível de dificuldade salvo.
        sbDifficultyLevel.progress = settings.getInt(KEY_DIFFICULT_LEVEL, 2)
        bot.changeDifficultyLevel(getDifficultLevelFrom(sbDifficultyLevel.progress))
        //Obtém o valor de efeito sonoro salvo.
        soundEffect = settings.getBoolean(KEY_SOUND_EFFECT, false)
    }


//GAME CONTROLLER___________________________________________________________________________________

    /**
     * Iniciar uma nova partida. Caso o jogador da vez seja o do sistema, realiza a jogada imediatamente.
     */
    private fun startNewMatch() {
        controller.startNewMatch()
        reset()
        updateArrow()
        updateScore()
        checkIsTheBotTurn()
        playSound(R.raw.sound3)
    }

    /**
     * Marcar uma posição vazia do tabuleiro. Após a jogada, verifica se o jogador a marcar  na
     * sequência é o sistema. Caso seja, executa a jogada do mesmo.
     * @param player jogador que vai marcar uma posição vazia do tabuleiro.
     */
    private fun makeTheMove(player: Player) {
        Thread(Runnable {
            runOnUiThread {
                try {
                    val position: CellPosition? = controller.makeTheMove(player)
                    playSound(R.raw.sound1)
                    if (position != null) drawBoardCell(position, player.getLabel())
                    updateArrow()
                    checkIsTheBotTurn()
                } catch (e: Exception) {
                    Snackbar.make(
                        imvHuman,
                        e.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }).start()
    }

    /**
     * Verifica se o jogador da vez é o do próprio sistema. Se for, realiza a jogada do mesmo.
     */
    private fun checkIsTheBotTurn() {
        if (!controller.isblocked) {
            if (controller.currentplayer == bot) {
                makeTheMove(bot)
            }
        }
    }

    /**
     * Limpar o tabuleiro e configurar o status do jogo.
     */
    private fun reset() {
        imvCell00.setImageResource(R.drawable.label_empty_icon)
        imvCell01.setImageResource(R.drawable.label_empty_icon)
        imvCell02.setImageResource(R.drawable.label_empty_icon)
        imvCell10.setImageResource(R.drawable.label_empty_icon)
        imvCell11.setImageResource(R.drawable.label_empty_icon)
        imvCell12.setImageResource(R.drawable.label_empty_icon)
        imvCell20.setImageResource(R.drawable.label_empty_icon)
        imvCell21.setImageResource(R.drawable.label_empty_icon)
        imvCell22.setImageResource(R.drawable.label_empty_icon)
        imvArrow1.isVisible = false
        imvArrow2.isVisible = false
    }

    /**
     * Desenhar o marcador numa célula do tabuleiro.
     * @param position célula do tabuleiro a ser desenhada.
     * @param label marcador a ser desenhado.
     */
    private fun drawBoardCell(position: CellPosition, label: Byte) {
        val imageView: ImageView? = when (position.line) {
            0 -> {
                when (position.column) {
                    0 -> imvCell00 //[0,0]
                    1 -> imvCell01 //[0,1]
                    2 -> imvCell02 //[0,2]
                    else -> null
                }
            }
            1 -> {
                when (position.column) {
                    0 -> imvCell10 //[1,0]
                    1 -> imvCell11 //[1,1]
                    2 -> imvCell12 //[1,2]
                    else -> null
                }
            }
            2 -> {
                when (position.column) {
                    0 -> imvCell20 //[2,0]
                    1 -> imvCell21 //[2,1]
                    2 -> imvCell22 //[2,2]
                    else -> null
                }
            }
            else -> null
        }
        val imageId = getImageResourceByLabel(label)
        imageView?.setImageResource(imageId)
    }

    /**
     * Atualizar a direção da seta que aponta o jogador da vez a marcar o tabuleiro.
     */
    private fun updateArrow() {
        if (!controller.isblocked) {
            if (controller.currentplayer == humanPlayer) {
                imvArrow1.isVisible = true
                imvArrow2.isVisible = false
            } else {
                imvArrow1.isVisible = false
                imvArrow2.isVisible = true
            }
        }
    }

    /**
     * Atualizar o placar do jogo: sua pontuação, pontuação do sistema, ordem da partida.
      */
    private fun updateScore() {
        tvHumanScore.text = controller.player1score.toString()
        tvBotScore.text = controller.player2score.toString()
        tvMatchNumber.text = controller.matchnumber.toString()
    }


//GAME LISTENER EVENTS______________________________________________________________________________

    override fun onWinning(winner: Player, line: BoardLine) {
        playSound(R.raw.sound2)
        tvHumanScore.text = controller.player1score.toString()
        tvBotScore.text = controller.player2score.toString()
        sbDifficultyLevel.isEnabled = false
        val images: Array<ImageView> = when (line) {
            BoardLine.HORIZONTAL_1 -> arrayOf(imvCell00, imvCell01, imvCell02)
            BoardLine.HORIZONTAL_2 -> arrayOf(imvCell10, imvCell11, imvCell12)
            BoardLine.HORIZONTAL_3 -> arrayOf(imvCell20, imvCell21, imvCell22)
            BoardLine.VERTICAL_1 -> arrayOf(imvCell00, imvCell10, imvCell20)
            BoardLine.VERTICAL_2 -> arrayOf(imvCell01, imvCell11, imvCell21)
            BoardLine.VERTICAL_3 -> arrayOf(imvCell02, imvCell12, imvCell22)
            BoardLine.DIAGONAL_1 -> arrayOf(imvCell00, imvCell11, imvCell22)
            BoardLine.DIAGONAL_2 -> arrayOf(imvCell02, imvCell11, imvCell20)
        }
        val rid = getImageResourceByLabel(winner.getLabel())
        //Cria o efeito de intermitência com a linha marcada.
        var counter = 1
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (counter <= 9) {
                        if (counter % 2 != 0) {
                            images[0].setImageResource(rid)
                            images[1].setImageResource(rid)
                            images[2].setImageResource(rid)
                        } else {
                            images[0].setImageResource(R.drawable.label_empty_icon)
                            images[1].setImageResource(R.drawable.label_empty_icon)
                            images[2].setImageResource(R.drawable.label_empty_icon)
                        }
                        counter++
                    } else {
                        this.cancel()
                        sbDifficultyLevel.isEnabled = true
                        startNewMatch()
                    }
                }
            }
        }, 0, 500)
    }

    override fun onFillingBoard() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    startNewMatch()
                    this.cancel()
                }
            }
        }, 1000)
    }


//UI CONTROLS EVENTS________________________________________________________________________________

    /**
     * Tratar o clique em alguma das casas do tabuleiro.
     * @param position posição da casa clicada.
     */
    private fun boardCellClick(position: CellPosition) {
        if (!controller.isblocked) {
            if (controller.isEmptyBoardPosition(position)) {
                if (controller.currentplayer == humanPlayer) {
                    humanPlayer.position = position
                    makeTheMove(humanPlayer)
                }
            } else {
                Snackbar.make(
                    imvHuman,
                    R.string.tictactoe_cell_isnot_empty_message,
                    1000
                ).show()
            }
        }
    }

    /**
     * Tratar o clique no SeekBar para alteração do nível de dificuldade.
     * @param progress valor do SeekBar.
     */
    private fun difficultyLevelChange(progress: Int) {
        settings.setInt(KEY_DIFFICULT_LEVEL, progress)
        controller.block()
        val difficulty = getDifficultLevelFrom(progress)
        bot.changeDifficultyLevel(difficulty)
        reset()
        startNewMatch()
    }


//ADAPTERS__________________________________________________________________________________________

    /**
     * Transforma o valor int em referência a [DifficultyLevel].
     * @param progress valor int obtido da interface gráfica.
     * @return respectivo nível de dificuldade associado.
     */
    private fun getDifficultLevelFrom(progress: Int): DifficultyLevel = when (progress) {
        0 -> DifficultyLevel.NORMAL
        1 -> DifficultyLevel.HARD
        2 -> DifficultyLevel.INVINCIBLE
        else -> DifficultyLevel.INVINCIBLE
    }

    /**
     * Obter o desenho relacionado a um marcador específico identificado por [label].
     * @param label marcador relacionado.
     * @return identificador da imagem nos resources do aplicativo.
     */
    private fun getImageResourceByLabel(label: Byte): Int = when (label) {
        LABEL_X.code.toByte() -> R.drawable.label_x_icon
        LABEL_O.code.toByte() -> R.drawable.label_o_icon
        else -> 0
    }

    /**
     * Tocar um som predefinido.
     * @param rid identificador do arquivo nos resources do aplicativo.
     */
    private fun playSound(rid: Int) {
        if (soundEffect) {
            soundPlayer.execute(rid)
        }
    }


//POPUP MENU________________________________________________________________________________________

    /**
     * Exibir o menu popup num clique longo numa das casas do tabuleiro do jogo.
     * @param view componente clicado.
     */
    private fun showPopupMenu(view: View?): Boolean {
        val popup = PopupMenu(this, view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setForceShowIcon(popup)
        }
        popup.inflate(R.menu.popup_menu_play_sound)
        popup.menu[0].isVisible = !soundEffect
        popup.menu[1].isVisible = soundEffect
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.sound_effect_on -> {
                    soundEffect = true
                    settings.setBoolean(KEY_SOUND_EFFECT, soundEffect)
                }
                R.id.sound_effect_off -> {
                    soundEffect = false
                    settings.setBoolean(KEY_SOUND_EFFECT, soundEffect)
                }
            }
            true
        })
        popup.show()
        return true
    }

    /**
     * Específico para Android a partir da versão 29, desenha um ícone no menu de opção do popup.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setForceShowIcon(popup: PopupMenu) {
        popup.setForceShowIcon(true)
    }

}