package com.anthony.mirespaldo

import android.content.Context
import android.os.Environment
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

/**
 * Mini-servidor que corre DENTRO del teléfono.
 * Expone (solo lectura) las carpetas del almacenamiento para que
 * la PC de Anthony pueda explorar y respaldar, CON consentimiento.
 *
 * Endpoints:
 *   GET /            -> info del dispositivo (ping)
 *   GET /listar?ruta=...     -> JSON con carpetas y archivos de esa ruta
 *   GET /descargar?ruta=...  -> descarga el archivo
 *   GET /estado      -> dice si el acceso está permitido ahora mismo
 */
class ServidorArchivos(
    private val ctx: Context,
    private val puerto: Int = 8787,
    private val nombreDispositivo: String,
    private val permitidoAhora: () -> Boolean
) : NanoHTTPD(puerto) {

    // Raíz permitida: el almacenamiento interno del teléfono
    private val raiz: File = Environment.getExternalStorageDirectory()

    override fun serve(session: IHTTPSession): Response {
        // CORS para que la PC (otra dirección) pueda consultar
        val headers = session.headers
        try {
            // Si el dueño apagó el acceso, todo queda bloqueado
            if (!permitidoAhora() && session.uri != "/estado") {
                return cors(newFixedLengthResponse(
                    Response.Status.FORBIDDEN, "application/json",
                    "{\"ok\":false,\"motivo\":\"acceso_desactivado\"}"
                ))
            }

            return when (session.uri) {
                "/" -> info()
                "/estado" -> estado()
                "/listar" -> listar(session.parameters["ruta"]?.firstOrNull() ?: "")
                "/descargar" -> descargar(session.parameters["ruta"]?.firstOrNull() ?: "")
                else -> cors(newFixedLengthResponse(
                    Response.Status.NOT_FOUND, "text/plain", "No existe"
                ))
            }
        } catch (e: Exception) {
            return cors(newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR, "application/json",
                "{\"ok\":false,\"error\":\"${e.message}\"}"
            ))
        }
    }

    private fun info(): Response {
        val o = JSONObject()
        o.put("ok", true)
        o.put("dispositivo", nombreDispositivo)
        o.put("tipo", "android")
        o.put("app", "MiRespaldoFamiliar")
        return cors(newFixedLengthResponse(Response.Status.OK, "application/json", o.toString()))
    }

    private fun estado(): Response {
        val o = JSONObject()
        o.put("ok", true)
        o.put("permitido", permitidoAhora())
        o.put("dispositivo", nombreDispositivo)
        return cors(newFixedLengthResponse(Response.Status.OK, "application/json", o.toString()))
    }

    /** Resuelve una ruta relativa de forma segura (no deja salir de la raíz). */
    private fun resolverSeguro(rutaRel: String): File? {
        val limpio = rutaRel.replace("..", "").trimStart('/')
        val f = File(raiz, limpio)
        val canon = f.canonicalFile
        return if (canon.path.startsWith(raiz.canonicalFile.path)) canon else null
    }

    private fun listar(rutaRel: String): Response {
        val dir = resolverSeguro(rutaRel) ?: return cors(newFixedLengthResponse(
            Response.Status.FORBIDDEN, "application/json", "{\"ok\":false}"
        ))
        val arrCarpetas = JSONArray()
        val arrArchivos = JSONArray()
        dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))?.forEach { f ->
            if (f.name.startsWith(".")) return@forEach // ocultos fuera
            if (f.isDirectory) {
                arrCarpetas.put(f.name)
            } else {
                val a = JSONObject()
                a.put("nombre", f.name)
                a.put("kb", (f.length() / 1024).toInt())
                arrArchivos.put(a)
            }
        }
        val o = JSONObject()
        o.put("ok", true)
        o.put("ruta", rutaRel)
        o.put("carpetas", arrCarpetas)
        o.put("archivos", arrArchivos)
        return cors(newFixedLengthResponse(Response.Status.OK, "application/json", o.toString()))
    }

    private fun descargar(rutaRel: String): Response {
        val f = resolverSeguro(rutaRel) ?: return cors(newFixedLengthResponse(
            Response.Status.FORBIDDEN, "text/plain", "Prohibido"
        ))
        if (!f.exists() || f.isDirectory) return cors(newFixedLengthResponse(
            Response.Status.NOT_FOUND, "text/plain", "No existe"
        ))
        val fis = FileInputStream(f)
        val resp = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis)
        resp.addHeader("Content-Disposition", "attachment; filename=\"${f.name}\"")
        return cors(resp)
    }

    private fun cors(r: Response): Response {
        r.addHeader("Access-Control-Allow-Origin", "*")
        r.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
        r.addHeader("Access-Control-Allow-Headers", "*")
        return r
    }
}
