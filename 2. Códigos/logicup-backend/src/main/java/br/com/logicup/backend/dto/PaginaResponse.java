package br.com.logicup.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Sprint 11: uma página de resultados, num formato NOSSO e estável.
 * Não devolvemos o Page do Spring direto: o JSON dele é grande, cheio de detalhes internos, e pode mudar
 * entre versões do Spring (o próprio Spring avisa contra isso). Aqui só vai o que a tela usa.
 */
public record PaginaResponse<T>(List<T> itens, int pagina, int totalPaginas, long total) {

    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> converter) {
        return new PaginaResponse<>(pagina.getContent().stream().map(converter).toList(), pagina.getNumber(),
                pagina.getTotalPages(), pagina.getTotalElements());
    }
}
