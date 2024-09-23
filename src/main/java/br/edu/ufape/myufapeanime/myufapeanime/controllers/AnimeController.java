package br.edu.ufape.myufapeanime.myufapeanime.controllers;

import br.edu.ufape.myufapeanime.myufapeanime.dto.anime.AnimeComAvaliacaoDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.anime.AnimeDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.avaliacao.AvaliacaoPeloIdDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.mappers.AnimeMapper;
import br.edu.ufape.myufapeanime.myufapeanime.dto.mappers.AvaliacaoMapper;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Anime;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Avaliacao;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Usuario;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAnimeExceptions.AnimeDuplicadoException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAnimeExceptions.AnimeInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAnimeExceptions.NumeroDeEpisodiosInvalidoException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAutenticacaoExceptions.AutorizacaoNegadaException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.fachada.GerenciadorAnimes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static br.edu.ufape.myufapeanime.myufapeanime.dto.mappers.AnimeMapper.*;


@RestController
@RequestMapping("/anime")
@Tag(name = "Animes", description = "API de gerenciamento de animes")
public class AnimeController {
    @Autowired
    private GerenciadorAnimes gerenciadorAnimes;

    // Cadastrar um novo anime
    @PostMapping("/cadastrar")
    @Operation(
            summary = "Cadastrar um novo anime",
            description = "Cadastra um novo anime no sistema",
            tags = {"Animes"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON do anime a ser cadastrado",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimeDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Exemplo de Anime",
                                            description = "Exemplo de um objeto de anime para cadastro",
                                            value = "{\n" +
                                                    "  \"nome\": \"Naruto\",\n" +
                                                    "  \"genero\": \"Ação\",\n" +
                                                    "  \"numeroEpisodios\": 220" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Anime cadastrado com sucesso"),
                    @ApiResponse(responseCode = "409", description = "Já existe um anime cadastrado com o mesmo nome"),
                    @ApiResponse(responseCode = "400", description = "O número de episódios deve ser maior que zero"),
                    @ApiResponse(responseCode = "403", description = "Somente administradores podem cadastrar animes")
            }
    )
    public ResponseEntity<Object> cadastrarAnime(@RequestBody AnimeDTO animeDTO, HttpSession session) {
        try {
            // Converte o DTO para a entidade Anime
            Usuario usuario = (Usuario) session.getAttribute("user");
            Anime anime = convertToAnimeEntity(animeDTO);
            Anime novoAnime = gerenciadorAnimes.createAnime(anime, usuario);

            // Retorna o novo anime convertido de volta para DTO
            AnimeDTO novoAnimeDTO = convertToAnimeDTO(novoAnime);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAnimeDTO);
        } catch (AnimeDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // HTTP 409
        } catch (NumeroDeEpisodiosInvalidoException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // HTTP 400
        } catch (AutorizacaoNegadaException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // HTTP 403
        }
    }

    // Listar todos os animes
    @GetMapping("/list")
    @Operation(
            summary = "Listar todos os animes",
            description = "Retorna uma lista de todos os animes cadastrados no sistema",
            tags = {"Animes"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de animes retornada com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AnimeDTO.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<AnimeDTO>> listarAnimes() {
        List<Anime> animes = gerenciadorAnimes.findAllAnime();

        // Converter lista de Anime para lista de AnimeDTO
        List<AnimeDTO> animeDTOs = animes.stream().map(AnimeMapper::convertToAnimeDTO).collect(Collectors.toList());

        return ResponseEntity.ok(animeDTOs);
    }

    // Listar anime por nome
    @GetMapping("/list/{nome}")
    @Operation(
            summary = "Listar animes por nome",
            description = "Retorna uma lista de animes que correspondem ao nome fornecido",
            tags = {"Animes"},
            parameters = {
                    @Parameter(name = "nome", description = "Nome do anime a ser buscado", required = true, example = "Naruto")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de animes retornada com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AnimeDTO.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<AnimeDTO>> listarAnimesPorNome(@PathVariable String nome) {
        List<Anime> animes = gerenciadorAnimes.findAnimeByNome(nome);

        // Converter lista de Anime para lista de AnimeDTO
        List<AnimeDTO> animeDTOs = animes.stream().map(AnimeMapper::convertToAnimeDTO).collect(Collectors.toList());

        return ResponseEntity.ok(animeDTOs);
    }


    // Buscar um anime por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar um anime por ID",
            description = "Retorna os detalhes de um anime específico pelo seu ID",
            tags = {"Animes"},
            parameters = {
                    @Parameter(name = "id", description = "ID do anime a ser buscado", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Anime encontrado com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AnimeComAvaliacaoDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Anime não encontrado",
                            content = @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public ResponseEntity<Object> buscarAnimePorId(@PathVariable Long id) {
        try {
            Anime anime = gerenciadorAnimes.findAnimeById(id);
            AnimeComAvaliacaoDTO animeAvaliacaoDTO = convertToAnimeComAvaliacaoDTO(anime);
            return ResponseEntity.ok(animeAvaliacaoDTO);
        } catch (AnimeInexistenteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // HTTP 404
        }
    }

    // Atualizar um anime
    @PutMapping("/atualizar/{id}")
    @Operation(
            summary = "Atualizar um anime",
            description = "Atualiza os detalhes de um anime existente Pondendo ser atualizado o nome, gênero e número de episódios, " +
                    "aceitando contendo apenas um desses dados para ser atualizado (favor checar os exemplos)",
            tags = {"Animes"},
            parameters = {
                    @Parameter(name = "id", description = "ID do anime a ser atualizado", required = true, example = "1")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto JSON do anime com os novos dados",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnimeDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Exemplo de Anime Completo",
                                            description = "Exemplo de um objeto de anime para atualização",
                                            value = "{\n" +
                                                    "  \"nome\": \"Naruto Shippuden\",\n" +
                                                    "  \"genero\": \"Ação\",\n" +
                                                    "  \"numeroEpisodios\": 500\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Exemplo de nome de Anime",
                                            description = "Exemplo de um objeto de anime para atualização",
                                            value = "{\n" +
                                                    "  \"nome\": \"Naruto Shippuden\"" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Exemplo de genero de Anime",
                                            description = "Exemplo de um objeto de anime para atualização",
                                            value = "{\n" +
                                                    "  \"genero\": \"Ação\"" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Exemplo de número de episódios de Anime",
                                            description = "Exemplo de um objeto de anime para atualização",
                                            value = "{\n" +
                                                    "  \"numeroEpisodios\": 500\n" +
                                                    "}"
                                    )

                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Anime atualizado com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AnimeDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Anime não encontrado ou conflito de duplicação",
                            content = @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Acesso negado, somente administradores podem atualizar animes",
                            content = @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public ResponseEntity<Object> atualizarAnime(@PathVariable Long id, @RequestBody AnimeDTO animeDTO, HttpSession session) {
        try {
            Anime animeAtualizado = convertToAnimeEntity(animeDTO);
            animeAtualizado.setId(id);
            Usuario usuario = (Usuario) session.getAttribute("user");

            Anime anime = gerenciadorAnimes.atualizarAnime(id, animeAtualizado, usuario);
            AnimeDTO animeAtualizadoDTO = convertToAnimeDTO(anime);
            return ResponseEntity.ok(animeAtualizadoDTO);
        } catch (AnimeInexistenteException | AnimeDuplicadoException | NumeroDeEpisodiosInvalidoException | AutorizacaoNegadaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // HTTP 404
        }
    }

    // Deletar um anime
    @DeleteMapping("/deletar/{id}")
    @Operation(
            summary = "Deletar um anime",
            description = "Remove um anime existente do sistema",
            tags = {"Animes"},
            parameters = {
                    @Parameter(name = "id", description = "ID do anime a ser deletado", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Anime deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Anime não encontrado",
                            content = @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public ResponseEntity<Object> deletarAnime(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("user");

            gerenciadorAnimes.deletarAnime(id, usuario);
            return ResponseEntity.ok("Anime e suas avaliações foram deletados com sucesso.");
        } catch (AnimeInexistenteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // HTTP 404
        } catch (AutorizacaoNegadaException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    private AvaliacaoPeloIdDTO convertToComIdDTO(Avaliacao avaliacao) {
        return AvaliacaoMapper.convertToComIdDTO(avaliacao);
    }

    // Listar os animes mais avaliados
    @GetMapping("/list/mais-avaliados")
    @Operation(
            summary = "Listar os animes mais bem avaliados",
            description = "Retorna uma lista de animes com nota média maior ou igual a 4.5",
            tags = {"Animes"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de animes retornada com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AnimeDTO.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<AnimeDTO>> listarAnimesMaisAvaliados() {
        List<Anime> animes = gerenciadorAnimes.findAllAnime();

        // Converter lista de Anime para lista de AnimeDTO
        List<AnimeDTO> animeDTOs = animes.stream().map(AnimeMapper::convertToAnimeDTO).sorted(Comparator.comparing(AnimeDTO::getNotaMedia).reversed()).filter(Animes -> Animes.getNotaMedia() >= 4.5).collect(Collectors.toList());

        return ResponseEntity.ok(animeDTOs);
    }

    // Listar os animes por genero
    @GetMapping("/list/generos/{Genero}")
    @Operation(
            summary = "Listar animes por gênero",
            description = "Retorna uma lista de animes que correspondem ao gênero fornecido",
            tags = {"Animes"},
            parameters = {
                    @Parameter(name = "Genero", description = "Gênero dos animes a serem buscados", required = true, example = "Ação")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de animes retornada com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AnimeDTO.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<AnimeDTO>> listarAnimesGenero(@PathVariable String Genero) {
        List<Anime> animes = gerenciadorAnimes.findAllAnime();

        // Converter lista de Anime para lista de AnimeDTO
        List<AnimeDTO> animeDTOs = animes.stream().map(AnimeMapper::convertToAnimeDTO).filter(Animes -> Animes.getGenero().equalsIgnoreCase(Genero)).collect(Collectors.toList());

        return ResponseEntity.ok(animeDTOs);
    }
}



