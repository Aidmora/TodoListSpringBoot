package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @BeforeEach
    void cleanDb() {
        // Si lo necesitas, limpia la tabla de usuarios aquí
    }

    @Test
    void registroFormShowsCheckboxOnlyOnce() throws Exception {
        // 1) sin admin: checkbox visible
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrarme como administrador")));

        // 2) creamos un admin
        UsuarioData admin = new UsuarioData();
        admin.setEmail("adm@e.com");
        admin.setPassword("p");
        admin.setAdministrador(true);
        usuarioService.registrar(admin);

        // 3) volvemos al formulario: checkbox no aparece
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Registrarme como administrador"))));
    }

    @Test
    void loginAdminRedirectsToRegistrados() throws Exception {
        // registramos admin
        UsuarioData admin = new UsuarioData();
        admin.setEmail("adm@e.com");
        admin.setPassword("p");
        admin.setAdministrador(true);
        usuarioService.registrar(admin);

        mockMvc.perform(post("/login")
                .param("eMail", "adm@e.com")
                .param("password", "p"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }
}
