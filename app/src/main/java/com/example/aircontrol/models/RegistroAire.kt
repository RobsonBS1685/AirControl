package com.example.aircontrol.models

data class RegistroAire(

    var id: String = "",

    var temperatura: Double = 0.0,

    var humedad: Double = 0.0,

    var estadoVentilador: String = "",

    var usuarioId: String = ""
)