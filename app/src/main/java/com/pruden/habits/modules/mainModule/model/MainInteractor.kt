package com.pruden.habits.modules.mainModule.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.ListAdapter
import com.pruden.habits.HabitosApplication
import com.pruden.habits.HabitosApplication.Companion.listaHabitos
import com.pruden.habits.common.Constantes
import com.pruden.habits.common.clases.auxClass.HabitosEtiqueta
import com.pruden.habits.common.clases.data.Habito
import com.pruden.habits.common.clases.entities.DataHabitoEntity
import com.pruden.habits.common.clases.entities.EtiquetaEntity
import com.pruden.habits.common.clases.entities.HabitoEntity
import com.pruden.habits.common.metodos.fechas.obtenerFechaActual
import com.pruden.habits.common.metodos.fechas.obtenerFechasEntre
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainInteractor {

    suspend fun sincronizarRegistrosFaltantes() {
        val listaNombresHabitos = HabitosApplication.database.habitoDao().obtenerTdosLosNombres()
        if (listaNombresHabitos.isNotEmpty()) {
            val ultimaFechaDB = HabitosApplication.database.dataHabitoDao().selectMaxFecha()
            val listaFechas = obtenerFechasEntre(ultimaFechaDB, obtenerFechaActual())

            insertarRegistrosFaltantes(listaFechas, listaNombresHabitos)
        }
    }

    private suspend fun insertarRegistrosFaltantes(fechas: List<String>, nombresHabitos: List<String>) {
        fechas.forEach { fecha ->
            nombresHabitos.forEach { nombre ->
                HabitosApplication.database.dataHabitoDao().insertDataHabito(
                    DataHabitoEntity(nombre, fecha, "0", null)
                )
            }
        }
        if(fechas.isNotEmpty()){
            HabitosApplication.database.miniHabitoDao().reiniciarValores()
        }
    }

    fun borrarHabito(habitoEntity: HabitoEntity){
        CoroutineScope(Dispatchers.IO).launch {
            HabitosApplication.database.habitoDao().deleteHabito(habitoEntity)
        }
    }

    fun getAllHabitosConDatos(): LiveData<List<Habito>> {
        val result = MediatorLiveData<List<Habito>>()

        CoroutineScope(Dispatchers.IO).launch {
            sincronizarRegistrosFaltantes()

            withContext(Dispatchers.Main) {
                val liveDataFromRoom = HabitosApplication.database.habitoDao().obtenerHabitosConLiveData()
                result.addSource(liveDataFromRoom) { habitos ->
                    result.value = habitos
                }
            }
        }
        return result
    }

    fun archivarHabito(habitoEntity: HabitoEntity){
        CoroutineScope(Dispatchers.IO).launch {
            val posicion = habitoEntity.posicion
            HabitosApplication.database.habitoDao().alternarArchivado(true, habitoEntity.nombre, Constantes.CANTIDAD_DIFF_HABITO_ARCHIVADO + habitoEntity.posicion)

            for(habito in listaHabitos){
                if(habito.posicion > posicion && habito.posicion < Constantes.CANTIDAD_DIFF_HABITO_ARCHIVADO){
                    with(habito){
                        HabitosApplication.database.habitoDao().updateHabito(
                            HabitoEntity(nombre, objetivo, tipoNumerico, unidad, colorHabito, archivado,
                                this.posicion -1, objetivoSemanal, objetivoMensual, objetivoAnual)
                        )
                    }
                }
            }
        }
    }

    fun actualizarPosicionesHabitos(listaHabitos: MutableList<HabitoEntity>, callback: () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            listaHabitos.forEach { habito ->
                HabitosApplication.database.habitoDao().updateHabito(habito)
            }

            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    fun obtenerEtiquetaConHabitos(): LiveData<List<EtiquetaEntity>> {
        val result = MediatorLiveData<List<EtiquetaEntity>>()

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                val liveDataFromRoom = HabitosApplication.database.etiquetaDao().obtenerTodasLasEtiquetasConLiveData()
                result.addSource(liveDataFromRoom) { etiquetas ->
                    result.value = etiquetas
                }
            }
        }
        return result
    }


    fun insertarEtiqueta(etiquetaEntity: EtiquetaEntity){
        CoroutineScope(Dispatchers.IO).launch {
            HabitosApplication.database.etiquetaDao().insertarEtiqueta(etiquetaEntity)
        }
    }
}