package madstodolist.controller;

import madstodolist.controller.exception.UsuarioNoLogeadoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNoLogeadoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorized() {
        // Renderiza templates/error/401.html
        return "error/401";
    }
}
