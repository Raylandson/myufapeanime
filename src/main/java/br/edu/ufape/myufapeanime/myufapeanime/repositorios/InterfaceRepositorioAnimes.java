package br.edu.ufape.myufapeanime.myufapeanime.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Anime;

@Repository
public interface InterfaceRepositorioAnimes extends JpaRepository<Anime, Long> {
    Anime findByNome(String nome);
}
