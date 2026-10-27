package model

enum class Categoria(val etiqueta: String) {
    INCENDIO("Incendio estructural"),
    ROBO("Robo en la vía pública"),
    SOSPECHOSO("Persona sospechosa"),
    ACCIDENTE("Accidente de tránsito"),
    VANDALISMO("Vandalismo"),
    OTRO("Otra situación");

    companion object {
        fun desde(nombre: String): Categoria =
            Categoria.entries.firstOrNull { it.name == nombre } ?: OTRO
    }
}