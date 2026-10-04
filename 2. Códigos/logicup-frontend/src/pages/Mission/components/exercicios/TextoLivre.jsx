/* TEXTO_LIVRE — resposta {"texto": "..."}; o estilo do textarea vem do ExerciseItem */
const TextoLivre = ({ exercicio, valor, aoMudar, desabilitado }) => (
    <textarea
        aria-label={`Resposta da questão ${exercicio.titulo}`}
        placeholder="Escreva sua resposta com suas palavras..."
        maxLength={2000}
        value={valor?.texto ?? ""}
        disabled={desabilitado}
        onChange={(evento) => aoMudar({ texto: evento.target.value })}
    />
);

export default TextoLivre;
