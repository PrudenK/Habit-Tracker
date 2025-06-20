package com.pruden.habits.modules.agregarEditarHabitoModule.model

import com.pruden.habits.HabitosApplication
import com.pruden.habits.common.clases.entities.DataHabitoEntity
import com.pruden.habits.common.clases.entities.HabitoEntity
import com.pruden.habits.common.metodos.fechas.generarFechasFormatoYYYYMMDD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgregarHabitoEditarInteractor {
    fun obtenerTodosLosNombresDeLosHabitos(callback: (MutableList<String>) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            val listaNombres = HabitosApplication.database.habitoDao().obtenerTdosLosNombres()
            withContext(Dispatchers.Main){
                callback(listaNombres)
            }
        }
    }

    fun insertarHabito(habitoEntity: HabitoEntity){
        CoroutineScope(Dispatchers.IO).launch {
            HabitosApplication.database.habitoDao().insertHabito(habitoEntity)
        }
    }

    suspend fun agregarRegistrosDBDataHabitos(nombreHabito: String) {
        val listaFechas = generarFechasFormatoYYYYMMDD()

        val listaRegistros = listaFechas.map { fecha ->
            DataHabitoEntity(
                nombre = nombreHabito,
                fecha = fecha,
                valorCampo = "0",
                notas = null
            )
        }

        HabitosApplication.database.dataHabitoDao().insertarListaDataHabito(listaRegistros)
    }
}