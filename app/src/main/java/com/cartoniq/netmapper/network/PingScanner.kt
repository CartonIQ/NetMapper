package com.cartoniq.netmapper.network

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.ConnectException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

data class NetHost(
    val ip: String,
    val hostname: String,
    val latencyMs: Long,
    val isSelf: Boolean,
    val isGateway: Boolean
) {
    val lastOctet: Int get() = ip.substringAfterLast('.').toIntOrNull() ?: 0
}

val COMMON_PORTS: List<Pair<Int, String>> = listOf(
    21 to "FTP", 22 to "SSH", 23 to "Telnet", 25 to "SMTP", 53 to "DNS",
    80 to "HTTP", 110 to "POP3", 139 to "NetBIOS", 143 to "IMAP", 443 to "HTTPS",
    445 to "SMB", 554 to "RTSP", 631 to "IPP", 1883 to "MQTT", 3389 to "RDP",
    5000 to "UPnP", 5555 to "ADB", 8008 to "Cast", 8080 to "HTTP-Alt", 8443 to "HTTPS-Alt",
    9100 to "Printer"
)

object PingScanner {

    private val PROBE_PORTS = listOf(80, 443, 445, 22, 53, 8080)

    /** IPv4 address of the Wi-Fi / hotspot interface, or null when not on a local network. */
    fun localIp(): String? {
        return try {
            val all = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            val candidates = all
                .filter { it.isUp && !it.isLoopback }
                .filter { nif ->
                    val n = nif.name.lowercase()
                    n.startsWith("wlan") || n.startsWith("ap") || n.startsWith("swlan") ||
                        n.startsWith("eth") || n.startsWith("softap")
                }
                .flatMap { nif ->
                    nif.inetAddresses.toList()
                        .filter { a -> a is Inet4Address && a.isSiteLocalAddress }
                        .map { a -> nif.name to (a.hostAddress ?: "") }
                }
            (candidates.firstOrNull { it.first.startsWith("wlan") } ?: candidates.firstOrNull())
                ?.second?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    private fun elapsedMs(startNanos: Long): Long =
        ((System.nanoTime() - startNanos) / 1_000_000L).coerceAtLeast(1L)

    private fun tcpAlive(ip: String, port: Int, timeoutMs: Int): Boolean {
        val socket = Socket()
        return try {
            socket.connect(InetSocketAddress(ip, port), timeoutMs)
            true
        } catch (e: ConnectException) {
            e.message?.contains("refused", ignoreCase = true) == true
        } catch (e: Exception) {
            false
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun tcpOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        val socket = Socket()
        return try {
            socket.connect(InetSocketAddress(ip, port), timeoutMs)
            true
        } catch (e: Exception) {
            false
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    /** Returns latency in ms if the host answers (ICMP echo or TCP reply/refusal), else null. */
    fun probe(host: String, reachTimeoutMs: Int = 400, tcpTimeoutMs: Int = 250): Long? {
        val addr = try {
            InetAddress.getByName(host)
        } catch (e: Exception) {
            return null
        }
        val ip = addr.hostAddress ?: host
        var t = System.nanoTime()
        val reachable = try {
            addr.isReachable(reachTimeoutMs)
        } catch (e: Exception) {
            false
        }
        if (reachable) return elapsedMs(t)
        for (p in PROBE_PORTS) {
            t = System.nanoTime()
            if (tcpAlive(ip, p, tcpTimeoutMs)) return elapsedMs(t)
        }
        return null
    }

    private fun hostName(ip: String): String = try {
        val h = InetAddress.getByName(ip).canonicalHostName
        if (h == ip) "" else h
    } catch (e: Exception) {
        ""
    }

    /** Pings .1 to .254 of the local /24 with coroutines on Dispatchers.IO. */
    suspend fun scanSubnet(
        localIp: String,
        onProgress: (Int) -> Unit,
        onHost: (NetHost) -> Unit
    ) = coroutineScope {
        val prefix = localIp.substringBeforeLast('.')
        val gateway = "$prefix.1"
        val semaphore = Semaphore(48)
        val done = AtomicInteger(0)
        (1..254).map { i ->
            launch(Dispatchers.IO) {
                semaphore.withPermit {
                    ensureActive()
                    val ip = "$prefix.$i"
                    val isSelf = ip == localIp
                    val latency = if (isSelf) 0L else probe(ip)
                    if (latency != null) {
                        val name = if (isSelf) Build.MODEL.orEmpty() else hostName(ip)
                        onHost(NetHost(ip, name, latency, isSelf, ip == gateway))
                    }
                    onProgress(done.incrementAndGet())
                }
            }
        }.joinAll()
    }

    suspend fun scanPorts(
        host: String,
        ports: List<Pair<Int, String>>,
        onProgress: (Int) -> Unit,
        onOpen: (Pair<Int, String>) -> Unit
    ) = coroutineScope {
        val semaphore = Semaphore(32)
        val done = AtomicInteger(0)
        val ip = try {
            InetAddress.getByName(host).hostAddress ?: host
        } catch (e: Exception) {
            host
        }
        ports.map { p ->
            launch(Dispatchers.IO) {
                semaphore.withPermit {
                    ensureActive()
                    if (tcpOpen(ip, p.first, 500)) onOpen(p)
                    onProgress(done.incrementAndGet())
                }
            }
        }.joinAll()
    }
}
