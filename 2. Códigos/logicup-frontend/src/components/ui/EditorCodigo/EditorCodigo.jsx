import { useEffect, useRef } from "react";
import { EditorState, Compartment } from "@codemirror/state";
import { EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter, drawSelection } from "@codemirror/view";
import { defaultKeymap, history, historyKeymap, indentWithTab } from "@codemirror/commands";
import { indentOnInput, bracketMatching, indentUnit, syntaxHighlighting, HighlightStyle } from "@codemirror/language";
import { closeBrackets, closeBracketsKeymap } from "@codemirror/autocomplete";
import { java } from "@codemirror/lang-java";
import { tags } from "@lezer/highlight";

/*
 * Sprint 9 (ADR-001): o editor de código de verdade (CodeMirror 6).
 * Este arquivo é carregado SOB DEMANDA (ver index.jsx): quem nunca abre uma questão de código
 * não baixa o CodeMirror.
 *
 * Como o CodeMirror funciona com o React:
 *   o CodeMirror NÃO é um componente React; ele controla o próprio pedaço da página (o <div> do ref).
 *   - useEffect cria o editor UMA vez (EditorView) e o destrói quando o componente sai da tela;
 *   - a cada mudança no texto, o "updateListener" avisa o React (aoMudar);
 *   - se o valor mudar POR FORA (ex.: o aluno refaz a questão), substituímos o texto do editor;
 *   - "Compartment" é uma "gaveta" de configuração que dá para trocar depois: usamos para ligar e
 *     desligar o modo somente leitura sem recriar o editor.
 *
 * Escolhas para iniciantes: sem autocompletar com pop-up e sem busca (distraem); com números de linha
 * (as mensagens de erro citam a linha), recuo automático, fecha parênteses/chaves e Ctrl+Z.
 */

// As MESMAS cores do componente CodigoJava (teoria e enunciados): o aluno reconhece o código.
const CORES = HighlightStyle.define([
    { tag: [tags.keyword, tags.modifier, tags.controlKeyword, tags.operatorKeyword, tags.definitionKeyword, tags.bool, tags.null, tags.self], color: "#ff9e64", fontWeight: "600" },
    { tag: [tags.string, tags.character], color: "#9ece6a" },
    { tag: [tags.comment, tags.lineComment, tags.blockComment], color: "#8b90a6", fontStyle: "italic" },
    { tag: [tags.number, tags.integer, tags.float], color: "#7dcfff" },
    { tag: [tags.typeName, tags.className, tags.standard(tags.typeName)], color: "#e0af68" },
    { tag: [tags.function(tags.variableName), tags.function(tags.propertyName)], color: "#7aa2f7" },
    { tag: [tags.annotation, tags.meta], color: "#e0d561" },
]);

const TEMA = EditorView.theme(
    {
        "&": { backgroundColor: "#1e1f29", color: "#e6e6e6", fontSize: "14px" },
        ".cm-scroller": { fontFamily: '"JetBrains Mono", Consolas, "Courier New", monospace', lineHeight: "1.7" },
        ".cm-content": { padding: "12px 0", caretColor: "#ff9e64", minHeight: "190px" },
        ".cm-gutters": { backgroundColor: "#1e1f29", color: "#6b6f80", borderRight: "1px solid #343646" },
        ".cm-activeLine": { backgroundColor: "rgba(255, 255, 255, 0.04)" },
        ".cm-activeLineGutter": { backgroundColor: "rgba(255, 255, 255, 0.06)", color: "#c9ccd6" },
        "&.cm-focused .cm-cursor": { borderLeftColor: "#ff9e64", borderLeftWidth: "2px" },
        "&.cm-focused .cm-selectionBackground, .cm-selectionBackground, ::selection": { backgroundColor: "#3d4166 !important" },
        ".cm-matchingBracket": { backgroundColor: "rgba(255, 158, 100, 0.25)", outline: "1px solid #ff9e64" },
        "&.cm-focused": { outline: "none" },
    },
    { dark: true }
);

const EditorCodigo = ({ valor, aoMudar, desabilitado, rotulo }) => {
    const elementoRef = useRef(null);
    const viewRef = useRef(null);
    const aoMudarRef = useRef(aoMudar);
    const somenteLeitura = useRef(new Compartment());

    // guarda sempre a versão mais nova do callback (sem recriar o editor a cada render)
    useEffect(() => {
        aoMudarRef.current = aoMudar;
    }, [aoMudar]);

    // cria o editor uma única vez
    useEffect(() => {
        const view = new EditorView({
            parent: elementoRef.current,
            state: EditorState.create({
                doc: valor ?? "",
                extensions: [
                    lineNumbers(),
                    highlightActiveLine(),
                    highlightActiveLineGutter(),
                    drawSelection(),
                    history(),
                    indentOnInput(),
                    bracketMatching(),
                    closeBrackets(),
                    indentUnit.of("    "),
                    EditorState.tabSize.of(4),
                    keymap.of([...closeBracketsKeymap, ...defaultKeymap, ...historyKeymap, indentWithTab]),
                    java(),
                    syntaxHighlighting(CORES),
                    TEMA,
                    somenteLeitura.current.of(EditorState.readOnly.of(Boolean(desabilitado))),
                    // celular e leitor de tela: sem corretor ortográfico/maiúscula automática (estragariam o código)
                    EditorView.contentAttributes.of({
                        "aria-label": rotulo,
                        spellcheck: "false",
                        autocorrect: "off",
                        autocapitalize: "off",
                    }),
                    EditorView.updateListener.of((atualizacao) => {
                        if (atualizacao.docChanged) aoMudarRef.current?.(atualizacao.state.doc.toString());
                    }),
                ],
            }),
        });
        viewRef.current = view;
        return () => view.destroy();
        // eslint-disable-next-line react-hooks/exhaustive-deps -- de propósito: o editor é criado uma vez
    }, []);

    // valor mudou por fora (ex.: refazer a questão volta para o código inicial)
    useEffect(() => {
        const view = viewRef.current;
        if (view && valor !== view.state.doc.toString()) {
            view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: valor ?? "" } });
        }
    }, [valor]);

    // liga/desliga o somente leitura sem recriar o editor
    useEffect(() => {
        viewRef.current?.dispatch({
            effects: somenteLeitura.current.reconfigure(EditorState.readOnly.of(Boolean(desabilitado))),
        });
    }, [desabilitado]);

    return <div ref={elementoRef} />;
};

export default EditorCodigo;
