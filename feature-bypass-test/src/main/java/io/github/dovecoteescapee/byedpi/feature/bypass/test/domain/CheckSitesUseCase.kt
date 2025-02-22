package io.github.dovecoteescapee.byedpi.feature.bypass.test.domain

import android.util.Log
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.LogManager
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ByPassTestViewModel.State.Data.CommandType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.Collections

internal class CheckSitesUseCase(
    private val logManager: LogManager,
    private val appSettings: KeyValueStorage<StorageType.AppSettings>,
) {

    private val proxyIp: String = "127.0.0.1"
    private val proxyPort: Int = 10080

    private val delaySec by lazy {
        runBlocking {
            appSettings.getString("byedpi_proxytest_delay")?.toIntOrNull()  ?: 1
        }
    }
    private val fullLog by lazy {
        runBlocking {
            appSettings.getBoolean("byedpi_proxytest_fulllog")
        }
    }
    private val requestsCount by lazy {
        runBlocking {
            appSettings.getString("byedpi_proxytest_requestsсount")
                ?.toIntOrNull()?.takeIf { it > 0 }
                ?: 1
        }
    }


    suspend fun invoke(cmd: String, sites: List<String>): CommandType {
        // val successfulCmds = mutableListOf<Pair<String, Int>>()
        val detailLogList = Collections.synchronizedList(mutableListOf<String>())

        val totalRequests = sites.size * requestsCount
        val checkResults = if (fullLog) {
            detailLogList.clear()
            checkSitesAsync(sites, requestsCount) {
                detailLogList.add(it)
            }
        } else {
            checkSitesAsync(sites, requestsCount)
        }
        val successfulCount = checkResults.sumOf { it.second }
        val successPercentage = (successfulCount * 100) / totalRequests
        delay(delaySec * 1000L)

        //if (successPercentage >= 50) successfulCmds.add(cmd to successPercentage)

        val results = "$successfulCount/$totalRequests ($successPercentage%)"

        logManager.saveLog("cmd:{$cmd}\n{$results}")

        return addCmdToCommands(cmd, results, detailLogList)
    }

    private fun addCmdToCommands(
        cmd: String,
        results: String,
        detailLogList: List<String>
    ): CommandType {
        return if (detailLogList.isEmpty()) {
            CommandType(
                cmd = cmd,
                results = results,
            )
        } else {
            CommandType(
                cmd = cmd,
                results = results,
                sites = detailLogList
            )
        }
    }

    private suspend fun checkSitesAsync(
        sites: List<String>,
        requestsCount: Int,
        logVerbose: ((String) -> Unit)? = null,
    ): List<Pair<String, Int>> {
        return withContext(Dispatchers.IO) {
            sites.map { site ->
                async {
                    //if (!isProxyRunning) return@async site to 0

                    val successCount = checkSiteAccess(site, requestsCount)
                    logVerbose?.invoke("$site - $successCount/$requestsCount")

                    site to successCount
                }
            }.awaitAll()
        }
    }

    private suspend fun checkSiteAccess(
        site: String,
        requestsCount: Int
    ): Int = withContext(Dispatchers.IO) {
        var responseCount = 0
        val formattedUrl = if (site.startsWith("http://") || site.startsWith("https://"))
            site
        else
            "https://$site"

        repeat(requestsCount) { attempt ->
            Log.i("CheckSite", "Attempt ${attempt + 1}/$requestsCount for $site")
            try {
                val url = URL(formattedUrl)
                val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyIp, proxyPort))
                val connection = url.openConnection(proxy) as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 2000
                connection.readTimeout = 2000

                val responseCode = connection.responseCode
                responseCount++
                Log.i("CheckSite", "Response for $site: $responseCode")
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("CheckSite", "Error accessing $site: ${e.message}")
            }
        }
        responseCount
    }
}
