package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
class RegisteredControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void clean() {
        usuarioRepository.deleteAll();
    }

    @Test
    void getRegistradosShowsTableOfUsers() throws Exception {
        // GIVEN: dos usuarios
        UsuarioData u1 = new UsuarioData();
        u1.setEmail("x@x.com");
        u1.setPassword("p");
        UsuarioData saved1 = usuarioService.registrar(u1);

        UsuarioData u2 = new UsuarioData();
        u2.setEmail("y@y.com");
        u2.setPassword("p");
        UsuarioData saved2 = usuarioService.registrar(u2);

        // WHEN / THEN
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(view().name("registrados"))
                // Contiene cabecera de tabla
                .andExpect(content().string(containsString("<th>ID</th>")))
                .andExpect(content().string(containsString("<th>Correo electrónico</th>")))
                // Contiene filas con ID y email
                .andExpect(content().string(containsString(
                        "<td>" + saved1.getId() + "</td>")))
                .andExpect(content().string(containsString(
                        "<td>" + saved1.getEmail() + "</td>")))
                .andExpect(content().string(containsString(
                        "<td>" + saved2.getId() + "</td>")))
                .andExpect(content().string(containsString(
                        "<td>" + saved2.getEmail() + "</td>")));
    }

    @Test
    void navbarAppearsOnRegistrados() throws Exception {
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("navbar navbar-expand-lg")));
    }
}
