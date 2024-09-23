package br.edu.ufape.myufapeanime.myufapeanime.dto.mappers;

import br.edu.ufape.myufapeanime.myufapeanime.dto.anime.AnimeDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.avaliacao.AvaliacaoPeloIdDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.avaliacao.AvaliacaoDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.avaliacao.AvaliacaoUpdateDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.avaliacao.AvalicaoDoAnimeDTO;
import br.edu.ufape.myufapeanime.myufapeanime.dto.usuario.UsuarioComAvaliacaoDTO;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.basica.Avaliacao;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroAnimeExceptions.AnimeInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.cadastro.cadastroUsuarioExceptions.UsuarioInexistenteException;
import br.edu.ufape.myufapeanime.myufapeanime.negocio.fachada.GerenciadorAnimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

public class AvaliacaoMapper {

    @Autowired
    GerenciadorAnimes gerenciador;

    //converte Avaliacao para AvaliacaoComIdDTO
    public static AvaliacaoPeloIdDTO convertToComIdDTO(Avaliacao avaliacao) {
        AvaliacaoPeloIdDTO dto = new AvaliacaoPeloIdDTO();
        dto.setId(avaliacao.getId());
        dto.setNota(avaliacao.getNota());
        dto.setComentario(avaliacao.getComentario());
        dto.setUsuarioAvaliador(avaliacao.getUsuarioAvaliador().getId());
        dto.setAnimeAvaliado(avaliacao.getAnime().getId());

        return dto;
    }

    //converte avaliacao em avaliacaoDTO
    public static AvaliacaoDTO convertToDTO(Avaliacao avaliacao) {
        AvaliacaoDTO dto = new AvaliacaoDTO();
        dto.setId(avaliacao.getId());
        dto.setNota(avaliacao.getNota());
        dto.setComentario(avaliacao.getComentario());

        UsuarioComAvaliacaoDTO usuarioDTO = UsuarioMapper.convertToAvaliacaoDTO(avaliacao.getUsuarioAvaliador());
        dto.setUsuarioAvaliador(usuarioDTO);

        AnimeDTO animeDaAvaliacao = AnimeMapper.convertToAnimeDTO(avaliacao.getAnime());
        dto.setAnimeAvaliado(animeDaAvaliacao);

        return dto;
    }

    // converte avaliacoes em avaliacoesDTO
    public static AvalicaoDoAnimeDTO convertToAvaliacaoDoAnimeDTO(Avaliacao avaliacao) {
        AvalicaoDoAnimeDTO dto = new AvalicaoDoAnimeDTO();
        dto.setId(avaliacao.getId());
        dto.setNota(avaliacao.getNota());
        dto.setComentario(avaliacao.getComentario());
        dto.setNomeUsuario(avaliacao.getUsuarioAvaliador().getNome());
        return dto;
    }
    public static Avaliacao convertToEntity(AvaliacaoPeloIdDTO avaliacaoDTO) throws AnimeInexistenteException, UsuarioInexistenteException {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNota(avaliacaoDTO.getNota());
        avaliacao.setComentario(avaliacaoDTO.getComentario());
        //avaliacao.setUsuarioAvaliador(gerenciador.findUsuarioById(avaliacaoDTO.getUsuarioAvaliador()));

        return avaliacao;
    }

    public static Avaliacao convertToEntityUpdate(AvaliacaoUpdateDTO avaliacaoDTO) throws AnimeInexistenteException {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNota(avaliacaoDTO.getNota());
        avaliacao.setComentario(avaliacaoDTO.getComentario());
        avaliacao.setId(avaliacaoDTO.getId());

        return avaliacao;
    }

}
