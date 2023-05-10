package games.media

import android.content.Context
import android.media.MediaPlayer
import games.tictactoe.R

/**
 * Tocador de sons no aplicativo.
 */
class SoundPlayer(context: Context) {

    private val player1: MediaPlayer
    private val player2: MediaPlayer
    private val player3: MediaPlayer

    init {
        player1 = MediaPlayer.create(context, R.raw.sound1)
        player1.isLooping = false
        player2 = MediaPlayer.create(context, R.raw.sound2)
        player2.isLooping = false
        player3 = MediaPlayer.create(context, R.raw.sound3)
        player3.isLooping = false
    }

    /**
     * Executar uma trilha sonora que está inserida nos resources do aplicativo.
     * @param rid identificador da trilha sonora nos resources.
     */
    fun execute(rid: Int) {
        when (rid) {
            R.raw.sound1 -> player1.start()
            R.raw.sound2 -> player2.start()
            R.raw.sound3 -> player3.start()
        }
    }

}