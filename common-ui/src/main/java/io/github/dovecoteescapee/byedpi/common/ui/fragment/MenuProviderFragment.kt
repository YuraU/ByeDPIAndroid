package io.github.dovecoteescapee.byedpi.common.ui.fragment

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

interface MenuProviderFragment {

    fun initMenu(activity: FragmentActivity, viewLifecycleOwner: LifecycleOwner) {
        val weakRefActivity = WeakReference(activity)

        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(getMenuRes(), menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(val itemId = menuItem.itemId) {
                    android.R.id.home -> {
                        weakRefActivity.get()?.onBackPressedDispatcher?.onBackPressed()
                        true
                    }
                    else -> onMenuItemClicked(itemId)
                }
            }
        }

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                weakRefActivity.get()?.let {
                    it.addMenuProvider(menuProvider, it)
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                weakRefActivity.get()?.removeMenuProvider(menuProvider)
            }
        })
    }

    fun getMenuRes(): Int

    fun onMenuItemClicked(itemId: Int): Boolean
}
