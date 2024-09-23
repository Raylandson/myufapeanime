package br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro;

import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Anime;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Avaliacao;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAnimeExceptions.AnimeInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAvaliacaoExceptions.AvaliacaoDuplicadaException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAvaliacaoExceptions.AvaliacaoInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAvaliacaoExceptions.AvaliacaoNotaInvalidaException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroUsuarioExceptions.UsuarioInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.repositorios.InterfaceRepositorioAnimes;
import br.edu.ufape.myufapeanime.myufapeanime.repositorios.InterfaceRepositorioAvaliacoes;
import br.edu.ufape.myufapeanime.myufapeanime.repositorios.InterfaceRepositorioUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Classe responsável por gerenciar as operações relacionadas ao cadastro de avaliações de animes.
 * Realiza operações de criação, atualização, exclusão e busca de avaliações no sistema.
 * Também atualiza a nota média, pontuação e o número total de avaliações de um anime após
 * cada alteração nas avaliações.
 *
 * Esta classe coordena a interação entre avaliações, usuários e animes, garantindo
 * que as regras de negócio, como evitar avaliações duplicadas e validar notas, sejam respeitadas.
 *
 * Regras de Negócio:
 * - Uma avaliação deve ter uma nota entre 0 e 5.
 * - Não pode haver avaliações duplicadas (um usuário avaliando o mesmo anime mais de uma vez).
 * - Um anime e um usuário avaliador devem existir no sistema para que a avaliação seja válida.
 *
 * @author JorgeRibeiro
 */

@Service
public class CadastroAvaliacao implements CadastroInterface<Avaliacao> {

    @Autowired
    private InterfaceRepositorioAvaliacoes avaliacaoRepository;

    @Autowired
    private InterfaceRepositorioAnimes animeRepository;

    @Qualifier("interfaceRepositorioUsuarios")
    @Autowired
    private InterfaceRepositorioUsuarios repositorioUsuario;

    /**
     * Cria uma nova avaliação no sistema.
     * Verifica se a nota está dentro dos parâmetros válidos (0 a 5), se o usuário e o anime
     * associados à avaliação existem no sistema, e se o usuário já avaliou aquele anime.
     * Caso todas as validações sejam bem-sucedidas, salva a avaliação e ajusta a pontuação
     * do anime com base na nova avaliação.
     *
     * @param avaliacao O objeto Avaliacao a ser criado.
     * @return O objeto Avaliacao salvo no banco de dados.
     * @throws AvaliacaoNotaInvalidaException Lançada se a nota for inválida (fora de 0 a 5).
     * @throws UsuarioInexistenteException Lançada se o usuário não existir no banco de dados.
     * @throws AnimeInexistenteException Lançada se o anime não existir no banco de dados.
     * @throws AvaliacaoDuplicadaException Lançada se o usuário já tiver avaliado o mesmo anime.
     */
    @Override
    //TODO: falar com a professora sobre dependencias de outros repositorios no cadastro de avaliação
    public Avaliacao create(Avaliacao avaliacao)
            throws AvaliacaoNotaInvalidaException, UsuarioInexistenteException, AvaliacaoDuplicadaException, AnimeInexistenteException {
    if(avaliacao.getNota() > 5 || avaliacao.getNota() < 0){
        throw new AvaliacaoNotaInvalidaException(avaliacao.getNota());
    }

    if (!repositorioUsuario.existsById(avaliacao.getUsuarioAvaliador().getId())){
        throw new UsuarioInexistenteException(avaliacao.getUsuarioAvaliador().getId());
    }

    if(!animeRepository.existsById(avaliacao.getAnime().getId())){
        throw new AnimeInexistenteException(avaliacao.getAnime().getId());
    }

    if(avaliacaoRepository.existsAvaliacaoByAnimeAndUsuarioAvaliador(avaliacao.getAnime(),avaliacao.getUsuarioAvaliador())){
        throw new AvaliacaoDuplicadaException(avaliacao.getAnime().getId(),avaliacao.getUsuarioAvaliador().getId());
    }
        Avaliacao novaAvaliacao = avaliacaoRepository.save(avaliacao);
        mudarPontuacaoAnime(novaAvaliacao.getAnime(), novaAvaliacao.getNota(), 1L);

        return novaAvaliacao;
    }

    /**
     * Atualiza as informações de uma avaliação existente, mantendo o anime e o usuário avaliador
     * inalterados. Ajusta a pontuação do anime com base na diferença entre a nota anterior e a nova.
     *
     * @param newAvaliacao O objeto Avaliacao com as informações atualizadas.
     * @return O objeto Avaliacao atualizado.
     * @throws AvaliacaoNotaInvalidaException Lançada se a nota for inválida (fora de 0 a 5).
     * @throws AvaliacaoInexistenteException Lançada se a avaliação não existir no banco de dados.
     */
    @Override
    public Avaliacao update(Avaliacao newAvaliacao)
            throws AvaliacaoNotaInvalidaException, AvaliacaoInexistenteException{

        if(newAvaliacao.getNota() > 5 || newAvaliacao.getNota() < 0){
            throw new AvaliacaoNotaInvalidaException(newAvaliacao.getNota());
        }
        //aqui ele já pega a avaliação antiga e joga a exceção
        Avaliacao antigaAvaliacao = avaliacaoRepository.findById(newAvaliacao.getId())
                .orElseThrow(() -> new AvaliacaoInexistenteException(newAvaliacao.getId()));

        newAvaliacao.setAnime(antigaAvaliacao.getAnime());
        newAvaliacao.setUsuarioAvaliador(antigaAvaliacao.getUsuarioAvaliador());

        double notaAntiga = antigaAvaliacao.getNota();
        double notaAtual = newAvaliacao.getNota();
        Anime anime = newAvaliacao.getAnime();
        // eu tiro a pontuação antiga da nova para calcular a diferença
        mudarPontuacaoAnime(anime, notaAtual - notaAntiga, 0L);

        return avaliacaoRepository.save(newAvaliacao);
    }

    /**
     * Exclui uma avaliação do sistema e ajusta a pontuação do anime associado,
     * subtraindo a nota da avaliação excluída e reduzindo o número total de avaliações.
     *
     * @param avaliacao A avaliação a ser deletada.
     * @throws AvaliacaoInexistenteException Lançada se a avaliação não for encontrada no banco de dados.
     */
    @Override
    public void delete(Avaliacao avaliacao) throws AvaliacaoInexistenteException {
        deleteById(avaliacao.getId());
    }

    /**
     * Exclui uma avaliação pelo seu ID e ajusta a pontuação do anime associado.
     *
     * @param id O ID da avaliação a ser deletada.
     * @throws AvaliacaoInexistenteException Lançada se a avaliação não for encontrada no banco de dados.
     */
    @Override
    public void deleteById(Long id) throws AvaliacaoInexistenteException {

        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new AvaliacaoInexistenteException(id));
        //ele tira a nota e tira um do total
        mudarPontuacaoAnime(avaliacao.getAnime(), - avaliacao.getNota(), - 1L);
        avaliacaoRepository.deleteById(id);
    }

    /**
     * Lista todas as avaliações cadastradas no sistema.
     *
     * @return Lista de objetos Avaliacao.
     */
    @Override
    public List<Avaliacao> findAll(){
        return avaliacaoRepository.findAll();
    }


    /**
     * Busca uma avaliação pelo seu ID.
     *
     * @param id O ID da avaliação a ser buscada.
     * @return O objeto Avaliacao encontrado.
     * @throws AvaliacaoInexistenteException Lançada se a avaliação não for encontrada no banco de dados.
     */
    @Override
    public Avaliacao findById(Long id) throws AvaliacaoInexistenteException {
        return avaliacaoRepository.findById(id).orElseThrow(() -> new AvaliacaoInexistenteException(id));
    }


    /**
     * Altera a pontuação de um anime. É chamado quando se cria, atualiza ou exclui uma avaliação.
     *
     * @param anime O anime que terá a pontuação ajustada.
     * @param ajusteNota A diferença na nota a ser aplicada.
     * @param ajusteAvaliacoes A alteração no número total de avaliações (adicionar ou remover).
     */
    private void mudarPontuacaoAnime(Anime anime, Double ajusteNota, Long ajusteAvaliacoes) {
        anime.setPontuacao(anime.getPontuacao() + ajusteNota);
        anime.setAvaliacoesTotais(anime.getAvaliacoesTotais() + ajusteAvaliacoes);
        animeRepository.save(anime);
    }

}
