package com.example.aircontrol.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aircontrol.R
import com.example.aircontrol.models.RegistroAire

class RegistroAireAdapter(
    private val listaRegistros: MutableList<RegistroAire>,
    private val onEditar: (RegistroAire) -> Unit,
    private val onEliminar: (RegistroAire) -> Unit
) : RecyclerView.Adapter<RegistroAireAdapter.RegistroViewHolder>() {

    class RegistroViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val txtTemperatura: TextView =
            itemView.findViewById(R.id.txtItemTemperatura)

        val txtHumedad: TextView =
            itemView.findViewById(R.id.txtItemHumedad)

        val txtEstadoVentilador: TextView =
            itemView.findViewById(R.id.txtItemEstado)

        val btnEditar: Button =
            itemView.findViewById(R.id.btnEditar)

        val btnEliminar: Button =
            itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RegistroViewHolder {

        val vista = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_registro_aire,
                parent,
                false
            )

        return RegistroViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: RegistroViewHolder,
        position: Int
    ) {

        val registro = listaRegistros[position]

        // -------------------------------------------------
        // TEMPERATURA
        // -------------------------------------------------

        holder.txtTemperatura.text =
            "Temperatura: ${registro.temperatura} °C"

        // -------------------------------------------------
        // HUMEDAD
        // -------------------------------------------------

        holder.txtHumedad.text =
            "Humedad: ${registro.humedad} %"

        // -------------------------------------------------
        // ESTADO DEL VENTILADOR
        // -------------------------------------------------

        holder.txtEstadoVentilador.text =
            "Ventilador: ${registro.estadoVentilador}"

        // Cambiar color según el estado
        if (registro.estadoVentilador == "Encendido") {

            // Verde
            holder.txtEstadoVentilador.setTextColor(
                Color.parseColor("#059669")
            )

        } else {

            // Gris
            holder.txtEstadoVentilador.setTextColor(
                Color.parseColor("#64748B")
            )
        }

        // -------------------------------------------------
        // BOTÓN EDITAR
        // -------------------------------------------------

        holder.btnEditar.setOnClickListener {
            onEditar(registro)
        }

        // -------------------------------------------------
        // BOTÓN ELIMINAR
        // -------------------------------------------------

        holder.btnEliminar.setOnClickListener {
            onEliminar(registro)
        }
    }

    override fun getItemCount(): Int {
        return listaRegistros.size
    }
}