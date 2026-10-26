package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AceiteRequest;
import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.exception.VersaoDocumentoDesatualizadaException;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.AceiteDocumento;
import br.com.logicup.backend.model.DocumentoLegal;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AceiteDocumentoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sprint 8: regras do aceite dos documentos legais.
 *   - só vale o aceite da versão VIGENTE (a pessoa precisa ter lido o texto atual);
 *   - grava TERMOS (aceite) e PRIVACIDADE (ciência), uma linha por versão, sem repetir;
 *   - depois do aceite, o login ganha um token NOVO com o aceite dentro (é o que libera a API).
 */
@Service
public class AceiteService {

    private final AceiteDocumentoRepository aceiteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;

    public AceiteService(AceiteDocumentoRepository aceiteRepository, UsuarioRepository usuarioRepository,
                         TokenService tokenService) {
        this.aceiteRepository = aceiteRepository;
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    /** As versões que a pessoa viu na tela são as vigentes? Senão, 409: precisa recarregar e ler de novo. */
    public void exigirVersoesVigentes(String versaoTermos, String versaoPrivacidade) {
        if (!VersoesLegais.TERMOS.equals(versaoTermos) || !VersoesLegais.PRIVACIDADE.equals(versaoPrivacidade)) {
            throw new VersaoDocumentoDesatualizadaException();
        }
    }

    /** Grava o aceite das versões vigentes. Se já existe (mesma versão), não grava de novo. */
    @Transactional
    public void registrarVersoesVigentes(Usuario usuario) {
        for (DocumentoLegal documento : DocumentoLegal.values()) {
            String versao = VersoesLegais.vigente(documento);
            if (!aceiteRepository.existsByUsuarioIdAndDocumentoAndVersao(usuario.getId(), documento, versao)) {
                aceiteRepository.save(new AceiteDocumento(usuario, documento, versao));
            }
        }
    }

    /** O usuário já aceitou as versões vigentes dos dois documentos? (consultado no LOGIN, não a cada requisição) */
    @Transactional(readOnly = true)
    public boolean aceitouVersoesVigentes(Integer idUsuario) {
        for (DocumentoLegal documento : DocumentoLegal.values()) {
            if (!aceiteRepository.existsByUsuarioIdAndDocumentoAndVersao(idUsuario, documento,
                    VersoesLegais.vigente(documento))) {
                return false;
            }
        }
        return true;
    }

    /** POST /api/legal/aceite: valida, grava e devolve o usuário + um token novo (com o aceite). */
    @Transactional
    public AutenticacaoResultado aceitar(Integer idUsuario, AceiteRequest dados) {
        exigirVersoesVigentes(dados.versaoTermos(), dados.versaoPrivacidade());
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(SessaoInvalidaException::new);
        registrarVersoesVigentes(usuario);
        return new AutenticacaoResultado(UsuarioResponse.de(usuario, false), tokenService.gerarToken(usuario, true));
    }
}
