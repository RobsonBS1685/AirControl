package com.example.aircontrol

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        // -----------------------------------------
        // FIREBASE
        // -----------------------------------------

        auth = FirebaseAuth.getInstance()

        // -----------------------------------------
        // ELEMENTOS DEL LOGIN
        // -----------------------------------------

        val etUsuario =
            findViewById<EditText>(R.id.etUsuario)

        val etPassword =
            findViewById<EditText>(R.id.etPassword)

        val btnVerPassword =
            findViewById<ImageButton>(R.id.btnVerPassword)

        val tvErrorUsuario =
            findViewById<TextView>(R.id.tvErrorUsuario)

        val tvErrorPassword =
            findViewById<TextView>(R.id.tvErrorPassword)

        val cbRecordarme =
            findViewById<CheckBox>(R.id.cbRecordarme)

        val btnIngresar =
            findViewById<Button>(R.id.btnIngresar)

        val tvRegistrarse =
            findViewById<TextView>(R.id.tvRegistrarse)

        // -----------------------------------------
        // SHARED PREFERENCES
        // -----------------------------------------

        val preferencias =
            getSharedPreferences(
                "AirControlPreferencias",
                MODE_PRIVATE
            )

        // Comprobar si anteriormente se marcó
        // la opción Recordarme
        val recordarUsuario =
            preferencias.getBoolean(
                "recordarme",
                false
            )

        if (recordarUsuario) {

            val correoGuardado =
                preferencias.getString(
                    "correo",
                    ""
                )

            etUsuario.setText(correoGuardado)

            cbRecordarme.isChecked = true
        }

        // -----------------------------------------
        // MOSTRAR / OCULTAR CONTRASEÑA
        // -----------------------------------------

        var passwordVisible = false

        btnVerPassword.setOnClickListener {

            passwordVisible = !passwordVisible

            if (passwordVisible) {

                // Mostrar contraseña
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                // Candado abierto
                btnVerPassword.setImageResource(
                    R.drawable.ic_lock_open
                )

                btnVerPassword.contentDescription =
                    "Ocultar contraseña"

            } else {

                // Ocultar contraseña
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                // Candado cerrado
                btnVerPassword.setImageResource(
                    R.drawable.ic_lock
                )

                btnVerPassword.contentDescription =
                    "Mostrar contraseña"
            }

            // Mantener cursor al final
            etPassword.setSelection(
                etPassword.text.length
            )
        }

        // -----------------------------------------
        // BOTÓN INGRESAR
        // -----------------------------------------

        btnIngresar.setOnClickListener {

            val email =
                etUsuario.text.toString().trim()

            val password =
                etPassword.text.toString()

            var formularioValido = true

            // Ocultar mensajes anteriores
            tvErrorUsuario.visibility =
                View.GONE

            tvErrorPassword.visibility =
                View.GONE

            tvErrorUsuario.text = ""
            tvErrorPassword.text = ""

            // -------------------------------------
            // VALIDACIÓN DEL CORREO
            // -------------------------------------

            if (email.isEmpty()) {

                tvErrorUsuario.text =
                    " Ingresa tu correo electrónico !"

                tvErrorUsuario.visibility =
                    View.VISIBLE

                formularioValido = false

            } else if (
                !email.matches(
                    Regex(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                    )
                )
            ) {

                tvErrorUsuario.text =
                    " Ingresa un correo electrónico válido !"

                tvErrorUsuario.visibility =
                    View.VISIBLE

                formularioValido = false
            }

            // -------------------------------------
            // VALIDACIÓN DE CONTRASEÑA
            // -------------------------------------

            if (password.isEmpty()) {

                tvErrorPassword.text =
                    " Ingresa tu contraseña !"

                tvErrorPassword.visibility =
                    View.VISIBLE

                formularioValido = false
            }

            // Si existen errores mostramos TODOS
            // y no consultamos Firebase
            if (!formularioValido) {
                return@setOnClickListener
            }

            // -------------------------------------
            // INICIAR SESIÓN CON FIREBASE
            // -------------------------------------

            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // ---------------------------------
                    // RECORDAR CORREO
                    // ---------------------------------

                    if (cbRecordarme.isChecked) {

                        preferencias.edit()
                            .putBoolean(
                                "recordarme",
                                true
                            )
                            .putString(
                                "correo",
                                email
                            )
                            .apply()

                    } else {

                        // Si Recordarme no está marcado,
                        // eliminamos cualquier correo
                        // guardado anteriormente.
                        preferencias.edit()
                            .putBoolean(
                                "recordarme",
                                false
                            )
                            .remove("correo")
                            .apply()
                    }

                    // Limpiar mensajes
                    tvErrorUsuario.visibility =
                        View.GONE

                    tvErrorPassword.visibility =
                        View.GONE

                    Toast.makeText(
                        this,
                        "Inicio de sesión correcto",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Abrir Bienvenida
                    val intent =
                        Intent(
                            this,
                            BienvenidaActivity::class.java
                        )

                    intent.putExtra(
                        "email",
                        email
                    )

                    startActivity(intent)

                } else {

                    // Por seguridad no indicamos
                    // específicamente si falló
                    // el correo o la contraseña.
                    tvErrorPassword.text =
                        " Correo o contraseña incorrectos !"

                    tvErrorPassword.visibility =
                        View.VISIBLE
                }
            }
        }

        // -----------------------------------------
        // IR A REGISTRO
        // -----------------------------------------

        tvRegistrarse.setOnClickListener {

            val intent =
                Intent(
                    this,
                    RegistroActivity::class.java
                )

            startActivity(intent)
        }
    }
}