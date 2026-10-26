package br.com.logicup.backend.model;

/**
 * Sprint 8: documentos legais que o usuário aceita (ou declara ciência) no LogiCup.
 * Os nomes são os mesmos do ENUM da coluna aceite_documento.documento.
 */
public enum DocumentoLegal {
    TERMOS,      // Termos de Uso: ACEITE
    PRIVACIDADE  // Política de Privacidade: declaração de CIÊNCIA (não é consentimento para tratar dados)
}
