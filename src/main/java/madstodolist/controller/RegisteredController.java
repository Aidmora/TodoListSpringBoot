package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
