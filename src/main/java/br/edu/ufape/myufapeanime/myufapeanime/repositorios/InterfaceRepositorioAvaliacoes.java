package br.edu.ufape.myufapeanime.myufapeanime.repositorios;

import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Anime;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Avaliacao;

import java.util.Optional;

@Repository
public interface InterfaceRepositorioAvaliacoes extends JpaRepository<Avaliacao, Long> {

    boolean existsAvaliacaoByAnimeAndUsuarioAvaliador(Anime anime, Usuario usuarioAvaliador);
    Optional<Avaliacao> findAvaliacaoByAnimeAndUsuario(Anime anime, Usuario usuario);

}
