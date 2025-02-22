package io.github.dovecoteescapee.byedpi.common.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.ui.R

abstract class BaseFragment(layoutRes: Int) : Fragment(layoutRes), HasDiComponent {

    abstract override val component: DIComponent

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_color))

        view.isClickable = true
        view.isFocusable = true
    }
}
