package com.anthony.mirespaldo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Servicio en primer plano:
 *  - Arranca el ServidorArchivos dentro del teléfono.
 *  - Cada cierto tiempo "avisa" a la PC de Anthony que este teléfono
 *    está disponible (registro/heartbeat), enviando su IP y nombre.
 */
class ServidorService : Service() {

    private var servidor: ServidorArchivos? = null
    private var vivo = true
    private val PUERTO = 8787
    private val CANAL = "mirespaldo_canal"

    override fun onCreate() {
        super.onCreate()
        crearCanal()
        startForeground(1, notificacion("Compartiendo con la PC de la casa"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("mirespaldo", Context.MODE_PRIVATE)
        val nombre = prefs.getString("nombre_dispositivo", "Teléfono") ?: "Teléfono"

        // Arranca el servidor si no está
        if (servidor == null) {
            servidor = ServidorArchivos(
                ctx = applicationContext,
                puerto = PUERTO,
                nombreDispositivo = nombre,
                permitidoAhora = { accesoPermitido() }
            )
            try { servidor?.start(NanoConst.SOCKET_TIMEOUT, false) } catch (_: Exception) {}
        }

        // Hilo de registro/heartbeat con la PC
        thread(start = true) {
            while (vivo) {
                try {
                    if (accesoPermitido()) registrarEnPC(nombre)
                } catch (_: Exception) {}
                Thread.sleep(15000) // cada 15 s
            }
        }

        return START_STICKY
    }

    private fun accesoPermitido(): Boolean {
        val prefs = getSharedPreferences("mirespaldo", Context.MODE_PRIVATE)
        // modo: "siempre" o "manual"; si es manual, mira el switch "activo_ahora"
        val modo = prefs.getString("modo", "manual")
        return if (modo == "siempre") true
        else prefs.getBoolean("activo_ahora", false)
    }

    /** Avisa a la PC (servidor Node del barrio) que este teléfono está disponible. */
    private fun registrarEnPC(nombre: String) {
        val prefs = getSharedPreferences("mirespaldo", Context.MODE_PRIVATE)
        val pcIp = prefs.getString("pc_ip", "") ?: ""
        if (pcIp.isBlank()) return

        val miIp = obtenerIpWifi() ?: return
        val body = JSONObject()
        body.put("nombre", nombre)
        body.put("ip", miIp)
        body.put("puerto", PUERTO)
        body.put("tipo", "android")

        val url = URL("http://$pcIp:3000/registrar-dispositivo")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.doOutput = true
        con.connectTimeout = 4000
        con.readTimeout = 4000
        con.setRequestProperty("Content-Type", "application/json")
        con.outputStream.use { it.write(body.toString().toByteArray()) }
        con.inputStream.use { it.readBytes() }
        con.disconnect()
    }

    private fun obtenerIpWifi(): String? {
        return try {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wm.connectionInfo.ipAddress
            if (ip == 0) null
            else String.format(
                "%d.%d.%d.%d",
                ip and 0xff, ip shr 8 and 0xff, ip shr 16 and 0xff, ip shr 24 and 0xff
            )
        } catch (e: Exception) { null }
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL, "MiRespaldo",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(canal)
        }
    }

    private fun notificacion(texto: String): Notification {
        return NotificationCompat.Builder(this, CANAL)
            .setContentTitle("MiRespaldo Familiar")
            .setContentText(texto)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        vivo = false
        servidor?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

/** Constante de timeout para NanoHTTPD. */
object NanoConst { const val SOCKET_TIMEOUT = 10000 }
