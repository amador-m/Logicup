import { useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { termos } from "../../data/legal/termos";
import { privacidade } from "../../data/legal/privacidade";
import { cookies } from "../../data/legal/cookies";
import { Page, Hero } from "../LearningPath/styles";
import { Documento, Meta, AvisoPreliminar, Sumario, Secao, TabelaRolavel, OutrosDocumentos } from "./styles";

/*
 * Sprint 8: UMA página para os três documentos legais (/termos, /privacidade, /cookies).
 * O conteúdo fica em src/data/legal: a página só desenha. Assim, quando a Luciana revisar um texto,
 * muda-se só o arquivo de dados, sem mexer em layout.
 * Páginas públicas: precisam ser lidas ANTES do cadastro e por quem está com o aceite pendente.
 */
const DOCUMENTOS = { termos, privacidade, cookies };

/** Desenha um bloco: texto = parágrafo; { lista } = lista; { tabela } = tabela com cabeçalho. */
const Bloco = ({ bloco }) => {
    if (typeof bloco === "string") {
        return <p>{bloco}</p>;
    }
    if (bloco.lista) {
        return (
            <ul>
                {bloco.lista.map((item) => <li key={item}>{item}</li>)}
            </ul>
        );
    }
    if (bloco.tabela) {
        return (
            // tabela larga no celular: rola só ela, e não a página inteira
            <TabelaRolavel tabIndex={0} role="region" aria-label="Tabela (role para os lados no celular)">
                <table>
                    <thead>
                        <tr>{bloco.tabela.colunas.map((coluna) => <th key={coluna} scope="col">{coluna}</th>)}</tr>
                    </thead>
                    <tbody>
                        {bloco.tabela.linhas.map((linha) => (
                            <tr key={linha[0]}>
                                {linha.map((celula, i) => <td key={i}>{celula}</td>)}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </TabelaRolavel>
        );
    }
    return null;
};

const Legal = ({ documento }) => {
    const doc = DOCUMENTOS[documento];
    const { hash } = useLocation();

    // ao trocar de documento, começa do topo (o React Router mantém a rolagem da página anterior)
    useEffect(() => {
        if (!hash) window.scrollTo(0, 0);
    }, [documento, hash]);

    const outros = Object.values(DOCUMENTOS).filter((outro) => outro.slug !== doc.slug);

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Documentos do LogiCup</span>
                        <h1>{doc.titulo}</h1>
                        <p>{doc.resumo}</p>
                    </div>
                </Hero>

                <Documento aria-label={doc.titulo}>
                    <Meta>Versão {doc.versao} · Atualizado em {doc.atualizadoEm}</Meta>

                    <AvisoPreliminar role="note">
                        <strong>Versão preliminar.</strong> Este documento descreve o funcionamento atual do LogiCup,
                        um projeto acadêmico em desenvolvimento. Alguns pontos, como a identificação formal do
                        responsável e o canal de contato, serão definidos na versão definitiva.
                    </AvisoPreliminar>

                    <Sumario aria-label="Nesta página">
                        <h2>Nesta página</h2>
                        <ol>
                            {doc.secoes.map((secao, i) => (
                                <li key={secao.titulo}><a href={`#secao-${i + 1}`}>{secao.titulo}</a></li>
                            ))}
                        </ol>
                    </Sumario>

                    {doc.secoes.map((secao, i) => (
                        <Secao key={secao.titulo} id={`secao-${i + 1}`} aria-labelledby={`secao-${i + 1}-titulo`}>
                            <h2 id={`secao-${i + 1}-titulo`}>{i + 1}. {secao.titulo}</h2>
                            {secao.blocos.map((bloco, j) => <Bloco key={j} bloco={bloco} />)}
                        </Secao>
                    ))}

                    <OutrosDocumentos aria-label="Outros documentos">
                        <span>Veja também:</span>
                        {outros.map((outro) => <Link key={outro.slug} to={`/${outro.slug}`}>{outro.titulo}</Link>)}
                    </OutrosDocumentos>
                </Documento>
            </Page>
            <Footer />
        </>
    );
};

export default Legal;
