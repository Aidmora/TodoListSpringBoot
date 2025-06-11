package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.UsuarioNotFoundException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class RegisteredController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ManagerUserSession managerUserSession;

    /** Lanza 401 si no hay sesión o no es administrador */
    private void comprobarAdmin() {
        Long id = managerUserSession.usuarioLogeado();
        if (id == null) {
            throw new UsuarioNoLogeadoException();
        }
        UsuarioData u = usuarioService.findById(id);
        if (u == null || !u.isAdministrador()) {
            throw new UsuarioNoLogeadoException();
        }
    }

    @GetMapping("/registrados")
    public String registrados(Model model) {
        comprobarAdmin();
        List<UsuarioData> usuarios = usuarioService.listAllUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "registrados";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable Long id, Model model) {
        comprobarAdmin();
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new UsuarioNotFoundException();
        }
        model.addAttribute("usuario", usuario);
        return "usuario";
    }

    @PostMapping("/registrados/{id}/bloquear")
    public String bloquearUsuario(@PathVariable Long id) {
        comprobarAdmin();
        usuarioService.setBloqueoUsuario(id, true);
        return "redirect:/registrados";
    }

    @PostMapping("/registrados/{id}/habilitar")
    public String habilitarUsuario(@PathVariable Long id) {
        comprobarAdmin();
        usuarioService.setBloqueoUsuario(id, false);
        return "redirect:/registrados";
    }
}
