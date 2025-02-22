package io.github.dovecoteescapee.byedpi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import io.github.dovecoteescapee.byedpi.bypass.feature.api.fromKeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.byeDpiProxyArgs
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.NavigationComponentHolder
import io.github.dovecoteescapee.byedpi.databinding.ActivityMainBinding
import io.github.dovecoteescapee.byedpi.feature.connection.ui.api.ConnectionFragmentScreen
import io.github.dovecoteescapee.byedpi.navigation.impl.FragmentNavigator
import io.github.dovecoteescapee.byedpi.navigation.impl.RouterImpl
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val router: RouterImpl
        get() = NavigationComponentHolder.get().router as RouterImpl

    private val appSettings: KeyValueStorage<StorageType.AppSettings>
        get() = StorageComponentHolder.get().appSettings()

    private val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
        get() = StorageComponentHolder.get().byeDpiArgSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            val lang = appSettings.getString("language") ?: "system"
            setLang(lang)

            val theme = appSettings.getString("app_theme") ?: "system"
            setTheme(theme)

            byeDpiProxyArgs = fromKeyValueStorage(byeDpiSettings)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        router.setNavigator(
            FragmentNavigator(
                WeakReference(this),
                binding.fragmentCont.id
            ),
            savedInstanceState == null
        )

        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        if (savedInstanceState == null) {
            router.navigateTo(ConnectionFragmentScreen(), true)
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    router.goBack()
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        router.goBack()
        return true
    }

    override fun onDestroy() {
        router.clearNavigator()
        super.onDestroy()
    }

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName

        fun setLang(lang: String) {
            val appLocale = localeByName(lang) ?: throw IllegalStateException("Invalid value for language: $lang")
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        private fun localeByName(lang: String): LocaleListCompat? = when (lang) {
            "system" -> LocaleListCompat.getEmptyLocaleList()
            "ru" -> LocaleListCompat.forLanguageTags("ru")
            "en" -> LocaleListCompat.forLanguageTags("en")
            else -> {
                Log.w(TAG, "Invalid value for language: $lang")
                null
            }
        }

        fun setTheme(name: String) =
            themeByName(name)?.let {
                AppCompatDelegate.setDefaultNightMode(it)
            } ?: throw IllegalStateException("Invalid value for app_theme: $name")

        private fun themeByName(name: String): Int? = when (name) {
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> {
                Log.w(TAG, "Invalid value for app_theme: $name")
                null
            }
        }
    }
}
