# Sistema de Gestión de Recitales - TP Paradigmas de Programación

![Java](https://img.shields.io/badge/Java-21-orange) ![Maven](https://img.shields.io/badge/Maven-3.8-blue) ![Prolog](https://img.shields.io/badge/Prolog-SWI-red) ![Status](https://img.shields.io/badge/Status-Terminado-brightgreen)

Este repositorio contiene la solución al Trabajo Práctico de la materia **Paradigmas de Programación (2025)**. El sistema modela la gestión de una banda temporal para una discográfica, resolviendo la asignación de artistas a canciones mediante Programación Orientada a Objetos, algoritmos de optimización y lógica (Prolog).

## 👥 Integrantes del Equipo
* **Ignacio Parra**
---

## 📋 Descripción del Proyecto

El sistema permite administrar la conformación de un recital, combinando **Artistas Base** (fijos y sin costo) con **Artistas Externos** (contratables). El objetivo es cubrir todos los roles necesarios para cada canción minimizando los costos totales y respetando restricciones como la cantidad máxima de canciones por artista.

### Funcionalidades Principales

1.  **Gestión de Canciones y Roles:** Cálculo de roles faltantes por canción y por recital.
2.  **Contratación Manual:** Asignación de artistas a una canción específica optimizando el costo local.
3.  **Contratación Global (Automática):** Algoritmo de optimización para cubrir todo el recital a la vez.
4.  **Entrenamiento:** Capacidad de enseñar nuevos roles a artistas externos (incrementando su costo).
5.  **Persistencia de Estado:** Guardado y carga de estados del recital en archivos JSON.
6.  **Integración con Prolog:** Cálculo de "entrenamientos mínimos" necesarios mediante un motor lógico.

### Funcionalidades Bonus Implementadas
* **Arrepentimiento:** Posibilidad de quitar a un artista ya contratado, liberando sus asignaciones.
* **Artista Estrella:** Aplicación automática de descuentos si el artista coincide con el género del recital.
* **Guardado de Estado:** Persistencia completa del estado del recital para retomarlo luego.

---

## 🏗️ Arquitectura y Diseño

El proyecto sigue una arquitectura en capas con una fuerte separación de responsabilidades, aplicando principios **SOLID**.

### Patrones de Diseño Utilizados

* **Command Pattern:** Utilizado en el menú principal (`Menu`, `Command`, `AbstractCommand`). Permite encapsular cada acción del usuario como un objeto, facilitando la extensión de nuevas opciones sin modificar la clase invocadora.
* **Repository Pattern:** Abstrae la persistencia de datos (`JsonEstadoRecitalRepository`). Permite cambiar el mecanismo de almacenamiento (actualmente JSON con Jackson) sin afectar al dominio.
* **Facade (Fachada):** La clase `RecitalService` actúa como fachada, simplificando la interacción entre la capa de presentación (UI) y la lógica compleja de planificación.
* **DTO (Data Transfer Object):** Se utilizan DTOs (`RecitalEstadoDTO`, etc.) para desacoplar las entidades de dominio de la estructura de persistencia en JSON, evitando ciclos y manteniendo el encapsulamiento.

### Algoritmo de Optimización (Backtracking)

Para la funcionalidad de **"Contratar todo el recital"**, se implementó un algoritmo de **Backtracking** en la clase `PlanificacionService`. Este enfoque explora recursivamente el árbol de decisiones para asignar artistas a roles vacíos, permitiendo encontrar la combinación que minimiza el costo global respetando el límite de `maxCanciones` de cada artista.

### Diagrama de Clases (Mermaid)

```mermaid
classDiagram
    %% DOMINIO
    class Artista {
        <<Abstract>>
        -String nombre
        -Set~RolTipo~ rolesHistoricos
        -Set~String~ historialBandas
        +getCostoFinal(artistasBase) double*
        +puedeTocar(rol) boolean
    }
    class ArtistaBase {
        +getCostoFinal() double
    }
    class ArtistaExterno {
        -double costoBase
        -int maxCanciones
        -int cancionesAsignadas
        -TipoRecital tipoPreferido
        +entrenar(rol)
        +calcularCostosUnitarios()
    }
    
    Artista <|-- ArtistaBase
    Artista <|-- ArtistaExterno

    class Recital {
        -String nombre
        -TipoRecital tipo
        +calcularCostoDetallado()
        +getRolesFaltantesTotales()
    }
    
    class Cancion {
        -String titulo
        +estaCompleta() boolean
        +asignarArtista(rol, artista)
        +getCostoTotal() double
    }
    
    class RolRequerido {
        -RolTipo tipoRol
        -Artista artistaAsignado
        +asignar(artista)
        +estaCubierto() boolean
    }

    Recital "1" *-- "*" Cancion
    Recital o-- "*" ArtistaBase
    Recital o-- "*" ArtistaExterno
    Cancion "1" *-- "*" RolRequerido
    RolRequerido --> Artista : asignado

    %% SERVICIOS
    class RecitalService {
        +contratarParaCancion()
        +contratarParaTodoElRecital()
        +entrenarArtista()
    }
    
    class PlanificacionService {
        -Recital recital
        +contratarParaCancion()
        +contratarParaTodoElRecital() 
        -backtracking()
    }

    RecitalService --> PlanificacionService
    RecitalService --> Recital

    %% UI & COMMAND PATTERN
    class Menu {
        -Map~int, Command~ comandos
        +iniciar()
    }
    
    class Command {
        <<Interface>>
        +execute()
        +getDescription() String
    }
    
    class AbstractCommand {
        <<Abstract>>
        #RecitalService service
        #UiContext ui
    }
    
    class ContratarCancionCommand
    class ContratarTodoRecitalCommand
    class GuardarEstadoCommand
    
    Menu o-- Command
    Command <|.. AbstractCommand
    AbstractCommand <|-- ContratarCancionCommand
    AbstractCommand <|-- ContratarTodoRecitalCommand
    AbstractCommand <|-- GuardarEstadoCommand
    AbstractCommand <|-- CalcularEntrenamientosMinimosCommand

    %% PERSISTENCIA
    class EstadoRecitalRepository {
        <<Interface>>
        +guardarEstado()
        +cargarEstado()
    }
    class JsonEstadoRecitalRepository {
        -ObjectMapper mapper
    }
    
    EstadoRecitalRepository <|.. JsonEstadoRecitalRepository
    GuardarEstadoCommand --> EstadoRecitalRepository

    %% INTEGRACION
    class CalcularEntrenamientosMinimosCommand {
        -PrologEntrenamientoClient prologClient
        +execute()
    }

    class PrologEntrenamientoClient {
        +calcularEntrenamientosMinimos(faltantes) int
    }

    %% Relación corregida:
    CalcularEntrenamientosMinimosCommand --> PrologEntrenamientoClient : usa
```
