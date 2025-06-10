package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.repository.UsuarioRepository;
import madstodolist.service.UsuarioService;
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
public class RegisteredControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UsuarioService usuarioService;

        @Autowired
        private UsuarioRepository usuarioRepository;

        private Long adminId;

        @BeforeEach
        void setUp() {
                // Limpiamos la BD y creamos un administrador de prueba
                usuarioRepository.deleteAll();

                UsuarioData admin = new UsuarioData();
                admin.setEmail("admin@ua");
                admin.setPassword("admin");
                admin.setNombre("Admin User");
                admin.setAdministrador(true);
                UsuarioData savedAdmin = usuarioService.registrar(admin);
                this.adminId = savedAdmin.getId();
        }

        @Test
        void getRegistradosShowsTableOfUsers() throws Exception {
                // Añadimos dos usuarios normales
                UsuarioData u1 = new UsuarioData();
                u1.setEmail("a@a.com");
                u1.setPassword("p1");
                UsuarioData saved1 = usuarioService.registrar(u1);

                UsuarioData u2 = new UsuarioData();
                u2.setEmail("b@b.com");
                u2.setPassword("p2");
                UsuarioData saved2 = usuarioService.registrar(u2);

                // Hacemos la petición como admin
                mockMvc.perform(get("/registrados")
                                .sessionAttr("idUsuarioLogeado", adminId))
                                .andExpect(status().isOk())
                                .andExpect(view().name("registrados"))
                                // Verificamos que aparezcan las filas de ambos usuarios
                                .andExpect(content().string(containsString("<td>" + saved1.getId() + "</td>")))
                                .andExpect(content().string(containsString("<td>" + saved1.getEmail() + "</td>")))
                                .andExpect(content().string(containsString("<td>" + saved2.getId() + "</td>")))
                                .andExpect(content().string(containsString("<td>" + saved2.getEmail() + "</td>")));
        }

        @Test
        void getUsuarioDetailShowsUsuario() throws Exception {
                // Creamos un usuario normal
                UsuarioData u = new UsuarioData();
                u.setEmail("jane@doe.com");
                u.setPassword("pwd");
                u.setNombre("Jane Doe");
                UsuarioData saved = usuarioService.registrar(u);

                // Petición como admin para ver su detalle
                mockMvc.perform(get("/registrados/" + saved.getId())
                                .sessionAttr("idUsuarioLogeado", adminId))
                                .andExpect(status().isOk())
                                .andExpect(view().name("usuario"))
                                .andExpect(content().string(containsString("Usuario " + saved.getId())))
                                .andExpect(content().string(containsString("jane@doe.com")))
                                .andExpect(content().string(containsString("Jane Doe")))
                                .andExpect(content().string(containsString("Fecha de nacimiento")));
        }

        @Test
        void getUsuarioDetailNotFoundReturns404() throws Exception {
                mockMvc.perform(get("/registrados/9999")
                                .sessionAttr("idUsuarioLogeado", adminId))
                                .andExpect(status().isNotFound());
        }

        @Test
        void navbarAppearsOnRegistrados() throws Exception {
                mockMvc.perform(get("/registrados")
                                .sessionAttr("idUsuarioLogeado", adminId))
                                .andExpect(status().isOk())
                                // Verificamos que el navbar esté presente
                                .andExpect(content().string(containsString("navbar navbar-expand-lg")));
        }
}
