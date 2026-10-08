package com.example.aircontrol

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Cloud Firestore
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Campos
        val etNombre =
            findViewById<EditText>(R.id.etNombre)

        val etEmail =
            findViewById<EditText>(R.id.etEmailRegistro)

        val etPassword =
            findViewById<EditText>(R.id.etPasswordRegistro)

        val etConfirmarPassword =
            findViewById<EditText>(R.id.etConfirmarPassword)

        // Mensajes de error
        val tvErrorNombre =
            findViewById<TextView>(R.id.tvErrorNombre)

        val tvErrorEmail =
            findViewById<TextView>(R.id.tvErrorEmailRegistro)

        val tvErrorPassword =
            findViewById<TextView>(R.id.tvErrorPasswordRegistro)

        val tvErrorConfirmarPassword =
            findViewById<TextView>(R.id.tvErrorConfirmarPassword)

        // Botones
        val btnRegistrar =
            findViewById<Button>(R.id.btnRegistrar)

        val tvVolverLogin =
            findViewById<TextView>(R.id.tvVolverLogin)

        // -------------------------------------------------
        // BOTÓN REGISTRAR
        // -------------------------------------------------

        btnRegistrar.setOnClickListener {

            val nombre =
                etNombre.text.toString().trim()

            val email =
                etEmail.text.toString().trim()

            val password =
                etPassword.text.toString()

            val confirmarPassword =
                etConfirmarPassword.text.toString()

            var formularioValido = true

            // Ocultar errores anteriores
            tvErrorNombre.visibility = View.GONE
            tvErrorEmail.visibility = View.GONE
            tvErrorPassword.visibility = View.GONE
            tvErrorConfirmarPassword.visibility = View.GONE

            // -------------------------------------------------
            // VALIDAR NOMBRE
            // -------------------------------------------------

            if (nombre.isEmpty()) {

                tvErrorNombre.text =
                    " Ingresa tu nombre !"

                tvErrorNombre.visibility =
                    View.VISIBLE

                formularioValido = false
            }

            // -------------------------------------------------
            // VALIDAR CORREO
            // -------------------------------------------------

            if (email.isEmpty()) {

                tvErrorEmail.text =
                    " Ingresa tu correo electrónico !"

                tvErrorEmail.visibility =
                    View.VISIBLE

                formularioValido = false

            } else if (
                !email.matches(
                    Regex(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                    )
                )
            ) {

                tvErrorEmail.text =
                    " Ingresa un correo electrónico válido !"

                tvErrorEmail.visibility =
                    View.VISIBLE

                formularioValido = false
            }

            // -------------------------------------------------
            // VALIDAR CONTRASEÑA
            // -------------------------------------------------

            if (password.isEmpty()) {

                tvErrorPassword.text =
                    " Ingresa una contraseña !"

                tvErrorPassword.visibility =
                    View.VISIBLE

                formularioValido = false

            } else if (password.length < 6) {

                tvErrorPassword.text =
                    " La contraseña debe tener mínimo 6 caracteres !"

                tvErrorPassword.visibility =
                    View.VISIBLE

                formularioValido = false

            } else if (!password.any { it.isUpperCase() }) {

                tvErrorPassword.text =
                    " La contraseña debe incluir una letra mayúscula !"

                tvErrorPassword.visibility =
                    View.VISIBLE

                formularioValido = false

            } else if (!password.any { it.isLowerCase() }) {

                tvErrorPassword.text =
                    " La contraseña debe incluir una letra minúscula !"

                tvErrorPassword.visibility =
                    View.VISIBLE

                formularioValido = false

            } else if (!password.any { it.isDigit() }) {

                tvErrorPassword.text =
                    " La contraseña debe incluir al menos un número !"

                tvErrorPassword.visibility =
                    View.VISIBLE

                formularioValido = false
            }

            // -------------------------------------------------
            // VALIDAR CONFIRMACIÓN
            // -------------------------------------------------

            if (confirmarPassword.isEmpty()) {

                tvErrorConfirmarPassword.text =
                    " Confirma tu contraseña !"

                tvErrorConfirmarPassword.visibility =
                    View.VISIBLE

                formularioValido = false

            } else if (password != confirmarPassword) {

                tvErrorConfirmarPassword.text =
                    " Las contraseñas no coinciden !"

                tvErrorConfirmarPassword.visibility =
                    View.VISIBLE

                formularioValido = false
            }

            // Si existe algún error,
            // NO conectarse a Firebase
            if (!formularioValido) {
                return@setOnClickListener
            }

            // -------------------------------------------------
            // CREAR USUARIO EN FIREBASE AUTHENTICATION
            // -------------------------------------------------

            auth.createUserWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // Obtener ID único del usuario
                    val uid =
                        auth.currentUser?.uid

                    if (uid != null) {

                        // Datos para Firestore
                        val usuario = hashMapOf(
                            "nombre" to nombre,
                            "email" to email
                        )

                        // Guardar usuario
                        db.collection("usuarios")
                            .document(uid)
                            .set(usuario)
                            .addOnSuccessListener {

                                Toast.makeText(
                                    this,
                                    "Usuario registrado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Ir a Bienvenida
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

                                finish()
                            }
                            .addOnFailureListener {

                                Toast.makeText(
                                    this,
                                    "No se pudo guardar la información del usuario",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }

                } else {

                    // Firebase no pudo crear la cuenta
                    tvErrorEmail.text =
                        " No se pudo crear la cuenta. Verifica el correo e inténtalo nuevamente !"

                    tvErrorEmail.visibility =
                        View.VISIBLE
                }
            }
        }

        // -------------------------------------------------
        // VOLVER AL LOGIN
        // -------------------------------------------------

        tvVolverLogin.setOnClickListener {
            finish()
        }
    }
}