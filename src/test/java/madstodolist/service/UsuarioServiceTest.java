package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.SimpleDateFormat;
import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve el identificador del usuario de la BD
    Long addUsuarioBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123");
        UsuarioData nuevoUsuario = usuarioService.registrar(usuario);
        return nuevoUsuario.getId();
    }

    @Test
    public void servicioLoginUsuario() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // WHEN
        // intentamos logear un usuario y contraseña correctos
        UsuarioService.LoginStatus loginStatus1 = usuarioService.login("user@ua", "123");

        // intentamos logear un usuario correcto, con una contraseña incorrecta
        UsuarioService.LoginStatus loginStatus2 = usuarioService.login("user@ua", "000");

        // intentamos logear un usuario que no existe,
        UsuarioService.LoginStatus loginStatus3 = usuarioService.login("pepito.perez@gmail.com", "12345678");

        // THEN

        // el valor devuelto por el primer login es LOGIN_OK,
        assertThat(loginStatus1).isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);

        // el valor devuelto por el segundo login es ERROR_PASSWORD,
        assertThat(loginStatus2).isEqualTo(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // y el valor devuelto por el tercer login es USER_NOT_FOUND.
        assertThat(loginStatus3).isEqualTo(UsuarioService.LoginStatus.USER_NOT_FOUND);
    }

    @Test
    public void servicioRegistroUsuario() {
        // WHEN
        // Registramos un usuario con un e-mail no existente en la base de datos,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba2@gmail.com");
        usuario.setPassword("12345678");

        usuarioService.registrar(usuario);

        // THEN
        // el usuario se añade correctamente al sistema.

        UsuarioData usuarioBaseDatos = usuarioService.findByEmail("usuario.prueba2@gmail.com");
        assertThat(usuarioBaseDatos).isNotNull();
        assertThat(usuarioBaseDatos.getEmail()).isEqualTo("usuario.prueba2@gmail.com");
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConNullPassword() {
        // WHEN, THEN
        // Si intentamos registrar un usuario con un password null,
        // se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConEmailRepetido() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // THEN
        // Si registramos un usuario con un e-mail ya existente en la base de datos,
        // , se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("12345678");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioDevuelveUsuarioConId() {

        // WHEN
        // Si registramos en el sistema un usuario con un e-mail no existente en la base
        // de datos,
        // y un password no nulo,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");
        usuario.setPassword("12345678");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // THEN
        // se actualiza el identificador del usuario

        assertThat(usuarioNuevo.getId()).isNotNull();

        // con el identificador que se ha guardado en la BD.

        UsuarioData usuarioBD = usuarioService.findById(usuarioNuevo.getId());
        assertThat(usuarioBD).isEqualTo(usuarioNuevo);
    }

    @Test
    public void servicioConsultaUsuarioDevuelveUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioBD();

        // WHEN
        // recuperamos un usuario usando su e-mail,

        UsuarioData usuario = usuarioService.findByEmail("user@ua");

        // THEN
        // el usuario obtenido es el correcto.

        assertThat(usuario.getId()).isEqualTo(usuarioId);
        assertThat(usuario.getEmail()).isEqualTo("user@ua");
        assertThat(usuario.getNombre()).isEqualTo("Usuario Ejemplo");
    }

    @Test
    void testListAllUsuarios() {
        // GIVEN: dos usuarios en la BD
        UsuarioData u1 = new UsuarioData();
        u1.setEmail("a@a.com");
        u1.setPassword("p");
        UsuarioData saved1 = usuarioService.registrar(u1);

        UsuarioData u2 = new UsuarioData();
        u2.setEmail("b@b.com");
        u2.setPassword("p");
        UsuarioData saved2 = usuarioService.registrar(u2);

        // WHEN
        List<UsuarioData> lista = usuarioService.listAllUsuarios();

        // THEN
        assertThat(lista).hasSize(2)
                .extracting(UsuarioData::getEmail)
                .containsExactlyInAnyOrder("a@a.com", "b@b.com");
    }

    @Test
    void testFindByIdReturnsUsuario() throws Exception {
        // GIVEN
        UsuarioData u = new UsuarioData();
        u.setEmail("x@x.com");
        u.setPassword("pwd");
        u.setNombre("Xavier");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        u.setFechaNacimiento(sdf.parse("1990-05-10"));
        UsuarioData saved = usuarioService.registrar(u);

        // WHEN
        UsuarioData found = usuarioService.findById(saved.getId());

        // THEN
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getEmail()).isEqualTo("x@x.com");
        assertThat(found.getNombre()).isEqualTo("Xavier");
        assertThat(found.getFechaNacimiento()).isEqualTo(sdf.parse("1990-05-10"));
    }

    @Test
    void testFindByIdNotFound() {
        assertThat(usuarioService.findById(999L)).isNull();
    }

    @Test
    void adminExistsBehaviour() {
        // al principio no hay admin
        assertThat(usuarioService.adminExists()).isFalse();

        // registramos uno
        UsuarioData admin = new UsuarioData();
        admin.setEmail("a@a.com");
        admin.setPassword("p");
        admin.setAdministrador(true);
        usuarioService.registrar(admin);

        assertThat(usuarioService.adminExists()).isTrue();

        // intentar registrar otro admin falla
        UsuarioData otro = new UsuarioData();
        otro.setEmail("b@b.com");
        otro.setPassword("p");
        otro.setAdministrador(true);
        assertThrows(UsuarioServiceException.class,
                () -> usuarioService.registrar(otro));
    }

    @Test
    void testBloquearYHabilitarUsuario() {
        // registramos un usuario
        UsuarioData u = new UsuarioData();
        u.setEmail("x@x.com");
        u.setPassword("p");
        UsuarioData saved = usuarioService.registrar(u);

        // bloqueamos
        usuarioService.setBloqueoUsuario(saved.getId(), true);
        assertThat(usuarioService.isUsuarioBloqueado(saved.getId())).isTrue();

        // habilitamos
        usuarioService.setBloqueoUsuario(saved.getId(), false);
        assertThat(usuarioService.isUsuarioBloqueado(saved.getId())).isFalse();
    }
}