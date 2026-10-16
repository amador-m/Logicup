package br.com.logicup.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratador GLOBAL de erros: qualquer exceção que escapar de um Controller cai aqui
 * e vira uma resposta JSON padronizada (ErroResponse), em vez de uma página de erro HTML.
 * O Spring escolhe o @ExceptionHandler mais específico para cada tipo de exceção.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @Valid falhou (ex.: e-mail com formato errado) -> 400 com a lista de campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, new ErroResponse(400, "Dados inválidos",
                "Verifique os campos informados.", campos));
    }

    // JSON malformado ou valor que não existe no enum (ex.: "perfil": "ADMIN") -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, new ErroResponse(400, "Requisição inválida",
                "O corpo da requisição está ausente ou com formato inválido."));
    }

    // Sprint 7: 409 com o erro NO CAMPO, para o formulário mostrar a mensagem embaixo do username
    @ExceptionHandler(UsernameEmUsoException.class)
    public ResponseEntity<ErroResponse> usernameEmUso(UsernameEmUsoException ex) {
        return resposta(HttpStatus.CONFLICT, new ErroResponse(409, "Conflito", ex.getMessage(),
                Map.of("username", ex.getMessage())));
    }

    // Sprint 7 (perfil): erros de um CAMPO da troca de senha -> 400 com a mensagem embaixo do campo
    @ExceptionHandler(SenhaAtualIncorretaException.class)
    public ResponseEntity<ErroResponse> senhaAtualIncorreta(SenhaAtualIncorretaException ex) {
        return resposta(HttpStatus.BAD_REQUEST, new ErroResponse(400, "Dados inválidos", ex.getMessage(),
                Map.of("senhaAtual", ex.getMessage())));
    }

    @ExceptionHandler(NovaSenhaIgualException.class)
    public ResponseEntity<ErroResponse> novaSenhaIgual(NovaSenhaIgualException ex) {
        return resposta(HttpStatus.BAD_REQUEST, new ErroResponse(400, "Dados inválidos", ex.getMessage(),
                Map.of("novaSenha", ex.getMessage())));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> emailJaCadastrado(EmailJaCadastradoException ex) {
        return resposta(HttpStatus.CONFLICT, new ErroResponse(409, "Conflito", ex.getMessage()));
    }

    // UNIQUE/FK violada no banco (ex.: dois cadastros simultâneos com o mesmo e-mail) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, new ErroResponse(409, "Conflito",
                "Os dados informados conflitam com um registro existente."));
    }

    @ExceptionHandler({CredenciaisInvalidasException.class, SessaoInvalidaException.class})
    public ResponseEntity<ErroResponse> naoAutenticado(RuntimeException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, new ErroResponse(401, "Não autenticado", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, new ErroResponse(404, "Não encontrado", ex.getMessage()));
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponse> requisicaoInvalida(RequisicaoInvalidaException ex) {
        return resposta(HttpStatus.BAD_REQUEST, new ErroResponse(400, "Requisição inválida", ex.getMessage()));
    }

    @ExceptionHandler(RodadaBloqueadaException.class)
    public ResponseEntity<ErroResponse> rodadaBloqueada(RodadaBloqueadaException ex) {
        return resposta(HttpStatus.FORBIDDEN, new ErroResponse(403, "Acesso negado", ex.getMessage()));
    }

    // negação de acesso lançada DENTRO de um Controller/Service (ex.: futuras regras por role) -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, new ErroResponse(403, "Acesso negado",
                "Você não tem permissão para acessar este recurso."));
    }

    // qualquer outra coisa
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> generico(Exception ex) {
        // erros "normais" do Spring MVC (404 rota inexistente, 405 método errado...) já sabem seu status
        if (ex instanceof ErrorResponse erroSpring) {
            HttpStatusCode status = erroSpring.getStatusCode();
            return ResponseEntity.status(status).body(new ErroResponse(status.value(), "Requisição não atendida",
                    "Não foi possível atender a requisição (" + status.value() + ")."));
        }
        // erro inesperado: registra o detalhe no log, mas NÃO expõe ao cliente (pode conter dados internos)
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, new ErroResponse(500, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, ErroResponse corpo) {
        return ResponseEntity.status(status).body(corpo);
    }
}
