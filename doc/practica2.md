# Documentación Técnica de la Evolución

Este documento proporciona una visión global de la evolución de la aplicación **ToDoList**, resaltando los principales cambios en seguridad, gestión de usuarios y arquitectura de tests.

---

## 1. Arquitectura por Capas

La aplicación sigue el patrón **MVC** y se estructura en las siguientes capas:

- **Modelo (`madstodolist.model`)**: Entidades JPA `Usuario` y `Tarea`.
- **DTO (`madstodolist.dto`)**: Clases de datos para el intercambio con las vistas.
- **Repositorio (`madstodolist.repository`)**: Interfaces `CrudRepository`.
- **Servicio (`madstodolist.service`)**: Lógica de negocio y transacciones.
- **Controlador (`madstodolist.controller`)**: Rutas HTTP y validación de sesión.
- **Vistas (`src/main/resources/templates`)**: Plantillas Thymeleaf.
- **Tests (`src/test/java`)**: Pruebas unitarias e integradas.

---

## 2. Gestión de Sesión y Autenticación

### 2.1 ManagerUserSession

Clase que encapsula `HttpSession` para manejar el ID del usuario logeado.

### 2.2 LoginController

Gestiona las rutas `/login` y `/registro`, validando credenciales, lógica de administrador y manejo de cuentas bloqueadas.

---

## 3. Funcionalidades Clave

### 3.1 Gestión de Tareas

- `TareaService`: Operaciones CRUD para tareas asociadas a usuarios.
- `TareaController`: Protege rutas validando login mediante `UsuarioNoLogeadoException`.

### 3.2 Listado y Detalle de Usuarios

- `RegisteredController`:
    - **GET `/registrados`**: Lista todos los usuarios (solo administradores).
    - **GET `/registrados/{id}`**: Detalle de usuario específico (solo administradores).
- Ambas rutas usan el método privado `comprobarAdmin()` para asegurar acceso exclusivo a administradores (error 401 si no se cumplen requisitos).

### 3.3 Bloqueo y Habilitación de Usuarios

- Se añade el campo booleano `bloqueado` a la entidad `Usuario`.
- `UsuarioService` incluye el método `@Transactional setBloqueoUsuario(Long id, boolean bloqueado)`.
- La vista `registrados.html` muestra el estado "Bloqueado" y botones dinámicos "Bloquear"/"Habilitar" que interactúan con nuevos endpoints POST.

### 3.4 Error 401 Personalizado

El `GlobalExceptionHandler` maneja excepciones de autorización:

```java
@ExceptionHandler(UsuarioNoLogeadoException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public String handleUnauthorized() { return "error/401"; }
```

Esto devuelve la plantilla `templates/error/401.html`, mostrando una alerta Bootstrap con el mensaje "No tienes permiso suficiente".

---

## 4. Vistas Thymeleaf Añadidas/Modificadas

- **`fragments.html`**: Incluye head, navbar común y scripts.
- **`error/401.html`**: Nueva vista para errores de autorización.
- **`registrados.html`**: Listado de usuarios con columnas "Admin", "Bloqueado" y acciones de gestión.
- **`usuario.html`**: Detalle de usuario (sin contraseña).
- **`formLogin.html` y `formRegistro.html`**: Actualizados para login, registro y gestión del checkbox de administrador.

---

## 5. Tests y Cobertura

Se han implementado y ampliado pruebas unitarias e integradas para robustecer los cambios.

### 5.1 Unidad de Servicio (`UsuarioServiceTest`)

- `testBloquearYHabilitarUsuario()`
- `testLoginBloqueado()`
- `testListAllUsuarios()`

### 5.2 Integración Controladores

- **`LoginControllerTest`**: Redirección para administradores y mensaje de error para logins bloqueados.
- **`RegistroControllerIntegrationTest`**: Registro de usuarios con rol de administrador.
- **`RegisteredControllerAuthTest`**: Verifica comportamiento 401 (no autorizado) y 200 (autorizado) para `/registrados`.
- **`RegisteredControllerTest`**: Funcionamiento del listado y detalle de usuarios para administradores.

---

## 6. Ejemplo de Código Relevante

### Verificación de Administrador

```java
private void comprobarAdmin() {
    Long id = managerUserSession.usuarioLogeado();
    if (id == null) throw new UsuarioNoLogeadoException();
    UsuarioData u = usuarioService.findById(id);
    if (!u.isAdministrador()) throw new UsuarioNoLogeadoException();
}
```

Este método centraliza la lógica de autorización, asegurando que solo los administradores accedan a rutas protegidas y evitando duplicación de código.

---
