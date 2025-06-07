package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NavbarWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void cleanDatabase() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /about sin sesión muestra sólo Login y Registro")
    void navbarWithoutLoginShowsOnlyLoginAndRegister() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                // ToDoList → /about
                .andExpect(content().string(containsString("href=\"/about\"")))
                // Solo Login y Registro
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("href=\"/registro\"")))
                // No Tareas
                .andExpect(content().string(not(containsString("href=\"/usuarios/"))))
                // No dropdown de usuario
                .andExpect(content().string(not(containsString("dropdown-toggle"))));
    }

    @Test
    @DisplayName("GET /about con sesión muestra Tareas, Cuenta y Cerrar sesión<nombre>")
    void navbarWithLoginShowsTasksAndUserDropdown() throws Exception {
        // Crear y registrar un usuario
        UsuarioData u = new UsuarioData();
        u.setEmail("test@domain.com");
        u.setPassword("pwd");
        u.setNombre("TestUser");
        UsuarioData saved = usuarioService.registrar(u);

        mockMvc.perform(get("/about")
                .sessionAttr("idUsuarioLogeado", saved.getId()))
                .andExpect(status().isOk())
                // ToDoList → /about
                .andExpect(content().string(containsString("href=\"/about\"")))
                // Tareas → /usuarios/{id}/tareas
                .andExpect(content().string(containsString(
                        "href=\"/usuarios/" + saved.getId() + "/tareas\"")))
                // Enlace Cuenta
                .andExpect(content().string(containsString("href=\"/cuenta\"")))
                // Enlace Cerrar sesión...
                .andExpect(content().string(containsString("Cerrar sesión")))
                // ...y el nombre dentro del <span>
                .andExpect(content().string(containsString("<span>TestUser</span>")))
                // No Login / Registro
                .andExpect(content().string(not(containsString("href=\"/login\""))))
                .andExpect(content().string(not(containsString("href=\"/registro\""))))
                // Dropdown presente
                .andExpect(content().string(containsString("dropdown-toggle")));
    }

    @Test
    @DisplayName("GET /login y /registro no incluyen navbar")
    void loginAndRegistroPagesHaveNoNavbar() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("navbar navbar-expand-lg"))));

        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("navbar navbar-expand-lg"))));
    }

    @Test
    @DisplayName("GET /about invoca HomeController y renderiza about.html")
    void aboutControllerRendersAboutView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(view().name("about"))
                .andExpect(content().string(containsString("<h1>ToDoList</h1>")));
    }

    @Test
    @DisplayName("GET /cuenta invoca AccountController y renderiza account.html")
    void accountControllerRendersAccountView() throws Exception {
        mockMvc.perform(get("/cuenta")
                .sessionAttr("idUsuarioLogeado", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("account"))
                .andExpect(content().string(containsString("Página en construcción")));
    }
}
