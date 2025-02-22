package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.dovecoteescapee.byedpi.feature.connection.ui.R
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.swipemenu.OneTouchHelperCallback
import java.util.Collections

internal class ProfilesAdapter(
    private val items: MutableList<String> = mutableListOf(),
    private val swapListener: (List<String>) -> Unit,
    private val editClickListener: (String) -> Unit,
    private val deleteClickListener: (String) -> Unit,
): RecyclerView.Adapter<RecyclerViewHolder>(), OneTouchHelperCallback.DragAdapter {

    fun updateItems(items: List<String>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(items, fromPosition, toPosition)
        notifyDataSetChanged()
        swapListener.invoke(items)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_rv_item, parent,false)
        return RecyclerViewHolder(view).also { viewHolder ->
            viewHolder.foregroundKnobLayout.setOnClickListener {
                Unit
            }

            viewHolder.editProfile.setOnClickListener {
                editClickListener.invoke(items[viewHolder.adapterPosition])
            }

            viewHolder.deleteProfile.setOnClickListener {
                if (items.size > 1) {
                    deleteClickListener.invoke(items[viewHolder.adapterPosition])
                    val position = viewHolder.adapterPosition
                    items.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerViewHolder, position: Int) {
        viewHolder.bind(items[position], position)
    }
}

internal class RecyclerViewHolder(view: View): RecyclerView.ViewHolder(view), OneTouchHelperCallback.SwipeViewHolder {

    override val foregroundKnobLayout: ViewGroup = view.findViewById(R.id.foregroundKnobLayout)
    override val backgroundLeftButtonLayout: ViewGroup = view.findViewById(R.id.backgroundLeftButtonLayout)
    override val backgroundRightButtonLayout: ViewGroup = view.findViewById(R.id.backgroundRightButtonLayout)
    override val canRemoveOnSwipingFromRight: Boolean get() = true

    val editProfile: ImageButton = view.findViewById(R.id.editProfile)
    val deleteProfile: ImageButton = view.findViewById(R.id.deleteProfile)

    private val profileTv: TextView = view.findViewById(R.id.profileText)

    fun bind(profileText: String, position: Int) {
        profileTv.text = profileText
    }
}
