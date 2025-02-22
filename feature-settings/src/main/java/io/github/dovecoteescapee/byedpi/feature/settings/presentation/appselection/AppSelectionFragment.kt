package io.github.dovecoteescapee.byedpi.feature.settings.presentation.appselection

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.getSelectedApps
import io.github.dovecoteescapee.byedpi.common.storage.utils.putSelectedApps
import io.github.dovecoteescapee.byedpi.common.ui.fragment.BaseFragment
import io.github.dovecoteescapee.byedpi.feature.settings.R
import io.github.dovecoteescapee.byedpi.feature.settings.di.AppSettingsComponentHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AppSelectionFragment : BaseFragment(R.layout.app_selection_fragment) {

    lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>

    override val component = AppSettingsComponentHolder.get()

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AppSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        view.isClickable = true
        view.isFocusable = true

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        setupSearchView()

        loadApps()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
        searchView.setOnQueryTextListener(null)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun loadApps() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                getInstalledApps()
            }
            adapter = AppSelectionAdapter(apps) { app, isChecked ->
                lifecycleScope.launch {
                    updateSelectedApps(app.packageName, isChecked)
                }
            }
            recyclerView.adapter = adapter
            progressBar.visibility = View.GONE
            searchView.visibility = View.VISIBLE
        }
    }

    private suspend fun getInstalledApps(): List<AppInfo> {
        val pm = requireContext().packageManager
        val installedApps = pm.getInstalledApplications(0)

        val selectedApps = appSettings.getSelectedApps()

        return installedApps
            .filter { it.packageName != requireContext().packageName }
            .map {
                AppInfo(
                    pm.getApplicationLabel(it).toString(),
                    it.packageName,
                    pm.getApplicationIcon(it.packageName),
                    selectedApps.contains(it.packageName)
                )
            }
            .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
    }

    private suspend fun updateSelectedApps(packageName: String, isSelected: Boolean) {
        val selectedApps = appSettings.getSelectedApps().toMutableSet()

        if (isSelected) {
            selectedApps.add(packageName)
        } else {
            selectedApps.remove(packageName)
        }

        appSettings.putSelectedApps(selectedApps)
    }

    companion object {
        val TAG = AppSelectionFragment::class.java.simpleName
    }
}
