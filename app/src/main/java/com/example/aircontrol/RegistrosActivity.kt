package com.example.aircontrol

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aircontrol.adapters.RegistroAireAdapter
import com.example.aircontrol.models.RegistroAire
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrosActivity : AppCompatActivity() {

    private lateinit var etTemperatura: EditText
    private lateinit var etHumedad: EditText
    private lateinit var txtEstadoVentilador: TextView
    private lateinit var btnGuardarRegistro: Button
    private lateinit var recyclerRegistros: RecyclerView

    private lateinit var db: FirebaseFirestore

    private val listaRegistros = mutableListOf<RegistroAire>()

    private lateinit var adapter: RegistroAireAdapter

    private var registroEditando: RegistroAire? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registros)

        // -------------------------------------------------
        // CONECTAR COMPONENTES DEL XML
        // -------------------------------------------------

        etTemperatura =
            findViewById(R.id.etTemperatura)

        etHumedad =
            findViewById(R.id.etHumedad)

        txtEstadoVentilador =
            findViewById(R.id.txtEstadoVentilador)

        btnGuardarRegistro =
            findViewById(R.id.btnGuardarRegistro)

        recyclerRegistros =
            findViewById(R.id.recyclerRegistros)

        // -------------------------------------------------
        // FIREBASE
        // -------------------------------------------------

        db = FirebaseFirestore.getInstance()

        // -------------------------------------------------
        // RECYCLERVIEW
        // -------------------------------------------------

        adapter = RegistroAireAdapter(
            listaRegistros,

            onEditar = { registro ->
                cargarRegistroParaEditar(registro)
            },

            onEliminar = { registro ->
                confirmarEliminarRegistro(registro)
            }
        )

        recyclerRegistros.layoutManager =
            LinearLayoutManager(this)

        recyclerRegistros.adapter =
            adapter

        // -------------------------------------------------
        // ESTADO INICIAL
        // -------------------------------------------------

        mostrarEstadoVentilador("Apagado")

        // -------------------------------------------------
        // ACTUALIZAR ESTADO AUTOMÁTICAMENTE
        // -------------------------------------------------

        etTemperatura.setOnFocusChangeListener { _, hasFocus ->

            if (!hasFocus) {
                actualizarEstadoVisual()
            }
        }

        etHumedad.setOnFocusChangeListener { _, hasFocus ->

            if (!hasFocus) {
                actualizarEstadoVisual()
            }
        }

        // -------------------------------------------------
        // BOTÓN GUARDAR / ACTUALIZAR
        // -------------------------------------------------

        btnGuardarRegistro.setOnClickListener {

            if (registroEditando == null) {
                guardarRegistro()
            } else {
                actualizarRegistro()
            }
        }

        // -------------------------------------------------
        // CARGAR REGISTROS
        // -------------------------------------------------

        escucharRegistros()
    }

    // =====================================================
    // CALCULAR ESTADO DEL VENTILADOR
    // =====================================================

    private fun calcularEstadoVentilador(
        temperatura: Double,
        humedad: Double
    ): String {

        return if (
            temperatura > 28 ||
            humedad > 60
        ) {
            "Encendido"
        } else {
            "Apagado"
        }
    }

    // =====================================================
    // MOSTRAR ESTADO Y COLOR
    // =====================================================

    private fun mostrarEstadoVentilador(
        estado: String
    ) {

        txtEstadoVentilador.text =
            "Ventilador: $estado"

        if (estado == "Encendido") {

            // Verde
            txtEstadoVentilador.setTextColor(
                Color.parseColor("#059669")
            )

        } else {

            // Gris
            txtEstadoVentilador.setTextColor(
                Color.parseColor("#64748B")
            )
        }
    }

    // =====================================================
    // ACTUALIZAR ESTADO VISUAL
    // =====================================================

    private fun actualizarEstadoVisual() {

        val temperatura =
            etTemperatura.text
                .toString()
                .toDoubleOrNull()

        val humedad =
            etHumedad.text
                .toString()
                .toDoubleOrNull()

        if (
            temperatura == null ||
            humedad == null
        ) {

            mostrarEstadoVentilador("Apagado")

            return
        }

        val estado =
            calcularEstadoVentilador(
                temperatura,
                humedad
            )

        mostrarEstadoVentilador(estado)
    }

    // =====================================================
    // GUARDAR REGISTRO
    // =====================================================

    private fun guardarRegistro() {

        etTemperatura.error = null
        etHumedad.error = null

        val temperaturaTexto =
            etTemperatura.text
                .toString()
                .trim()

        val humedadTexto =
            etHumedad.text
                .toString()
                .trim()

        if (temperaturaTexto.isEmpty()) {

            etTemperatura.error =
                "Ingrese la temperatura"

            etTemperatura.requestFocus()

            return
        }

        if (humedadTexto.isEmpty()) {

            etHumedad.error =
                "Ingrese la humedad"

            etHumedad.requestFocus()

            return
        }

        val temperatura =
            temperaturaTexto.toDoubleOrNull()

        val humedad =
            humedadTexto.toDoubleOrNull()

        if (temperatura == null) {

            etTemperatura.error =
                "Ingrese una temperatura válida"

            etTemperatura.requestFocus()

            return
        }

        if (humedad == null) {

            etHumedad.error =
                "Ingrese una humedad válida"

            etHumedad.requestFocus()

            return
        }

        // -------------------------------------------------
        // CALCULAR VENTILADOR AUTOMÁTICAMENTE
        // -------------------------------------------------

        val estadoVentilador =
            calcularEstadoVentilador(
                temperatura,
                humedad
            )

        mostrarEstadoVentilador(
            estadoVentilador
        )

        // -------------------------------------------------
        // USUARIO CONECTADO
        // -------------------------------------------------

        val usuarioId =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

        if (usuarioId == null) {

            Toast.makeText(
                this,
                "No hay un usuario conectado",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // -------------------------------------------------
        // CREAR REGISTRO
        // -------------------------------------------------

        val registro = RegistroAire(
            temperatura = temperatura,
            humedad = humedad,
            estadoVentilador = estadoVentilador,
            usuarioId = usuarioId
        )

        // -------------------------------------------------
        // GUARDAR EN FIRESTORE
        // -------------------------------------------------

        db.collection("registros_aire")
            .add(registro)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Registro guardado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                limpiarCampos()
            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Error al guardar el registro",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // =====================================================
    // CARGAR REGISTRO PARA EDITAR
    // =====================================================

    private fun cargarRegistroParaEditar(
        registro: RegistroAire
    ) {

        registroEditando = registro

        etTemperatura.setText(
            registro.temperatura.toString()
        )

        etHumedad.setText(
            registro.humedad.toString()
        )

        mostrarEstadoVentilador(
            registro.estadoVentilador
        )

        btnGuardarRegistro.text =
            "Actualizar registro"

        Toast.makeText(
            this,
            "Modifique los datos y presione Actualizar registro",
            Toast.LENGTH_SHORT
        ).show()
    }

    // =====================================================
    // ACTUALIZAR REGISTRO
    // =====================================================

    private fun actualizarRegistro() {

        val registro =
            registroEditando ?: return

        etTemperatura.error = null
        etHumedad.error = null

        val temperaturaTexto =
            etTemperatura.text
                .toString()
                .trim()

        val humedadTexto =
            etHumedad.text
                .toString()
                .trim()

        if (temperaturaTexto.isEmpty()) {

            etTemperatura.error =
                "Ingrese la temperatura"

            etTemperatura.requestFocus()

            return
        }

        if (humedadTexto.isEmpty()) {

            etHumedad.error =
                "Ingrese la humedad"

            etHumedad.requestFocus()

            return
        }

        val temperatura =
            temperaturaTexto.toDoubleOrNull()

        val humedad =
            humedadTexto.toDoubleOrNull()

        if (temperatura == null) {

            etTemperatura.error =
                "Ingrese una temperatura válida"

            etTemperatura.requestFocus()

            return
        }

        if (humedad == null) {

            etHumedad.error =
                "Ingrese una humedad válida"

            etHumedad.requestFocus()

            return
        }

        // -------------------------------------------------
        // RECALCULAR ESTADO AUTOMÁTICAMENTE
        // -------------------------------------------------

        val estadoVentilador =
            calcularEstadoVentilador(
                temperatura,
                humedad
            )

        mostrarEstadoVentilador(
            estadoVentilador
        )

        // -------------------------------------------------
        // VERIFICAR ID
        // -------------------------------------------------

        if (registro.id.isEmpty()) {

            Toast.makeText(
                this,
                "No se pudo identificar el registro",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // -------------------------------------------------
        // DATOS ACTUALIZADOS
        // -------------------------------------------------

        val datosActualizados =
            hashMapOf<String, Any>(
                "temperatura" to temperatura,
                "humedad" to humedad,
                "estadoVentilador" to estadoVentilador
            )

        // -------------------------------------------------
        // ACTUALIZAR FIRESTORE
        // -------------------------------------------------

        db.collection("registros_aire")
            .document(registro.id)
            .update(datosActualizados)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Registro actualizado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                registroEditando = null

                limpiarCampos()

                btnGuardarRegistro.text =
                    "Guardar registro"
            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Error al actualizar el registro",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // =====================================================
    // CONFIRMAR ELIMINACIÓN
    // =====================================================

    private fun confirmarEliminarRegistro(
        registro: RegistroAire
    ) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage(
                "¿Está seguro de que desea eliminar este registro?"
            )

            .setPositiveButton("Eliminar") { _, _ ->
                eliminarRegistro(registro)
            }

            .setNegativeButton(
                "Cancelar",
                null
            )

            .show()
    }

    // =====================================================
    // ELIMINAR REGISTRO
    // =====================================================

    private fun eliminarRegistro(
        registro: RegistroAire
    ) {

        if (registro.id.isEmpty()) {

            Toast.makeText(
                this,
                "No se pudo identificar el registro",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        db.collection("registros_aire")
            .document(registro.id)
            .delete()

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Registro eliminado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                if (
                    registroEditando?.id ==
                    registro.id
                ) {

                    registroEditando = null

                    limpiarCampos()

                    btnGuardarRegistro.text =
                        "Guardar registro"
                }
            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Error al eliminar el registro",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // =====================================================
    // LIMPIAR CAMPOS
    // =====================================================

    private fun limpiarCampos() {

        etTemperatura.text.clear()

        etHumedad.text.clear()

        mostrarEstadoVentilador(
            "Apagado"
        )
    }

    // =====================================================
    // ESCUCHAR REGISTROS DEL USUARIO
    // =====================================================

    private fun escucharRegistros() {

        val usuarioId =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

        if (usuarioId == null) {

            Toast.makeText(
                this,
                "No hay un usuario conectado",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        db.collection("registros_aire")

            .whereEqualTo(
                "usuarioId",
                usuarioId
            )

            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    Toast.makeText(
                        this,
                        "Error al cargar los registros",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    listaRegistros.clear()

                    for (
                    documento
                    in snapshot.documents
                    ) {

                        val registro =
                            documento.toObject(
                                RegistroAire::class.java
                            )

                        if (registro != null) {

                            registro.id =
                                documento.id

                            listaRegistros.add(
                                registro
                            )
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
            }
    }
}