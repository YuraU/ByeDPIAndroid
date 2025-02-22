package io.github.dovecoteescapee.byedpi.navigation.impl

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import java.lang.ref.WeakReference

interface Navigator {
    fun updateActionBar(backStackCount: Int)
    fun showScreen(screen: Screen, replace: Boolean)
    fun goBack()
    fun exit()
}

class FragmentNavigator(
    private val activityRef: WeakReference<AppCompatActivity>,
    private val containerId: Int
) : Navigator {

    init {
        activityRef.get()?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
                    if (!fm.isStateSaved) {
                        fm.fragments.forEach { frag ->
                            if (frag != fragment) {
                                fm.beginTransaction()
                                    .setMaxLifecycle(
                                        frag,
                                        Lifecycle.State.STARTED
                                    )
                                    .commit()
                            } else {
                                fm.beginTransaction()
                                    .setMaxLifecycle(
                                        fragment,
                                        Lifecycle.State.RESUMED
                                    )
                                    .commit()
                            }
                        }
                    }
                }

                override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                    if (!fm.isStateSaved) {
                        fm.findFragmentById(containerId)?.let {
                            fm.beginTransaction()
                                .setMaxLifecycle(it, Lifecycle.State.RESUMED)
                                .commit()
                        }
                    }
                }
            },
            true
        )

        activityRef.get()?.supportFragmentManager?.let { fragmentManager ->
            fragmentManager.addOnBackStackChangedListener {
                if (!fragmentManager.isStateSaved) {
                    fragmentManager.findFragmentById(containerId)?.let {
                        fragmentManager.beginTransaction()
                            .setMaxLifecycle(it, Lifecycle.State.RESUMED)
                            .commit()
                    }
                }
            }
        }
    }

    override fun showScreen(screen: Screen, replace: Boolean) {
        activityRef.get()?.let { activity ->
            if (replace) {
                activity.supportFragmentManager.beginTransaction()
                    .replace(containerId, screen.createFragment(), screen.getTag())
                    .addToBackStack(null)
                    .commit()
            } else {
                activity.supportFragmentManager.beginTransaction()
                    .add(containerId, screen.createFragment(), screen.getTag())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun goBack() {
        activityRef.get()?.supportFragmentManager?.popBackStack()
    }

    override fun exit() {
        activityRef.get()?.finish()
    }

    override fun updateActionBar(backStackCount: Int) {
        activityRef.get()?.let { activity ->
            if (backStackCount <= 1) {
                activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }
}
