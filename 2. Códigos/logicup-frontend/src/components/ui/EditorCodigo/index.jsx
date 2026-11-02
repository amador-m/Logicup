import { lazy, Suspense } from "react";
import { Janela, Barra } from "../CodigoJava/styles";
import { CorpoEditor, Carregando, AjudaTeclado } from "./styles";

/*
 * Sprint 9 (ADR-001): o editor de código, com a mesma "janela de IDE" do CodigoJava.
 *
 * lazy + Suspense = CARREGAMENTO SOB DEMANDA: o CodeMirror (~150 KB) vira um arquivo separado no build
 * e só é baixado quando um editor aparece na tela pela primeira vez. Enquanto baixa, mostramos o
 * "Carregando editor...". O resto do site não fica mais pesado por causa dele.
 *
 * Props: valor, aoMudar(texto), desabilitado, titulo (aba), rotulo (leitor de tela)
 */
const EditorReal = lazy(() => import("./EditorCodigo"));

const EditorCodigo = ({ valor, aoMudar, desabilitado = false, titulo = "Seu código", rotulo = "Editor de código Java" }) => (
    <>
        <Janela>
            <Barra aria-hidden="true">
                <span className="bolinhas"><i /><i /><i /></span>
                <span className="aba">{titulo}</span>
            </Barra>
            <CorpoEditor>
                <Suspense fallback={<Carregando role="status">Carregando editor...</Carregando>}>
                    <EditorReal valor={valor} aoMudar={aoMudar} desabilitado={desabilitado} rotulo={rotulo} />
                </Suspense>
            </CorpoEditor>
        </Janela>
        {/* Tab recua o código dentro do editor; para sair dele pelo teclado: Esc e depois Tab (acessibilidade) */}
        {!desabilitado && <AjudaTeclado>Tab recua o código · para sair do editor com o teclado: Esc e depois Tab</AjudaTeclado>}
    </>
);

export default EditorCodigo;
