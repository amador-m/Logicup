import { Rotulo, Erro } from "./styles";

/*
 * Sprint 8: caixa de aceite dos Termos de Uso e da Política de Privacidade.
 * Usada no cadastro e na janela de aceite pendente, para o texto ser IGUAL nos dois lugares.
 *
 *  - nunca vem marcada (o aceite precisa de uma ação da pessoa);
 *  - os links abrem em NOVA ABA: assim a pessoa lê o documento sem perder o que já digitou no cadastro
 *    (e sem fechar a janela de aceite, que cobre a página);
 *  - "aceito" vale para os Termos; para a Política é "estou ciente", porque ela informa, não pede permissão.
 */
const CaixaAceite = ({ id, marcado, aoMudar, erro, desabilitado = false }) => (
    <div>
        <Rotulo htmlFor={id}>
            <input
                id={id}
                name="aceiteTermos"
                type="checkbox"
                checked={marcado}
                onChange={(evento) => aoMudar(evento.target.checked)}
                disabled={desabilitado}
                required
                aria-invalid={erro ? true : undefined}
                aria-describedby={erro ? `${id}-erro` : undefined}
            />
            <span>
                Li e aceito os{" "}
                <a href="/termos" target="_blank" rel="noopener noreferrer">Termos de Uso</a>{" "}
                e estou ciente da{" "}
                <a href="/privacidade" target="_blank" rel="noopener noreferrer">Política de Privacidade</a>.
                <span className="nova-aba"> (os links abrem em uma nova aba)</span>
            </span>
        </Rotulo>
        {erro && <Erro id={`${id}-erro`}>{erro}</Erro>}
    </div>
);

export default CaixaAceite;
