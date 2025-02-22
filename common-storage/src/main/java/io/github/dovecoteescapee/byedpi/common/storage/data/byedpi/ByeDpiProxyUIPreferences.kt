package io.github.dovecoteescapee.byedpi.common.storage.data.byedpi

import android.util.Log
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProxyIp
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProxyPort

class ByeDpiProxyUIPreferences(
    ip: String,
    port: Int,
    maxConnections: Int? = null,
    bufferSize: Int? = null,
    defaultTtl: Int? = null,
    noDomain: Boolean? = null,
    desyncHttp: Boolean? = null,
    desyncHttps: Boolean? = null,
    desyncUdp: Boolean? = null,
    desyncMethod: DesyncMethod? = null,
    splitPosition: Int? = null,
    splitAtHost: Boolean? = null,
    fakeTtl: Int? = null,
    fakeSni: String? = null,
    oobChar: String? = null,
    hostMixedCase: Boolean? = null,
    domainMixedCase: Boolean? = null,
    hostRemoveSpaces: Boolean? = null,
    tlsRecordSplit: Boolean? = null,
    tlsRecordSplitPosition: Int? = null,
    tlsRecordSplitAtSni: Boolean? = null,
    hostsMode: HostsMode? = null,
    hosts: String? = null,
    tcpFastOpen: Boolean? = null,
    udpFakeCount: Int? = null,
    dropSack: Boolean? = null,
    byedpiFakeOffset: Int? = null,
) : ByeDpiProxyArgs {
    val ip: String = ip
    val port: Int = port
    val maxConnections: Int = maxConnections ?: 512
    val bufferSize: Int = bufferSize ?: 16384
    val defaultTtl: Int = defaultTtl ?: 0
    val customTtl: Boolean = defaultTtl != null
    val noDomain: Boolean = noDomain ?: false
    val desyncHttp: Boolean = desyncHttp ?: true
    val desyncHttps: Boolean = desyncHttps ?: true
    val desyncUdp: Boolean = desyncUdp ?: false
    val desyncMethod: DesyncMethod = desyncMethod ?: DesyncMethod.Disorder
    val splitPosition: Int = splitPosition ?: 1
    val splitAtHost: Boolean = splitAtHost ?: false
    val fakeTtl: Int = fakeTtl ?: 8
    val fakeSni: String = fakeSni ?: "www.iana.org"
    val oobChar: Byte = (oobChar ?: "a")[0].code.toByte()
    val hostMixedCase: Boolean = hostMixedCase ?: false
    val domainMixedCase: Boolean = domainMixedCase ?: false
    val hostRemoveSpaces: Boolean = hostRemoveSpaces ?: false
    val tlsRecordSplit: Boolean = tlsRecordSplit ?: false
    val tlsRecordSplitPosition: Int = tlsRecordSplitPosition ?: 0
    val tlsRecordSplitAtSni: Boolean = tlsRecordSplitAtSni ?: false
    val hostsMode: HostsMode =
        if (hosts?.isBlank() != false) HostsMode.Disable
        else hostsMode ?: HostsMode.Disable
    val hosts: String? =
        if (this.hostsMode == HostsMode.Disable) null
        else hosts?.trim()
    val tcpFastOpen: Boolean = tcpFastOpen ?: false
    val udpFakeCount: Int = udpFakeCount ?: 1
    val dropSack: Boolean = dropSack ?: false
    val fakeOffset: Int = byedpiFakeOffset ?: 0

    companion object {
        suspend fun createFromStorage(storage: KeyValueStorage<StorageType.ByeDpiArgSettings>): ByeDpiProxyUIPreferences {
            return ByeDpiProxyUIPreferences(
                ip = storage.getProxyIp(),
                port = storage.getProxyPort().toInt(),
                maxConnections = storage.getString("byedpi_max_connections")?.toIntOrNull(),
                bufferSize = storage.getString("byedpi_buffer_size")?.toIntOrNull(),
                defaultTtl = storage.getString("byedpi_default_ttl")?.toIntOrNull(),
                noDomain = storage.getBoolean("byedpi_no_domain"),
                desyncHttp = storage.getBoolean("byedpi_desync_http"),
                desyncHttps = storage.getBoolean("byedpi_desync_https"),
                desyncUdp = storage.getBoolean("byedpi_desync_udp"),
                desyncMethod = storage.getString("byedpi_desync_method")
                    ?.let { DesyncMethod.fromName(it) },
                splitPosition = storage.getString("byedpi_split_position")?.toIntOrNull(),
                splitAtHost = storage.getBoolean("byedpi_split_at_host"),
                fakeTtl = storage.getString("byedpi_fake_ttl")?.toIntOrNull(),
                fakeSni = storage.getString("byedpi_fake_sni"),
                oobChar = storage.getString("byedpi_oob_data"),
                hostMixedCase = storage.getBoolean("byedpi_host_mixed_case"),
                domainMixedCase = storage.getBoolean("byedpi_domain_mixed_case"),
                hostRemoveSpaces = storage.getBoolean("byedpi_host_remove_spaces"),
                tlsRecordSplit = storage.getBoolean("byedpi_tlsrec_enabled"),
                tlsRecordSplitPosition = storage.getString("byedpi_tlsrec_position")
                    ?.toIntOrNull(),
                tlsRecordSplitAtSni = storage.getBoolean("byedpi_tlsrec_at_sni"),
                hostsMode = storage.getString("byedpi_hosts_mode")
                    ?.let { HostsMode.fromName(it) },
                hosts = storage.getString("byedpi_hosts_mode")?.let {
                    when (HostsMode.fromName(it)) {
                        HostsMode.Blacklist -> storage.getString("byedpi_hosts_blacklist")
                        HostsMode.Whitelist -> storage.getString("byedpi_hosts_whitelist")
                        else -> null
                    }
                },
                tcpFastOpen = storage.getBoolean("byedpi_tcp_fast_open"),
                udpFakeCount = storage.getString("byedpi_udp_fake_count")?.toIntOrNull(),
                dropSack = storage.getBoolean("byedpi_drop_sack"),
                byedpiFakeOffset = storage.getString("byedpi_fake_offset")?.toIntOrNull(),
            )
        }
    }

    enum class DesyncMethod {
        None,
        Split,
        Disorder,
        Fake,
        OOB,
        DISOOB;

        companion object {
            fun fromName(name: String): DesyncMethod {
                return when (name) {
                    "none" -> None
                    "split" -> Split
                    "disorder" -> Disorder
                    "fake" -> Fake
                    "oob" -> OOB
                    "disoob" -> DISOOB
                    else -> throw IllegalArgumentException("Unknown desync method: $name")
                }
            }
        }
    }

    enum class HostsMode {
        Disable,
        Blacklist,
        Whitelist;

        companion object {
            fun fromName(name: String): HostsMode {
                return when (name) {
                    "disable" -> Disable
                    "blacklist" -> Blacklist
                    "whitelist" -> Whitelist
                    else -> throw IllegalArgumentException("Unknown hosts mode: $name")
                }
            }
        }
    }

    override val args: Array<String>
        get() {
            val args = mutableListOf("ciadpi")

            ip.takeIf { it.isNotEmpty() }?.let {
                args.add("-i${it}")
            }

            port.takeIf { it != 0 }?.let {
                args.add("-p${it}")
            }

            maxConnections.takeIf { it != 0 }?.let {
                args.add("-c${it}")
            }

            bufferSize.takeIf { it != 0 }?.let {
                args.add("-b${it}")
            }

            val protocols = mutableListOf<String>()
            if (desyncHttps) protocols.add("t")
            if (desyncHttp) protocols.add("h")

            if (!hosts.isNullOrBlank()) {
                val hostStr = ":${hosts}"
                val hostBlock = mutableListOf<String>()

                when (hostsMode) {
                    HostsMode.Blacklist -> {
                        hostBlock.add("-H${hostStr}")
                        hostBlock.add("-An")
                        if (protocols.isNotEmpty()) {
                            hostBlock.add("-K${protocols.joinToString(",")}")
                        }
                    }
                    HostsMode.Whitelist -> {
                        if (protocols.isNotEmpty()) {
                            hostBlock.add("-K${protocols.joinToString(",")}")
                        }
                        hostBlock.add("-H${hostStr}")
                    }
                    else -> {}
                }
                args.addAll(hostBlock)
            } else {
                if (protocols.isNotEmpty()) {
                    args.add("-K${protocols.joinToString(",")}")
                }
            }

            defaultTtl.takeIf { it != 0 }?.let {
                args.add("-g${it}")
            }

            if (noDomain) {
                args.add("-N")
            }

            desyncMethod.let { method ->
                splitPosition.takeIf { it != 0 }?.let { pos ->
                    var posArg = pos.toString()
                    if (splitAtHost) {
                        posArg += "+h"
                    }
                    val option = when (method) {
                        DesyncMethod.Split -> "-s"
                        DesyncMethod.Disorder -> "-d"
                        DesyncMethod.OOB -> "-o"
                        DesyncMethod.DISOOB -> "-q"
                        DesyncMethod.Fake -> "-f"
                        DesyncMethod.None -> ""
                    }
                    args.add("${option}${posArg}")
                }
            }

            if (desyncMethod == DesyncMethod.Fake) {
                fakeTtl.takeIf { it != 0 }?.let {
                    args.add("-t${it}")
                }

                fakeSni.takeIf { it.isNotEmpty() }?.let {
                    args.add("-n${it}")
                }

                fakeOffset.takeIf { it != 0 }?.let {
                    args.add("-O${it}")
                }
            }

            if (desyncMethod == DesyncMethod.OOB ||
                desyncMethod == DesyncMethod.DISOOB) {
                args.add("-e${oobChar}")
            }

            val modHttpFlags = mutableListOf<String>()
            if (hostMixedCase) modHttpFlags.add("h")
            if (domainMixedCase) modHttpFlags.add("d")
            if (hostRemoveSpaces) modHttpFlags.add("r")
            if (modHttpFlags.isNotEmpty()) {
                args.add("-M${modHttpFlags.joinToString(",")}")
            }

            if (tlsRecordSplit) {
                tlsRecordSplitPosition.takeIf { it != 0 }?.let {
                    var tlsRecArg = it.toString()
                    if (tlsRecordSplitAtSni) {
                        tlsRecArg += "+s"
                    }
                    args.add("-r${tlsRecArg}")
                }
            }

            if (tcpFastOpen) {
                args.add("-F")
            }

            if (dropSack) {
                args.add("-Y")
            }

            args.add("-An")

            if (desyncUdp) {
                args.add("-Ku")

                udpFakeCount.takeIf { it != 0 }?.let {
                    args.add("-a${it}")
                }

                args.add("-An")
            }

            Log.d("ProxyPref", "UI to cmd: ${args.joinToString(" ")}")
            return args.toTypedArray()
        }
}
