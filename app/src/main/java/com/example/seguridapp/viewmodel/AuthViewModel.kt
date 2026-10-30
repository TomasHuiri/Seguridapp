package com.example.seguridapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridapp.data.local.UsuarioEntity
import com.example.seguridapp.data.repository.UsuarioRepository
import com.example.seguridapp.data.session.SessionStore
import com.example.seguridapp.util.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SesionUi(val cargando: Boolean = true, val usuario: UsuarioEntity? = null)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val verPassword: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null
)

data class RegistroUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val terminos: Boolean = false,
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmacionError: String? = null,
    val terminosError: String? = null,
    val verPassword: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModel(
    private val repo: UsuarioRepository,
    private val session: SessionStore
) : ViewModel() {

    val sesion: StateFlow<SesionUi> = session.usuarioId
        .flatMapLatest { id ->
            if (id == null) flowOf(SesionUi(cargando = false, usuario = null))
            else repo.observar(id).map { SesionUi(cargando = false, usuario = it) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SesionUi())

    private val _login = MutableStateFlow(LoginUiState())
    val login = _login.asStateFlow()

    private val _registro = MutableStateFlow(RegistroUiState())
    val registro = _registro.asStateFlow()

    fun onLoginEmail(v: String) = _login.update {
        it.copy(email = v, emailError = if (v.isEmpty()) null else Validators.email(v), errorGeneral = null)
    }

    fun onLoginPassword(v: String) = _login.update {
        it.copy(password = v, passwordError = null, errorGeneral = null)
    }

    fun toggleLoginVerPassword() = _login.update { it.copy(verPassword = !it.verPassword) }

    fun limpiarLogin() { _login.value = LoginUiState() }

    fun iniciarSesion(onExito: () -> Unit) {
        val s = _login.value
        val emailError = Validators.email(s.email)
        val passwordError = if (s.password.isBlank()) "La contraseña es obligatoria" else null
        if (emailError != null || passwordError != null) {
            _login.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _login.update { it.copy(cargando = true, errorGeneral = null) }
            delay(600)
            val usuario = repo.autenticar(s.email, s.password)
            if (usuario == null) {
                _login.update { it.copy(cargando = false, errorGeneral = "Correo o contraseña incorrectos") }
            } else {
                session.guardar(usuario.id)
                _login.value = LoginUiState()
                onExito()
            }
        }
    }

    fun onRegNombre(v: String) = _registro.update {
        it.copy(nombre = v, nombreError = if (v.isEmpty()) null else Validators.nombre(v), errorGeneral = null)
    }

    fun onRegEmail(v: String) = _registro.update {
        it.copy(email = v, emailError = if (v.isEmpty()) null else Validators.email(v), errorGeneral = null)
    }

    fun onRegPassword(v: String) = _registro.update {
        it.copy(
            password = v,
            passwordError = if (v.isEmpty()) null else Validators.password(v),
            confirmacionError = if (it.confirmacion.isEmpty()) null else Validators.confirmacion(v, it.confirmacion)
        )
    }

    fun onRegConfirmacion(v: String) = _registro.update {
        it.copy(confirmacion = v, confirmacionError = if (v.isEmpty()) null else Validators.confirmacion(it.password, v))
    }

    fun onRegTerminos(v: Boolean) = _registro.update {
        it.copy(terminos = v, terminosError = if (v) null else it.terminosError)
    }

    fun toggleRegVerPassword() = _registro.update { it.copy(verPassword = !it.verPassword) }

    fun limpiarRegistro() { _registro.value = RegistroUiState() }

    fun registrarse(onExito: () -> Unit) {
        val s = _registro.value
        val errores = RegistroUiState(
            nombreError = Validators.nombre(s.nombre),
            emailError = Validators.email(s.email),
            passwordError = Validators.password(s.password),
            confirmacionError = Validators.confirmacion(s.password, s.confirmacion),
            terminosError = Validators.terminos(s.terminos)
        )
        val hayErrores = listOf(
            errores.nombreError, errores.emailError, errores.passwordError,
            errores.confirmacionError, errores.terminosError
        ).any { it != null }
        if (hayErrores) {
            _registro.update {
                it.copy(
                    nombreError = errores.nombreError,
                    emailError = errores.emailError,
                    passwordError = errores.passwordError,
                    confirmacionError = errores.confirmacionError,
                    terminosError = errores.terminosError
                )
            }
            return
        }
        viewModelScope.launch {
            _registro.update { it.copy(cargando = true, errorGeneral = null) }
            delay(600)
            val id = repo.registrar(s.nombre, s.email, s.password)
            if (id == null) {
                _registro.update {
                    it.copy(cargando = false, emailError = "Este correo ya está registrado", errorGeneral = null)
                }
            } else {
                session.guardar(id)
                _registro.value = RegistroUiState()
                onExito()
            }
        }
    }

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            session.cerrar()
            onListo()
        }
    }
}
