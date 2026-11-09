"""Sistema de gestión de citas médicas construido con Arquitectura Hexagonal.

Capas (de adentro hacia afuera):
    dominio         -> reglas del negocio puras, sin dependencias externas.
    aplicacion      -> casos de uso y puertos (interfaces) que el núcleo necesita.
    infraestructura -> adaptadores: web (Flask), persistencia (SQLAlchemy / memoria),
                       notificaciones, reloj y el ensamblaje de dependencias.
"""
