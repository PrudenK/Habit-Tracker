package com.pruden.habits.modules.miniHabitos.metodos.dialogos

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pruden.habits.R
import com.pruden.habits.common.clases.entities.MiniHabitoEntity
import com.pruden.habits.common.metodos.Dialogos.makeToast
import com.pruden.habits.modules.mainModule.metodos.ajustarDialogo
import com.pruden.habits.modules.miniHabitos.viewModel.MiniHabitosViewModel

fun dialogoAgregarMiniHabito(
    context: Context,
    recyclerMiniHabitos: RecyclerView,
    categoria: String,
    resources: Resources,
    miniHabitosViewModel: MiniHabitosViewModel,
    miniHabitos: MutableList<MiniHabitoEntity>
) {
    val dialogoView = LayoutInflater.from(context).inflate(R.layout.dialog_agregar_etiqueta, null)
    val dialogo = AlertDialog.Builder(context).setView(dialogoView).create()

    val btnCancelar = dialogoView.findViewById<Button>(R.id.button_cancelar_agregar_etiqueta)
    val btnGuardar = dialogoView.findViewById<Button>(R.id.button_guardar_agreagr_etiqueta)

    val nombreCategoria = dialogoView.findViewById<TextInputLayout>(R.id.til_nombre_etiqueta)
    nombreCategoria.hint = context.getString(R.string.nombre_del_mini_habito)
    
    val contenedorPosicion = dialogoView.findViewById<ConstraintLayout>(R.id.contenedor_posicion_mod_etiqueta)
    contenedorPosicion.visibility = View.GONE
    val imgColorPicker = dialogoView.findViewById<ImageView>(R.id.img_color_etiqueta_num)
    val tituloColor = dialogoView.findViewById<TextView>(R.id.titulo_color_etiqueta)
    imgColorPicker.visibility = View.GONE
    tituloColor.visibility = View.GONE

    dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val editeTextNombre = dialogoView.findViewById<TextInputEditText>(R.id.input_agregar_etiqueta)

    btnCancelar.setOnClickListener {
        dialogo.dismiss()
    }

    btnGuardar.setOnClickListener {
        val nombreEdit = editeTextNombre.text.toString()

        val miniHabitoRepetido = miniHabitosViewModel.miniHabitos.value!!
            .find { it.categoria == categoria && it.nombre == nombreEdit}

        if(miniHabitoRepetido == null){
            if(nombreEdit.isNotBlank()){
                val miniHabito = MiniHabitoEntity(
                    categoria = categoria,
                    nombre = nombreEdit,
                    cumplido = false,
                    posicion = miniHabitosViewModel.miniHabitos.value!!.filter { it.categoria == categoria }.size
                )

                miniHabitosViewModel.insertarMiniHabito(miniHabito)

                miniHabitos.clear()
                miniHabitos.addAll(miniHabitosViewModel.miniHabitos.value!!)
                recyclerMiniHabitos.adapter?.notifyDataSetChanged()

                makeToast(context.getString(R.string.mini_habito_creado_con_exito, editeTextNombre.text), context)

                dialogo.dismiss()
            }else {
                makeToast(context.getString(R.string.error_nombre_vacio, editeTextNombre.text), context)
            }
        }else{
            makeToast(context.getString(R.string.mini_habito_nombre_existe, editeTextNombre.text), context)
        }
    }


    dialogo.show()

    ajustarDialogo(resources, dialogo, 0.85f)
}