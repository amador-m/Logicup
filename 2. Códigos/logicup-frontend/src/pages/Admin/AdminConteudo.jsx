import { useEffect, useState } from "react";
import { adminApi } from "../../services/api";
import { ROTULO_TIPO } from "../Mission/rotulos";
import { BotaoPequeno, Info, Aviso, Editor, Exercicio, Filtros } from "./styles";

/** Copia o objeto e troca o valor no caminho (ex.: ["teoria", 0, "blocos", 2, "texto"]). Sem mutar o estado. */
function comValor(objeto, caminho, valor) {
    const copia = structuredClone(objeto);
    let alvo = copia;
    caminho.slice(0, -1).forEach((parte) => {
        alvo = alvo[parte];
    });
    alvo[caminho.at(-1)] = valor;
    return copia;
}

/** Linha de status embaixo do botão "Salvar": "Salvo!" ou a mensagem de erro do backend. */
const Status = ({ estado }) => {
    if (estado?.ok) return <span className="ok" role="status">✓ Salvo</span>;
    if (estado?.erro) return <span className="erro" role="alert">{estado.erro}</span>;
    return null;
};

/** Um bloco da teoria: cada tipo tem o seu campo. A edição troca só TEXTOS (sem acrescentar nem remover). */
const CampoBloco = ({ bloco, caminho, alterar, numero }) => {
    if (bloco.tipo === "paragrafo") {
        return (
            <label>Parágrafo {numero}
                <textarea rows={3} value={bloco.texto} onChange={(e) => alterar([...caminho, "texto"], e.target.value)} />
            </label>
        );
    }
    if (bloco.tipo === "codigo") {
        return (
            <label>Código {numero}
                <textarea className="codigo" rows={Math.min(14, bloco.codigo.split("\n").length + 1)} spellCheck={false}
                          value={bloco.codigo} onChange={(e) => alterar([...caminho, "codigo"], e.target.value)} />
            </label>
        );
    }
    if (bloco.tipo === "lista") {
        return (
            <div>
                <Info>Lista {numero} ({bloco.itens.length} itens)</Info>
                {bloco.itens.map((item, i) => (
                    <label key={i}>Item {i + 1}
                        <input value={item} onChange={(e) => alterar([...caminho, "itens", i], e.target.value)} />
                    </label>
                ))}
            </div>
        );
    }
    if (bloco.tipo === "tabela") {
        const colunas = bloco.linhas[0]?.length ?? 1;
        return (
            <div>
                <Info>Tabela {numero}</Info>
                <div className="tabela" style={{ gridTemplateColumns: `repeat(${colunas}, minmax(0, 1fr))` }}>
                    {bloco.linhas.flatMap((linha, l) => linha.map((celula, c) => (
                        <input key={`${l}-${c}`} aria-label={`Linha ${l + 1}, coluna ${c + 1}`} value={celula}
                               onChange={(e) => alterar([...caminho, "linhas", l, c], e.target.value)} />
                    )))}
                </div>
            </div>
        );
    }
    return null;
};

/** Os textos de UM exercício (título, enunciado e explicação). Cada exercício é salvo separado. */
const EditorExercicio = ({ exercicio }) => {
    const [dados, setDados] = useState({ titulo: exercicio.titulo, enunciado: exercicio.enunciado,
        explicacao: exercicio.explicacao });
    const [estado, setEstado] = useState(null);

    const salvar = async (evento) => {
        evento.preventDefault();
        setEstado(null);
        try {
            await adminApi.salvarExercicio(exercicio.id, dados);
            setEstado({ ok: true });
        } catch (e) {
            setEstado({ erro: Object.values(e.campos ?? {})[0] ?? e.message });
        }
    };
    const campo = (nome) => (e) => setDados((atual) => ({ ...atual, [nome]: e.target.value }));

    return (
        <Exercicio>
            <summary>{exercicio.codigo} — {exercicio.titulo}<small>{ROTULO_TIPO[exercicio.tipo] ?? exercicio.tipo}</small></summary>
            <Editor onSubmit={salvar}>
                <label>Título<input value={dados.titulo} maxLength={150} onChange={campo("titulo")} /></label>
                <label>Enunciado<textarea rows={4} value={dados.enunciado} onChange={campo("enunciado")} /></label>
                <label>Explicação (aparece depois da correção)
                    <textarea rows={4} value={dados.explicacao} onChange={campo("explicacao")} />
                </label>
                <div className="acoes">
                    <BotaoPequeno type="submit" className="claro">Salvar questão</BotaoPequeno>
                    <Status estado={estado} />
                </div>
            </Editor>
        </Exercicio>
    );
};

/*
 * Sprint 11: GESTÃO DE CONTEÚDOS (RF24). O admin escolhe uma rodada e edita:
 *  - o título e a teoria (objetivos, títulos das seções, parágrafos, listas, códigos e tabelas);
 *  - o título, o enunciado e a explicação de cada questão.
 * Só TEXTOS: gabarito, alternativas e a estrutura (quantos blocos, de que tipo) não mudam por aqui.
 */
const AdminConteudo = () => {
    const [arvore, setArvore] = useState(null);
    const [rodadaId, setRodadaId] = useState("");
    const [rodada, setRodada] = useState(null);    // o que veio do backend
    const [titulo, setTitulo] = useState("");
    const [conteudo, setConteudo] = useState(null); // a teoria sendo editada
    const [erro, setErro] = useState("");
    const [estado, setEstado] = useState(null);

    useEffect(() => {
        adminApi.conteudo().then(setArvore).catch((e) => setErro(e.message));
    }, []);

    useEffect(() => {
        if (!rodadaId) return undefined;
        let ativo = true;
        adminApi.rodada(rodadaId)
            .then((dados) => {
                if (!ativo) return;
                setRodada(dados);
                setTitulo(dados.titulo);
                setConteudo(dados.conteudo);
            })
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [rodadaId]);

    // trocar de rodada limpa a anterior AQUI (no evento), e não dentro do efeito
    const escolherRodada = (evento) => {
        setRodada(null);
        setEstado(null);
        setRodadaId(evento.target.value);
    };

    const alterar = (caminho, valor) => {
        setConteudo((atual) => comValor(atual, caminho, valor));
        setEstado(null);
    };

    const salvarTeoria = async (evento) => {
        evento.preventDefault();
        setEstado(null);
        try {
            const salva = await adminApi.salvarRodada(rodada.id, titulo, conteudo);
            setRodada(salva);
            setConteudo(salva.conteudo);
            setEstado({ ok: true });
        } catch (e) {
            setEstado({ erro: Object.values(e.campos ?? {})[0] ?? e.message });
        }
    };

    return (
        <>
            <Aviso>
                As alterações valem <strong>na hora</strong> para todos os alunos. Aqui só se editam textos:
                gabaritos, alternativas e a estrutura da trilha continuam como estão. Lembre de levar as mudanças
                também para o documento mestre (Banco de Questões).
            </Aviso>

            {erro && <Info className="erro" role="alert">{erro}</Info>}
            {!arvore && !erro && <Info role="status">Carregando o conteúdo...</Info>}

            {arvore && (
                <Filtros as="div">
                    <label htmlFor="admin-rodada">Rodada</label>
                    <select id="admin-rodada" value={rodadaId} onChange={escolherRodada}>
                        <option value="">Escolha uma rodada para editar</option>
                        {arvore.fases.map((fase) => (
                            <optgroup key={fase.id} label={`Fase ${fase.ordem}: ${fase.titulo}`}>
                                {fase.rodadas.map((r) => (
                                    <option key={r.id} value={r.id}>Rodada {r.ordem}: {r.titulo}</option>
                                ))}
                            </optgroup>
                        ))}
                    </select>
                </Filtros>
            )}

            {rodadaId && !rodada && !erro && <Info role="status">Carregando a rodada...</Info>}

            {rodada && conteudo && (
                <>
                    <Editor onSubmit={salvarTeoria} aria-label="Teoria da rodada">
                        <fieldset>
                            <legend>Fase {rodada.faseOrdem} • Rodada {rodada.ordem}</legend>
                            <label>Título da rodada
                                <input value={titulo} maxLength={150} onChange={(e) => { setTitulo(e.target.value); setEstado(null); }} />
                            </label>
                            {conteudo.objetivos.map((objetivo, i) => (
                                <label key={i}>Objetivo {i + 1}
                                    <textarea rows={2} value={objetivo} onChange={(e) => alterar(["objetivos", i], e.target.value)} />
                                </label>
                            ))}
                        </fieldset>

                        {conteudo.teoria.map((secao, s) => (
                            <fieldset key={s}>
                                <legend>Seção {s + 1}</legend>
                                <label>Título da seção
                                    <input value={secao.titulo} onChange={(e) => alterar(["teoria", s, "titulo"], e.target.value)} />
                                </label>
                                {(secao.blocos ?? []).map((bloco, b) => (
                                    <CampoBloco key={b} bloco={bloco} numero={b + 1}
                                                caminho={["teoria", s, "blocos", b]} alterar={alterar} />
                                ))}
                            </fieldset>
                        ))}

                        <div className="acoes">
                            <BotaoPequeno type="submit" className="claro">Salvar teoria</BotaoPequeno>
                            <Status estado={estado} />
                        </div>
                    </Editor>

                    <Info style={{ marginTop: 22 }}><strong>Questões da rodada</strong></Info>
                    {rodada.exercicios.map((exercicio) => (
                        <EditorExercicio key={exercicio.id} exercicio={exercicio} />
                    ))}
                </>
            )}
        </>
    );
};

export default AdminConteudo;
