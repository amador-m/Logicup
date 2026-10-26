import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import { conquistaApi } from "../../../services/api";
import { ouvirComemoracao, avisarProgresso } from "../../../util/gamificacao";
import { podeVerConquistas } from "../../../util/papeis";
import { moedaDa, NOME_DO_NIVEL } from "../../../data/conquistas";
import { tutorialEstaAberto } from "../Onboarding/controle";
import IconeGema from "../Gemas/IconeGema";
import Gattuccino from "../../../assets/images/onboarding/gattuccino-acerto.png";
import {
    Dialogo, Faixa, Palco, Brilho, Nivel, Premio, Contador, Botoes, BotaoPrincipal, BotaoTexto,
} from "./styles";

// documentos legais: lugar de ler com calma, sem festa por cima
const ROTAS_SEM_COMEMORACAO = ["/termos", "/privacidade", "/cookies"];

// na missão a festa espera a tela de resultado da rodada (ela pede pelo evento COMEMORAR)
const emMissao = (caminho) => caminho.startsWith("/missao");

/* mesmas cores das moedas (F1-B): bronze laranja, prata verde, ouro amarelo */
const COR_DA_FAIXA = { UNICA: "var(--brown-dark)", BRONZE: "var(--orange)", PRATA: "var(--green)", OURO: "#c98f00" };

/* posição das estrelinhas em volta da moeda: [topo, esquerda, tamanho, atraso] */
const BRILHOS = [
    ["8%", "18%", 26, 0], ["14%", "74%", 20, 0.4], ["62%", "12%", 18, 0.8],
    ["70%", "80%", 24, 0.2], ["2%", "48%", 16, 1.0],
];

/*
 * Sprint 9 (F4): comemoração "Nova conquista!".
 *
 * Decide SE aparece: só para ALUNO com o aceite em dia, nunca por cima do tutorial nem nos documentos legais.
 * Pergunta ao backend (GET /api/conquistas/novas):
 *  - quando a pessoa loga ou troca de página (fora da missão);
 *  - quando alguém pede comemoração (resultado da rodada, entrada em turma, tutorial fechado).
 * Quem GRAVA as medalhas é sempre o backend.
 */
const NovaConquista = () => {
    const { usuario } = useAuth();
    const { pathname } = useLocation();
    const [fila, setFila] = useState([]);
    const filaRef = useRef(fila); // a verificação (dentro do listener) precisa ler a fila ATUAL

    const ativo = podeVerConquistas(usuario) && !usuario.aceitePendente;
    const usuarioId = usuario?.id;

    useEffect(() => {
        filaRef.current = fila;
    }, [fila]);

    useEffect(() => {
        if (!ativo) return undefined;
        let vivo = true;
        const verificar = () => {
            if (filaRef.current.length > 0 || tutorialEstaAberto()) return; // já comemorando, ou tutorial na tela
            conquistaApi.novas()
                .then((novas) => { if (vivo && novas.length > 0 && !tutorialEstaAberto()) setFila(novas); })
                .catch(() => {}); // comemoração é bônus: se falhar, tenta no próximo aviso
        };
        if (!emMissao(pathname)) verificar();
        const pararDeOuvir = ouvirComemoracao(verificar);
        return () => {
            vivo = false;
            pararDeOuvir();
        };
    }, [ativo, usuarioId, pathname]);

    if (!ativo || fila.length === 0 || ROTAS_SEM_COMEMORACAO.includes(pathname)) return null;

    const encerrar = () => {
        setFila([]);
        // marca TODAS como vistas (mesmo fechando antes da última: a pessoa já viu a comemoração)
        conquistaApi.marcarVistas().catch(() => {}).finally(avisarProgresso);
    };

    // key: outra leva de conquistas recomeça da primeira
    return <JanelaConquista key={fila.map((c) => c.codigo + c.nivel).join()} conquistas={fila} aoFechar={encerrar} />;
};

const JanelaConquista = ({ conquistas, aoFechar }) => {
    const dialogoRef = useRef(null);
    const tituloRef = useRef(null);
    const navigate = useNavigate();
    const [indice, setIndice] = useState(0);

    const atual = conquistas[indice];
    const ultima = indice === conquistas.length - 1;

    useEffect(() => {
        const dialogo = dialogoRef.current;
        if (dialogo && !dialogo.open) dialogo.showModal();
        return () => {
            if (dialogo?.open) dialogo.close();
        };
    }, []);

    // a cada medalha, o foco vai para o título: o leitor de tela anuncia a conquista nova
    useEffect(() => {
        tituloRef.current?.focus();
    }, [indice]);

    const verConquistas = () => {
        aoFechar();
        navigate("/conquistas");
    };

    return (
        <Dialogo ref={dialogoRef} aria-labelledby="titulo-conquista" aria-describedby="texto-conquista"
                 onCancel={(evento) => { evento.preventDefault(); aoFechar(); }}>
            <Faixa>Nova conquista!</Faixa>

            {/* key: a animação da moeda roda de novo a cada medalha */}
            <Palco key={indice} aria-hidden="true">
                {BRILHOS.map(([topo, esquerda, tamanho, atraso]) => (
                    <Brilho key={topo + esquerda} $topo={topo} $esquerda={esquerda} $tamanho={tamanho} $atraso={atraso} />
                ))}
                <img className="moeda" src={moedaDa(atual.codigo, atual.nivel)} alt="" />
                <img className="mascote" src={Gattuccino} alt="" />
            </Palco>

            <h2 id="titulo-conquista" ref={tituloRef} tabIndex={-1}>{atual.titulo}</h2>
            <Nivel $cor={COR_DA_FAIXA[atual.nivel]}>{NOME_DO_NIVEL[atual.nivel]}</Nivel>
            <p id="texto-conquista">{atual.descricao}</p>

            <Premio>
                <IconeGema $tamanho={22} />
                +{atual.gemas} gemas
            </Premio>

            {conquistas.length > 1 && <Contador>Conquista {indice + 1} de {conquistas.length}</Contador>}

            <Botoes>
                <BotaoTexto type="button" onClick={verConquistas}>Ver minhas conquistas</BotaoTexto>
                {ultima
                    ? <BotaoPrincipal type="button" onClick={aoFechar}>Continuar</BotaoPrincipal>
                    : <BotaoPrincipal type="button" onClick={() => setIndice((i) => i + 1)}>Próxima</BotaoPrincipal>}
            </Botoes>
        </Dialogo>
    );
};

export default NovaConquista;
