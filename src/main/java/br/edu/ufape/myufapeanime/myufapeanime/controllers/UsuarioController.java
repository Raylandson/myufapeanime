package br.edu.ufape.myufapeanime.myufapeanime.controllers;

import br.edu.ufape.myufapeanime.myufapeanime.dto.anime.AnimeDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.avaliacao.AvaliacaoPeloIdDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.mappers.AvaliacaoMapper;
import br.edu.ufape.myufapeanime.myufapeanime.dto.mappers.AnimeMapper;
import br.edu.ufape.myufapeanime.myufapeanime.dto.mappers.UsuarioMapper;
import br.edu.ufape.myufapeanime.myufapeanime.dto.model.TipoLista;
import br.edu.ufape.myufapeanime.myufapeanime.dto.usuario.UsuarioComSenhaDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.usuario.UsuarioDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.usuario.UsuarioResponse;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Adm;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Anime;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Avaliacao;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Usuario;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAnimeExceptions.AnimeInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAutenticacaoExceptions.AutorizacaoNegadaException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAvaliacaoExceptions.AvaliacaoInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroUsuarioExceptions.UsuarioDuplicadoException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroUsuarioExceptions.UsuarioInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.fachada.GerenciadorAnimes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuario")
@Tag(name = "Usuários", description = "API de gerenciamento de usuarios")
public class UsuarioController {

    @Autowired
    private GerenciadorAnimes gerenciador;

    /*****  METODOS GET *****/

    //listar todos os usuarios
    @GetMapping("/list")
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista com todos os usuários cadastrados no sistema.",
            tags = {"Usuários"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado")
            })
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        List<Usuario> usuarios = gerenciador.findAllUsuarios();
        List<UsuarioDTO> result = usuarios.stream()
                .map(UsuarioMapper::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    //procurar por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os detalhes de um usuário específico com base no ID fornecido.",
            tags = {"Usuários"},
            parameters = {
                    @Parameter(name = "id", description = "ID do usuário a ser buscado", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            })
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        try {
            Usuario usuario = gerenciador.findUsuarioById(id);
            UsuarioDTO usuarioDTO = UsuarioMapper.convertToDTO(usuario);
            return ResponseEntity.ok(usuarioDTO);

        } catch (UsuarioInexistenteException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //procurar por nome
    @GetMapping("/nome/{nome}")
    @Operation(
            summary = "Buscar usuários por nome",
            description = "Retorna uma lista de usuários cujo nome corresponde ao fornecido.",
            tags = {"Usuários"},
            parameters = {
                    @Parameter(name = "nome", description = "Nome do usuário a ser buscado", required = true, example = "João")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado com o nome fornecido")
            })
    public ResponseEntity<List<UsuarioDTO>> findByNome(@PathVariable String nome) {
        List<Usuario> usuarios = gerenciador.findByNomeUsuario(nome);

        if (usuarios.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UsuarioDTO> dtos = usuarios.stream()
                .map(UsuarioMapper::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    //exibir lista_assistindo
    @GetMapping("/assistindo")
    @Operation(
            summary = "Listar animes na lista 'assistindo' do usuário",
            description = "Retorna a lista de animes que o usuário está atualmente assistindo.",
            tags = {"Usuários", "Animes"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de animes retornada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário ou lista de animes não encontrada"),
                    @ApiResponse(responseCode = "403", description = "Faça login para executar essa ação")
            })
    public ResponseEntity<List<AnimeDTO>> getAnimesAssistidosUsuario(HttpSession session) {
        try {
            //busca a lista de animes assistidos
            Usuario usuario = (Usuario) session.getAttribute("user");

            List<Anime> animesAssistidos = gerenciador.getAssistindoUsuario(usuario);

            //converte os Animes para AnimeDTOs
            List<AnimeDTO> dtos = animesAssistidos.stream()
                    .map(AnimeMapper::convertToAnimeDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (UsuarioInexistenteException | AutorizacaoNegadaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //exibir lista_completo
    @GetMapping("/completos")
    @Operation(
            summary = "Listar animes na lista 'completos' do usuário",
            description = "Retorna a lista de animes que o usuário completou.",
            tags = {"Usuários", "Animes"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de animes retornada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário ou lista de animes não encontrada"),
                    @ApiResponse(responseCode = "403", description = "Faça login para executar essa ação")
            })
    public ResponseEntity<List<AnimeDTO>> getAnimesCompletos(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("user");
            List<Anime> animesAssistidos = gerenciador.getCompletosUsuario(usuario);

            List<AnimeDTO> dtos = animesAssistidos.stream()
                    .map(AnimeMapper::convertToAnimeDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (UsuarioInexistenteException | AutorizacaoNegadaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //exibir lista_quero_assistir
    @GetMapping("/quero-assistir")
    @Operation(
            summary = "Listar animes na lista 'quero assistir' do usuário",
            description = "Retorna a lista de animes que o usuário marcou como 'quero assistir'.",
            tags = {"Usuários", "Animes"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de animes retornada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário ou lista de animes não encontrada"),
                    @ApiResponse(responseCode = "403", description = "Faça login para executar essa ação")
            })
    public ResponseEntity<List<AnimeDTO>> getAnimesQueroAssistir(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("user");

            List<Anime> animesAssistidos = gerenciador.getQueroAssistirUsuario(usuario);

            List<AnimeDTO> dtos = animesAssistidos.stream()
                    .map(AnimeMapper::convertToAnimeDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (UsuarioInexistenteException | AutorizacaoNegadaException e) {
            return ResponseEntity.notFound().build();
        }
    }


    /*****  METODOS POST *****/

    //add anime na lista do usuario
    @PostMapping("/listas/{tipoLista}/{animeId}")
    @Operation(
            summary = "Adicionar anime à lista de animes",
            description = "Adiciona um anime à lista especificada do usuário logado.",
            tags = {"Usuários", "Animes"},
            parameters = {
                    @Parameter(name = "animeId", description = "ID do anime a ser adicionado", required = true, example = "1"),
                    @Parameter(name = "tipoLista", description = "Tipo de lista (ASSISTINDO, QUERO_ASSISTIR, COMPLETO)", required = true, example = "ASSISTINDO")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Anime adicionado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário ou anime não encontrado"),
                    @ApiResponse(responseCode = "403", description = "Faça login para executar essa ação")
            }
    )
    public ResponseEntity<Object> adicionarAnimeLista(@PathVariable Long animeId, @PathVariable TipoLista tipoLista, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("user");

            gerenciador.adicionarAnimeLista(usuario, animeId, tipoLista);
            return ResponseEntity.ok("Anime adicionado à lista '" + tipoLista + "' com sucesso!");
        } catch (AutorizacaoNegadaException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AnimeInexistenteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /*****  METODOS PUT *****/

    //update usuário existente
    @PutMapping("/atualizar")
    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza as informações de um usuário existente com base no ID fornecido.",
            tags = {"Usuários"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON contendo os novos dados do usuário",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioComSenhaDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "409", description = "Conflito de dados, e-mail já cadastrado"),
                    @ApiResponse(responseCode = "403", description = "Faça login para executar essa ação")
            }
    )
    public ResponseEntity<Object> updateUsuario(@RequestBody UsuarioComSenhaDTO usuarioComSenhaDTO, HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("user");

            Usuario usuarioAtualizado = gerenciador.updateUsuarioLogado(usuarioComSenhaDTO, usuarioLogado);

            UsuarioDTO usuarioAtualizadoDTO = UsuarioMapper.convertToDTO(usuarioAtualizado);

            UsuarioResponse response = new UsuarioResponse("Usuário atualizado com sucesso!", usuarioAtualizadoDTO);
            return ResponseEntity.ok(response);

        } catch (UsuarioInexistenteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UsuarioDuplicadoException e) {
            // Se o e-mail já estiver em uso, retorna 409 (Conflito)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (AutorizacaoNegadaException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /*****  METODOS DELETE *****/


    //apagar usuario logado
    @DeleteMapping("/deletar")
    @Operation(
            summary = "Deletar usuário",
            description = "Remove o usuário logado no sistema.",
            tags = {"Usuários"},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                    @ApiResponse(responseCode = "500", description = "Erro interno do servidor"),
                    @ApiResponse(responseCode = "403", description = "Faça login para executar essa ação")

            }
    )
    public ResponseEntity<Object> deleteUsuario(HttpSession session) {
        try {

            Usuario usuarioLogado = (Usuario) session.getAttribute("user");

            gerenciador.deletarUsuarioLogado(usuarioLogado);

            session.removeAttribute("user");
            session.invalidate();

            return ResponseEntity.ok("Usuário apagado com sucesso.");
        } catch (UsuarioInexistenteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AutorizacaoNegadaException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    //deletar anime da lista
    @DeleteMapping("/listas/{tipoLista}/{animeId}")
    @Operation(
            summary = "Remover anime da lista de animes",
            description = "Remove um anime da lista especificada do usuário logado.",
            tags = {"Usuários", "Animes"},
            parameters = {
                    @Parameter(name = "animeId", description = "ID do anime a ser removido", required = true, example = "1"),
                    @Parameter(name = "tipoLista", description = "Tipo de lista (ASSISTINDO, QUERO_ASSISTIR, COMPLETO)", required = true, example = "ASSISTINDO")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Anime removido com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário ou anime não encontrado"),
                    @ApiResponse(responseCode = "403", description = "Acesso negado")
            })
    public ResponseEntity<Object> removerAnimeLista(@PathVariable Long animeId, @PathVariable TipoLista tipoLista, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("user");

            gerenciador.removerAnimeLista(usuario, animeId, tipoLista);
            return ResponseEntity.ok("Anime removido da lista '" + tipoLista + "' com sucesso!");
        } catch (AnimeInexistenteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (AutorizacaoNegadaException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }


    /*****  METODOS PRIVADOS *****/

}
