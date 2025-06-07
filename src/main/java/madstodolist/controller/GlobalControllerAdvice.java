// src/main/java/madstodolist/controller/GlobalControllerAdvice.java
package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute("usuarioLogeado")
    public UsuarioData usuarioLogeado() {
        Long id = managerUserSession.usuarioLogeado();
        if (id != null) {
            return usuarioService.findById(id);
        }
        return null;
    }
}
