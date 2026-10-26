import { useEffect, useState } from "react";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import IconeGema from "../../components/ui/Gemas/IconeGema";
import LojaAvatares from "../../components/ui/LojaAvatares";
import EstadoVazio from "../../components/ui/EstadoVazio";
import { conquistaApi } from "../../services/api";
import { ouvirProgresso } from "../../util/gamificacao";
import { moedaDa, NOME_DO_NIVEL, unidade } from "../../data/conquistas";
import {
    Page, Hero, CartaoGemas, BotaoLoja, Resumo, Vazio, Grade, Card, Selo, Niveis, Progresso, Barra, Aviso,
} from "./styles";

const COR_DO_SELO = { UNICA: "var(--brown-dark)", BRONZE: "var(--orange)", PRATA: "var(--green)", OURO: "#c98f00" };

const formatarData = (iso) => new Date(iso).toLocaleDateString("pt-BR");

/*
 * O "estado" de uma conquista para a tela:
 *  ganhos   = níveis já desbloqueados (têm data)
 *  maior    = o nível mais alto conquistado (define a cor da moeda grande) ou null (moeda cinza)
 *  proximo  = o primeiro nível que ainda falta (ou null quando completou tudo)
 */
function estadoDa(conquista) {
    const ganhos = conquista.niveis.filter((nivel) => nivel.desbloqueadaEm);
    const maior = ganhos.length > 0 ? ganhos[ganhos.length - 1] : null;
    const proximo = conquista.niveis.find((nivel) => !nivel.desbloqueadaEm) ?? null;
    return { ganhos, maior, proximo };
}

/**
 * Sprint 9 (C1, C2, F1-F3): a estante de medalhas do aluno.
 * Só LÊ (GET /api/conquistas): quem desbloqueia é o backend, no fim de cada rodada.
 */
const Conquistas = () => {
    const [dados, setDados] = useState(null);
    const [erro, setErro] = useState("");
    const [lojaAberta, setLojaAberta] = useState(false);

    useEffect(() => {
        let vivo = true;
        const carregar = () => conquistaApi.listar()
            .then((resposta) => { if (vivo) { setDados(resposta); setErro(""); } })
            .catch((falha) => {
                if (!vivo) return;
                setErro(falha.status === 403
                    ? "As conquistas são do modo aluno: elas contam o progresso de quem responde as missões."
                    : "Não foi possível carregar suas conquistas. Tente recarregar a página.");
            });
        carregar();
        const pararDeOuvir = ouvirProgresso(carregar); // ex.: comprou um avatar -> saldo novo
        return () => {
            vivo = false;
            pararDeOuvir();
        };
    }, []);

    const totalDeMedalhas = dados ? dados.conquistas.reduce((soma, c) => soma + c.niveis.length, 0) : 0;
    const medalhasGanhas = dados
        ? dados.conquistas.reduce((soma, c) => soma + c.niveis.filter((n) => n.desbloqueadaEm).length, 0)
        : 0;

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Conquistas</span>
                        <h1>Sua estante de medalhas</h1>
                        <p>
                            Cada medalha rende gemas. Junte gemas e troque por avatares novos na loja.
                            Bronze, prata e ouro: até onde você consegue chegar?
                        </p>
                    </div>

                    {dados && (
                        <CartaoGemas>
                            <span className="saldo"><IconeGema $tamanho={38} />{dados.gemas}</span>
                            <small>gemas para gastar</small>
                            <BotaoLoja type="button" onClick={() => setLojaAberta(true)}>Loja de avatares</BotaoLoja>
                        </CartaoGemas>
                    )}
                </Hero>

                {erro && <Aviso role="alert">{erro}</Aviso>}
                {!dados && !erro && <Aviso>Carregando suas medalhas...</Aviso>}

                {dados && (
                    <>
                        {medalhasGanhas > 0 ? (
                            <Resumo><strong>{medalhasGanhas}</strong> de {totalDeMedalhas} medalhas conquistadas</Resumo>
                        ) : (
                            <Vazio>
                                {/* Sprint 9 (E2): ninguém ganhou nada ainda -> Gattuccino dormindo */}
                                <EstadoVazio className="sobre-escuro" titulo="Sua estante ainda está vazia.">
                                    O Gattuccino está esperando a primeira medalha. Acerte uma questão na trilha para começar!
                                </EstadoVazio>
                            </Vazio>
                        )}
                        <Grade>
                            {dados.conquistas.map((conquista) => (
                                <CartaoConquista key={conquista.codigo} conquista={conquista} />
                            ))}
                        </Grade>
                    </>
                )}
            </Page>
            <Footer />

            {lojaAberta && <LojaAvatares aoFechar={() => setLojaAberta(false)} />}
        </>
    );
};

const CartaoConquista = ({ conquista }) => {
    const { maior, proximo } = estadoDa(conquista);
    const completa = proximo === null;
    const percentual = proximo ? Math.min(100, Math.round((conquista.valorAtual / proximo.meta) * 100)) : 100;
    const faltam = proximo ? Math.max(0, proximo.meta - conquista.valorAtual) : 0;
    const variosNiveis = conquista.niveis.length > 1;

    return (
        <Card className={maior ? "ganha" : "bloqueada"} $completa={completa}>
            <img className="moeda" src={moedaDa(conquista.codigo, maior?.nivel ?? null)} alt="" />
            <h2>{conquista.titulo}</h2>
            {maior
                ? <Selo $cor={COR_DO_SELO[maior.nivel]}>{NOME_DO_NIVEL[maior.nivel]}</Selo>
                : <Selo $cor="rgba(118, 73, 53, 0.55)">Bloqueada</Selo>}

            {variosNiveis && (
                <Niveis aria-label="Níveis desta conquista">
                    {conquista.niveis.map((nivel) => (
                        <li key={nivel.nivel} className={nivel.desbloqueadaEm ? "" : "falta"}
                            title={nivel.desbloqueadaEm ? `Conquistada em ${formatarData(nivel.desbloqueadaEm)}` : nivel.descricao}>
                            <img src={moedaDa(conquista.codigo, nivel.desbloqueadaEm ? nivel.nivel : null)} alt="" />
                            <span>{NOME_DO_NIVEL[nivel.nivel]}{nivel.desbloqueadaEm ? " ✓" : ""}</span>
                        </li>
                    ))}
                </Niveis>
            )}

            <Progresso>
                {completa ? (
                    <p>
                        {variosNiveis ? "Completa! Você pegou o ouro." : `Conquistada em ${formatarData(maior.desbloqueadaEm)}.`}
                    </p>
                ) : (
                    <>
                        <p>{proximo.descricao}</p>
                        {proximo.meta > 1 && (
                            <>
                                <Barra $percentual={percentual} role="progressbar" aria-valuemin={0}
                                       aria-valuemax={proximo.meta} aria-valuenow={Math.min(conquista.valorAtual, proximo.meta)}
                                       aria-label={`${conquista.titulo}: ${conquista.valorAtual} de ${proximo.meta}`}>
                                    <span />
                                </Barra>
                                <p className="detalhe">
                                    {Math.min(conquista.valorAtual, proximo.meta)}/{proximo.meta} · faltam {faltam} {unidade(conquista.codigo, faltam)}
                                    {" "}para {NOME_DO_NIVEL[proximo.nivel]} (+{proximo.gemas} gemas)
                                </p>
                            </>
                        )}
                        {proximo.meta <= 1 && <p>Vale +{proximo.gemas} gemas.</p>}
                    </>
                )}
            </Progresso>
        </Card>
    );
};

export default Conquistas;
