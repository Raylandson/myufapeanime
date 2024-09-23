package br.edu.ufape.myufapeanime.myufapeanime.controllers;

import br.edu.ufape.myufapeanime.myufapeanime.dto.login.LoginDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.mappers.UsuarioMapper;
import br.edu.ufape.myufapeanime.myufapeanime.dto.usuario.UsuarioComSenhaDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.usuario.UsuarioDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.usuario.UsuarioResponse;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Adm;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Usuario;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroUsuarioExceptions.UsuarioDuplicadoException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroUsuarioExceptions.UsuarioInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroUsuarioExceptions.UsuarioSenhaInvalidaException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.fachada.GerenciadorAnimes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "API de autenticação e gerenciamento de usuários")
public class AuthController {
    @Autowired
    private GerenciadorAnimes gerenciador;

    @PostMapping("/login")
    @Operation(
            summary = "Login de usuário",
            description = "Autentica um usuário com base no email e senha fornecidos.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON com email e senha",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginDTO.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de Login",
                                    value = "{ \"email\": \"usuario@example.com\", \"senha\": \"123456\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Email não cadastrado"),
                    @ApiResponse(responseCode = "403", description = "Senha inválida")
            }
    )
    public ResponseEntity<Object> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        try {
            Usuario usuario = gerenciador.login(loginDTO.getEmail(), loginDTO.getSenha());

            UsuarioDTO usuarioDTOParaExibir = UsuarioMapper.convertToUsuarioComAvaliacaoDTO(usuario);
            session.setAttribute("user", usuario);

            return ResponseEntity.ok(usuarioDTOParaExibir);
        } catch (UsuarioInexistenteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UsuarioSenhaInvalidaException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout de usuário",
            description = "Desconecta o usuário atual e encerra a sessão.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso")
            }
    )
    public ResponseEntity<Object> logout(HttpSession session) {
        //checar se tá logado
        session.removeAttribute("user");
        session.invalidate();

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PostMapping("/cadastrar")
    @Operation(
            summary = "Cadastrar novo usuário ou administrador",
            description = "Cadastra um novo usuário no sistema. Pode ser tanto um usuário comum quanto um administrador.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON com os dados do usuário a ser cadastrado",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDTO.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de Cadastro",
                                    value = "{ \"nome\": \"João Silva\", \"email\": \"joao@example.com\", \"senha\": \"123456\", \"isAdm\": false }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
                    @ApiResponse(responseCode = "409", description = "Usuário já existente"),
                    @ApiResponse(responseCode = "422", description = "Senha inválida")
            }
    )
    public ResponseEntity<Object> cadastrar(@RequestBody UsuarioComSenhaDTO usuarioComSenhaDTO, HttpSession session) {
        try {

            Usuario userCadastro = gerenciador.createUsuario(usuarioComSenhaDTO);
            UsuarioDTO novoUsuarioDTO = UsuarioMapper.convertToDTO(userCadastro);

            UsuarioResponse response = new UsuarioResponse("Usuário cadastrado com sucesso!", novoUsuarioDTO);
            session.setAttribute("user", userCadastro);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (UsuarioDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (UsuarioSenhaInvalidaException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        }
    }
}
