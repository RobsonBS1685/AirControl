package com.example.aircontrol

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BienvenidaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Componentes de la pantalla
        val txtCorreoUsuario =
            findViewById<TextView>(R.id.txtCorreoUsuario)

        val switchAlertas =
            findViewById<SwitchCompat>(R.id.switchAlertas)

        val spinnerTemperatura =
            findViewById<Spinner>(R.id.spinnerTemperatura)

        val btnRegistros =
            findViewById<Button>(R.id.btnRegistros)

        // -------------------------------------------------
        // MOSTRAR NOMBRE DEL USUARIO
        // -------------------------------------------------

        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {

            val uid = usuarioActual.uid

            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { documento ->

                    val nombre =
                        documento.getString("nombre")

                    if (!nombre.isNullOrEmpty()) {

                        txtCorreoUsuario.text =
                            "Bienvenido! $nombre \uD83C\uDF43"

                    } else {

                        txtCorreoUsuario.text =
                            "Bienvenido! \uD83C\uDF43"
                    }
                }
                .addOnFailureListener {

                    txtCorreoUsuario.text =
                        "Bienvenido! \uD83C\uDF43"
                }

        } else {

            txtCorreoUsuario.text =
                "Bienvenido! \uD83C\uDF43"
        }

        // -------------------------------------------------
        // TEMPERATURAS DISPONIBLES
        // -------------------------------------------------

        val temperaturas = arrayOf(
            "26 °C",
            "28 °C",
            "30 °C",
            "32 °C"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            temperaturas
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTemperatura.adapter = adapter

        // 28 °C seleccionado por defecto
        spinnerTemperatura.setSelection(1)

        // -------------------------------------------------
        // SWITCH DE ALERTAS
        // -------------------------------------------------

        switchAlertas.setOnCheckedChangeListener { _, activado ->

            if (activado) {

                val temperaturaSeleccionada =
                    spinnerTemperatura.selectedItem.toString()

                Toast.makeText(
                    this,
                    "Alertas activadas desde $temperaturaSeleccionada",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Alertas de temperatura desactivadas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // -------------------------------------------------
        // ABRIR REGISTROS
        // -------------------------------------------------

        btnRegistros.setOnClickListener {

            val intent = Intent(
                this,
                RegistrosActivity::class.java
            )

            startActivity(intent)
        }
    }
}