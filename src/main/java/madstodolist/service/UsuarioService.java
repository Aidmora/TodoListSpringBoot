package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public enum LoginStatus {
        LOGIN_OK,
        USER_NOT_FOUND,
        ERROR_PASSWORD,
        BLOQUEADO
    }

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Comprueba si existe algún usuario administrador en la base de datos.
    @Transactional(readOnly = true)
    public boolean adminExists() {
        return usuarioRepository.countByAdministradorTrue() > 0;
    }

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else if (usuario.get().isBloqueado()) {
            return LoginStatus.BLOQUEADO;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    // Se añade un usuario en la aplicación.
    // El email y password del usuario deben ser distinto de null
    // El email no debe estar registrado en la base de datos
    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");
        else if (usuario.getEmail() == null)
            throw new UsuarioServiceException("El usuario no tiene email");
        else if (usuario.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene password");
        else if (usuario.isAdministrador() && adminExists())
            throw new UsuarioServiceException("Ya existe un administrador");
        else {
            Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
            usuarioNuevo.setAdministrador(usuario.isAdministrador());
            usuarioNuevo = usuarioRepository.save(usuarioNuevo);
            return modelMapper.map(usuarioNuevo, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null)
            return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null)
            return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> listAllUsuarios() {
        logger.debug("Listando todos los usuarios");
        return ((List<Usuario>) usuarioRepository.findAll()).stream()
                .map(u -> modelMapper.map(u, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void setBloqueoUsuario(Long id, boolean bloqueado) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));
        u.setBloqueado(bloqueado);
        usuarioRepository.save(u);
    }

    @Transactional(readOnly = true)
    public boolean isUsuarioBloqueado(Long id) {
        return usuarioRepository.findById(id)
                .map(Usuario::isBloqueado)
                .orElse(false);
    }

}
