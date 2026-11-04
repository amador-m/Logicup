import { useEffect, useImperativeHandle, useRef, useState } from "react";
import * as Blockly from "blockly/core";
import { registrarBlocos } from "../../../blocos/blocosJava";
import { gerarJava } from "../../../blocos/geradorJava";
import { CAIXA_COMPLETA, ligarCategoriaVariaveis } from "../../../blocos/caixaDeFerramentas";
import { temaLogicup } from "../../../blocos/temaLogicup";
import JanelaDoBlockly from "./JanelaDoBlockly";
import { Area } from "./styles";

/*
 * Sprint 9 (ADR-001): a área de blocos (Blockly) usada no Laboratório e nas questões de blocos.
 * Este arquivo só é baixado quando uma área de blocos aparece (ver index.jsx: carregamento sob demanda).
 *
 * Props:
 *   estadoInicial  -> blocos para começar (formato JSON do Blockly); sem ele, começa só com o main
 *   comCaixa       -> true = com a caixa de ferramentas (categorias); false = sem (questão: só os blocos dados)
 *   aoMudar        -> chamado a cada mudança com { trecho, programa, faltando, foraDoMain, temMain, estado }
 *   somenteLeitura -> mostra os blocos sem deixar mexer (questão já corrigida)
 *   rotulo         -> nome da área para leitores de tela
 *   ref            -> .carregar(estado) e .limpar(), usados pelos botões do laboratório
 *
 * Por que o Blockly fica num useEffect: ele não é um componente React, ele "toma conta" de uma <div>
 * (Blockly.inject) e desenha um SVG dentro dela. O React só cria a <div>; o efeito liga o Blockly quando
 * ela aparece e o DESLIGA (dispose) quando ela some, para não sobrar nada na memória.
 */
const PROGRAMA_VAZIO = {
    blocks: { languageVersion: 0, blocks: [{ type: "java_main", x: 24, y: 24, deletable: false }] },
};

const BLOCOS_PRINCIPAIS = ["java_main", "java_algoritmo", "java_raiz"];

const AreaBlocos = ({ estadoInicial, comCaixa = false, aoMudar, somenteLeitura = false, rotulo = "Área de blocos", ref }) => {
    const divRef = useRef(null);
    const areaRef = useRef(null);
    // aoMudar muda a cada renderização da página; guardamos a versão mais nova sem recriar o Blockly
    const aoMudarRef = useRef(aoMudar);
    useEffect(() => { aoMudarRef.current = aoMudar; }, [aoMudar]);
    const [pedido, setPedido] = useState(null); // janela aberta pelo Blockly (nome de variável, confirmação)

    useImperativeHandle(ref, () => ({
        carregar: (estado) => carregarEstado(areaRef.current, estado),
        limpar: () => carregarEstado(areaRef.current, PROGRAMA_VAZIO),
    }), []);

    useEffect(() => {
        registrarBlocos();
        const caixa = comCaixa ? CAIXA_COMPLETA : null;
        const celular = window.matchMedia("(max-width: 700px)").matches;

        /*
         * O Blockly pede nomes e confirmações com janelas do navegador (prompt/confirm), que não dá para
         * estilizar e travam a página. Trocamos pelas nossas janelas (JanelaDoBlockly).
         */
        const pedir = (tipo) => (mensagem, padraoOuCallback, callback) => setPedido({
            tipo, mensagem,
            padrao: tipo === "prompt" ? padraoOuCallback : "",
            responder: tipo === "prompt" ? callback : padraoOuCallback,
        });
        Blockly.dialog.setPrompt(pedir("prompt"));
        Blockly.dialog.setConfirm(pedir("confirm"));
        Blockly.dialog.setAlert((mensagem, callback) => setPedido({ tipo: "alert", mensagem, responder: callback }));

        const area = Blockly.inject(divRef.current, {
            toolbox: caixa ?? undefined,
            theme: temaLogicup,
            renderer: "zelos",            // blocos arredondados e maiores: bons para o toque no celular
            readOnly: somenteLeitura,
            media: "/blockly/media/",     // ícones servidos pelo próprio site (o padrão buscaria em outro domínio)
            sounds: false,
            trashcan: Boolean(caixa) && !somenteLeitura,
            horizontalLayout: celular && Boolean(caixa), // no celular, as categorias ficam numa linha em cima
            toolboxPosition: "start",
            maxInstances: { java_main: 1 },
            grid: { spacing: 24, length: 3, colour: "#e8e1cc", snap: true },
            zoom: { controls: true, wheel: false, pinch: true, startScale: celular ? 0.7 : 0.85, maxScale: 1.6, minScale: 0.4 },
            move: { scrollbars: true, drag: true, wheel: true },
        });
        areaRef.current = area;
        if (caixa) ligarCategoriaVariaveis(area);

        // blocos soltos (fora do main) ficam apagadinhos: o Java não executa o que não está no main
        area.addChangeListener(Blockly.Events.disableOrphans);

        const avisar = () => {
            const resultado = gerarJava(area);
            marcarFaltando(area, resultado.faltando);
            aoMudarRef.current?.({ ...resultado, estado: Blockly.serialization.workspaces.save(area) });
        };
        area.addChangeListener((evento) => {
            if (!evento.isUiEvent) avisar(); // mexer na tela (rolar, selecionar) não muda o programa
        });
        carregarEstado(area, estadoInicial ?? PROGRAMA_VAZIO);
        // questão no celular: os blocos soltos ficariam fora da tela, à direita. "cleanUp" do Blockly
        // os empilha numa coluna (a ordem embaralhada continua), e a tela volta para o canto de cima.
        if (celular && !caixa) {
            area.cleanUp();
            mostrarCanto(area);
        }
        avisar();

        // o Blockly precisa saber quando a caixa dele muda de tamanho (girar o celular, trocar de aba)
        const observador = new ResizeObserver(() => Blockly.svgResize(area));
        observador.observe(divRef.current);

        return () => {
            observador.disconnect();
            area.dispose();
            areaRef.current = null;
        };
        // o Blockly é criado UMA vez por montagem; para trocar comCaixa/somenteLeitura, a página troca a "key"
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fecharPedido = (resposta) => {
        const atual = pedido;
        setPedido(null);
        atual?.responder?.(resposta);
    };

    return (
        <>
            <Area ref={divRef} role="application" aria-label={rotulo} />
            {pedido && <JanelaDoBlockly pedido={pedido} aoResponder={fecharPedido} />}
        </>
    );
};

function carregarEstado(area, estado) {
    if (!area) return;
    try {
        Blockly.serialization.workspaces.load(estado, area);
    } catch {
        Blockly.serialization.workspaces.load(PROGRAMA_VAZIO, area); // estado estragado (ex.: salvo por versão antiga)
    }
    // sem bloco principal (main, algoritmo da Fase 1 ou o bloco de fora dos blocos de linha): começa do zero
    if (!area.getTopBlocks(false).some((bloco) => BLOCOS_PRINCIPAIS.includes(bloco.type))) {
        Blockly.serialization.workspaces.load(PROGRAMA_VAZIO, area);
    }
    area.clearUndo(); // "desfazer" não deve desmontar o que acabou de ser carregado
    mostrarCanto(area);
}

/** Mostra o canto de cima à esquerda (onde fica o main), e não o meio do desenho. */
function mostrarCanto(area) {
    const caixa = area.getBlocksBoundingBox();
    area.scroll(-caixa.left * area.scale + 16, -caixa.top * area.scale + 16);
}

/** Mostra o "!" de aviso nos blocos com encaixe vazio (e tira dos que já foram completados). */
const AVISO_FALTANDO = "Falta encaixar um bloco aqui.";
const comAviso = new WeakSet(); // só mexe no aviso quando ele muda (mexer sempre geraria eventos à toa)

function marcarFaltando(area, ids) {
    const comFalta = new Set(ids);
    area.getAllBlocks(false).forEach((bloco) => {
        const precisa = comFalta.has(bloco.id);
        if (precisa === comAviso.has(bloco)) return;
        bloco.setWarningText(precisa ? AVISO_FALTANDO : null);
        if (precisa) comAviso.add(bloco); else comAviso.delete(bloco);
    });
}

export default AreaBlocos;
