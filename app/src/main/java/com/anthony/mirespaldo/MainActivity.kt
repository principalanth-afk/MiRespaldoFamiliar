package com.anthony.mirespaldo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("mirespaldo", Context.MODE_PRIVATE)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etPcIp = findViewById<EditText>(R.id.etPcIp)
        val grupoModo = findViewById<RadioGroup>(R.id.grupoModo)
        val rbSiempre = findViewById<RadioButton>(R.id.rbSiempre)
        val rbManual = findViewById<RadioButton>(R.id.rbManual)
        val swActivo = findViewById<Switch>(R.id.swActivo)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val txtEstado = findViewById<TextView>(R.id.txtEstado)

        // Cargar lo guardado
        etNombre.setText(prefs.getString("nombre_dispositivo", ""))
        etPcIp.setText(prefs.getString("pc_ip", "192.168.0."))
        val modo = prefs.getString("modo", "manual")
        if (modo == "siempre") rbSiempre.isChecked = true else rbManual.isChecked = true
        swActivo.isChecked = prefs.getBoolean("activo_ahora", false)
        actualizarVisibilidadSwitch(grupoModo, swActivo)

        grupoModo.setOnCheckedChangeListener { _, _ -> actualizarVisibilidadSwitch(grupoModo, swActivo) }

        // El switch de "activo ahora" (para modo manual)
        swActivo.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("activo_ahora", isChecked).apply()
            txtEstado.text = if (isChecked) "✅ Compartiendo ahora" else "⏸️ Pausado"
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val pcIp = etPcIp.text.toString().trim()
            if (nombre.isEmpty()) { toast("Escribe un nombre para este teléfono"); return@setOnClickListener }

            val modoSel = if (rbSiempre.isChecked) "siempre" else "manual"
            prefs.edit()
                .putString("nombre_dispositivo", nombre)
                .putString("pc_ip", pcIp)
                .putString("modo", modoSel)
                .apply()

            pedirPermisos()
            iniciarServicio()
            txtEstado.text = if (modoSel == "siempre") "✅ Compartiendo siempre con la PC"
                             else if (swActivo.isChecked) "✅ Compartiendo ahora" else "⏸️ Listo (activa el interruptor para compartir)"
            toast("Guardado ✓")
        }
    }

    private fun actualizarVisibilidadSwitch(grupo: RadioGroup, sw: Switch) {
        val manual = findViewById<RadioButton>(R.id.rbManual).isChecked
        sw.visibility = if (manual) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun iniciarServicio() {
        val i = Intent(this, ServidorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i)
        else startService(i)
    }

    private fun pedirPermisos() {
        val permisos = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.READ_MEDIA_IMAGES)
            permisos.add(Manifest.permission.READ_MEDIA_VIDEO)
            permisos.add(Manifest.permission.READ_MEDIA_AUDIO)
            permisos.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val faltan = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (faltan.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, faltan.toTypedArray(), 100)
        }
    }

    private fun toast(t: String) = Toast.makeText(this, t, Toast.LENGTH_SHORT).show()
}
