package games.launcher

import android.view.View.OnClickListener

data class GameItem (val rid: Int, val name: String, val description: String, val clickListener: OnClickListener)