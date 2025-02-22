package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.swipemenu

import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class OneTouchHelperCallback(
    private val recyclerView: RecyclerView
): ItemTouchHelper.Callback() {

    interface DragAdapter {

        fun onItemMove(fromPosition: Int, toPosition: Int)
    }

    interface SwipeViewHolder {

        val foregroundKnobLayout: ViewGroup

        val backgroundLeftButtonLayout: ViewGroup
        val backgroundRightButtonLayout: ViewGroup

        val canRemoveOnSwipingFromLeft: Boolean get() = false
        val canRemoveOnSwipingFromRight: Boolean get() = false
    }

    private var draggingFrom = -1

    private var swipingStartingX = 0f
    private var swipingAdapterPosition = -1

    private var helper: WeakReference<ItemTouchHelper?>? = null

    fun build() {
        helper = WeakReference(ItemTouchHelper(this))
        helper?.get()?.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        if (swipingAdapterPosition > -1) {
                            val position = swipingAdapterPosition
                            swipingAdapterPosition = -1
                            closeForAdapterPosition(position)
                        }
                    }
                }
            }
        })

        recyclerView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val childView = recyclerView.findChildViewUnder(event.x, event.y) ?: return@setOnTouchListener false
                    val adapterPosition = recyclerView.getChildAdapterPosition(childView)
                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(adapterPosition) as? SwipeViewHolder
                        ?: return@setOnTouchListener false
                    val tolerance = 3 * view.resources.displayMetrics.density.toInt()

                    if (viewHolder.foregroundKnobLayout.translationX >= viewHolder.backgroundLeftButtonLayout.width - tolerance) {
                        (0 until viewHolder.backgroundLeftButtonLayout.childCount).map(viewHolder.backgroundLeftButtonLayout::getChildAt).forEach { button ->
                            val rect = Rect()
                            button.getGlobalVisibleRect(rect)
                            if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                button.performClick()
                                return@setOnTouchListener true
                            }
                        }
                    }

                    if (viewHolder.foregroundKnobLayout.translationX <= -viewHolder.backgroundRightButtonLayout.width + tolerance) {
                        (0 until viewHolder.backgroundRightButtonLayout.childCount).map(viewHolder.backgroundRightButtonLayout::getChildAt).forEach { button ->
                            val rect = Rect()
                            button.getGlobalVisibleRect(rect)
                            if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                button.performClick()
                                return@setOnTouchListener true
                            }
                        }
                    }
                }
            }
            false
        }
    }

    private fun closeForAdapterPosition(position: Int) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) ?: return
        viewHolder as SwipeViewHolder

        viewHolder.foregroundKnobLayout.animate()
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .translationX(0f)
            .withStartAction {
                viewHolder.foregroundKnobLayout.isClickable = false
                helper?.get()?.onChildViewDetachedFromWindow(viewHolder.itemView)
                helper?.get()?.onChildViewAttachedToWindow(viewHolder.itemView)
            }
            .withEndAction {
                viewHolder.foregroundKnobLayout.isClickable = true
            }
            .start()
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // ItemTouchHelper.ACTION_STATE_IDLE onClickListener
        val drag = getDragDirs(recyclerView, viewHolder)
        val swipe = getSwipeDirs(recyclerView, viewHolder)
        return makeMovementFlags(drag, swipe)
    }

    open fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int = when {
        recyclerView.adapter !is DragAdapter -> ItemTouchHelper.ACTION_STATE_IDLE
        viewHolder is SwipeViewHolder && viewHolder.foregroundKnobLayout.translationX != 0f -> ItemTouchHelper.ACTION_STATE_IDLE
        recyclerView.layoutManager !is GridLayoutManager -> ItemTouchHelper.UP or ItemTouchHelper.DOWN
        else -> ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN
    }

    open fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int = when {
        viewHolder !is SwipeViewHolder -> ItemTouchHelper.ACTION_STATE_IDLE
        else -> ItemTouchHelper.LEFT
    }

    override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = current.itemViewType == target.itemViewType

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> onSelectedChangedForDrag(viewHolder, actionState)
            // ACTION_STATE_IDLE: viewHolder is null
            // ACTION_STATE_IDLE
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.ACTION_STATE_SWIPE -> onSelectedChangedForSwipe(viewHolder, actionState)
        }
    }

    private fun onSelectedChangedForDrag(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder ?: return

        closeForAdapterPosition(swipingAdapterPosition)

        draggingFrom = viewHolder.adapterPosition
        viewHolder.itemView.animate()
            .setDuration(20)
            .translationZ(30f)
            .start()
    }

    private fun onSelectedChangedForSwipe(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder !is SwipeViewHolder) return

        if (viewHolder.foregroundKnobLayout.isClickable == false) {
            return
        }

        if (swipingAdapterPosition != viewHolder.adapterPosition) {
            val position = swipingAdapterPosition
            swipingAdapterPosition = viewHolder.adapterPosition
            closeForAdapterPosition(position)
        }

        swipingStartingX = when {
            viewHolder.foregroundKnobLayout.translationX < 0f -> recyclerView.width.toFloat() - viewHolder.backgroundRightButtonLayout.width
            viewHolder.foregroundKnobLayout.translationX > 0f -> recyclerView.width.toFloat() - viewHolder.backgroundLeftButtonLayout.width
            else -> 0f
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> onChildDrawForDrag(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            ItemTouchHelper.ACTION_STATE_SWIPE -> onChildDrawForSwipe(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    private fun onChildDrawForDrag(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        viewHolder.itemView.translationX = dX
        viewHolder.itemView.translationY = dY
    }

    private fun onChildDrawForSwipe(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (swipingAdapterPosition != viewHolder.adapterPosition) {
            return
        }

        viewHolder as SwipeViewHolder

        if (viewHolder.foregroundKnobLayout.isClickable == false) {
            return
        }

        viewHolder.foregroundKnobLayout.translationX = if (dX < 0f) {
            if (viewHolder.canRemoveOnSwipingFromRight)
                min(0f,dX + swipingStartingX)
            else
                min(0f, max(-viewHolder.backgroundRightButtonLayout.width.toFloat(), dX + swipingStartingX))
        } else {
            if (viewHolder.canRemoveOnSwipingFromLeft)
                max(0f,dX - swipingStartingX)
            else
                max(0f, min(viewHolder.backgroundLeftButtonLayout.width.toFloat(), dX - swipingStartingX))
        }

        viewHolder.backgroundLeftButtonLayout.visibility = if (dX < 0f) ViewGroup.INVISIBLE else ViewGroup.VISIBLE
        viewHolder.backgroundRightButtonLayout.visibility = if (dX < 0f) ViewGroup.VISIBLE else ViewGroup.INVISIBLE
    }

    private fun onMoved(viewHolder: RecyclerView.ViewHolder) {
        if (draggingFrom > -1) {
            viewHolder.itemView.animate()
                .setDuration(20)
                .translationZ(0f)
                .withStartAction {
                    val from = draggingFrom
                    val to = viewHolder.adapterPosition
                    if (from != to) {
                        (recyclerView.adapter as DragAdapter).onItemMove(from, to)
                    }
                }
                .withEndAction {
                    draggingFrom = -1
                }
                .start()
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        viewHolder as SwipeViewHolder

        val dX = viewHolder.foregroundKnobLayout.translationX

        if (dX < 0f) {
            val x = viewHolder.itemView.width - (viewHolder.itemView.width - viewHolder.backgroundRightButtonLayout.width) / 2f
            if (dX < -x) {
                if (swipingStartingX > 0f) {
                    viewHolder.foregroundKnobLayout.animate()
                        .setDuration(250)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .translationX(-recyclerView.width.toFloat())
                        .withStartAction {
                            viewHolder.foregroundKnobLayout.isClickable = false
                        }
                        .withEndAction {
                            onSwiped(viewHolder, ItemTouchHelper.LEFT)
                            viewHolder.foregroundKnobLayout.isClickable = true
                        }
                        .start()
                }
                return 0.1f
            }
        } else {
            val x = viewHolder.itemView.width - (viewHolder.itemView.width - viewHolder.backgroundLeftButtonLayout.width) / 2f
            if (dX > x) {
                if (swipingStartingX > 0f) {
                    viewHolder.foregroundKnobLayout.animate()
                        .setDuration(250)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .translationX(recyclerView.width.toFloat())
                        .withStartAction {
                            viewHolder.foregroundKnobLayout.isClickable = false
                        }
                        .withEndAction {
                            onSwiped(viewHolder, ItemTouchHelper.RIGHT)
                            viewHolder.foregroundKnobLayout.isClickable = true
                        }
                        .start()
                }
                return 0.1f
            }
        }

        if (swipingStartingX == 0f) {
            var x = viewHolder.backgroundRightButtonLayout.width / 2f
            if (dX < -x) {
                viewHolder.foregroundKnobLayout.animate()
                    .setDuration(250)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .translationX(-viewHolder.backgroundRightButtonLayout.width.toFloat())
                    .withStartAction {
                        viewHolder.foregroundKnobLayout.isClickable = false
                    }
                    .withEndAction {
                        viewHolder.foregroundKnobLayout.isClickable = true
                    }
                    .start()
            }

            x = viewHolder.backgroundLeftButtonLayout.width / 2f
            if (dX > x) {
                viewHolder.foregroundKnobLayout.animate()
                    .setDuration(250)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .translationX(viewHolder.backgroundLeftButtonLayout.width.toFloat())
                    .withStartAction {
                        viewHolder.foregroundKnobLayout.isClickable = false
                    }
                    .withEndAction {
                        viewHolder.foregroundKnobLayout.isClickable = true
                    }
                    .start()
            }
        }

        val value = if (dX < 0f) {
            viewHolder.backgroundRightButtonLayout.width / 2f / recyclerView.width
        } else {
            viewHolder.backgroundLeftButtonLayout.width / 2f / recyclerView.width
        }

        return value.takeIf { swipingStartingX <= 0f } ?: (1 - value)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        viewHolder as SwipeViewHolder

        val dX = viewHolder.foregroundKnobLayout.translationX

        if (abs(dX) >= recyclerView.width.toFloat()) {
            if (direction == ItemTouchHelper.LEFT) {
                // viewHolder.backgroundRightButtonLayout.getChildAt(viewHolder.backgroundRightButtonLayout.childCount - 1).performClick()
            }
            if (direction == ItemTouchHelper.RIGHT) {
                viewHolder.backgroundLeftButtonLayout.getChildAt(0).performClick()
            }
            // recyclerView.adapter?.notifyItemRemoved(viewHolder.adapterPosition)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder.adapterPosition == -1) {
            (viewHolder as? SwipeViewHolder)?.foregroundKnobLayout?.translationX = 0f
        }

        onMoved(viewHolder)
    }
}
