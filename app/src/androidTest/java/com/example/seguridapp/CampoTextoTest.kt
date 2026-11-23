package com.example.seguridapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.seguridapp.ui.components.CampoTexto
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CampoTextoTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraErrorDeValidacion() {
        composeRule.setContent {
            MaterialTheme {
                CampoTexto(
                    valor = "",
                    onCambio = {},
                    etiqueta = "Ubicación",
                    icono = Icons.Filled.Place,
                    error = "La ubicación referencial es obligatoria"
                )
            }
        }

        composeRule.onNodeWithText(
            "La ubicación referencial es obligatoria"
        ).assertIsDisplayed()
    }

    @Test
    fun permiteIngresarTexto() {
        composeRule.setContent {
            MaterialTheme {
                var texto by remember {
                    mutableStateOf("")
                }

                CampoTexto(
                    valor = texto,
                    onCambio = { texto = it },
                    etiqueta = "Ubicación",
                    icono = Icons.Filled.Place,
                    error = null
                )
            }
        }

        composeRule.onNode(
            hasSetTextAction()
        ).performTextInput("Sector Centro")

        composeRule.onNode(
            hasSetTextAction()
        ).assertTextContains("Sector Centro")
    }
}