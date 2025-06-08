package madstodolist.controller;

import madstodolist.controller.exception.UsuarioNotFoundException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class RegisteredController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registrados")
    public String registrados(Model model) {
        List<UsuarioData> usuarios = usuarioService.listAllUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "registrados";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable Long id, Model model) {
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new UsuarioNotFoundException();
        }
        model.addAttribute("usuario", usuario);
        return "usuario";
    }
}
