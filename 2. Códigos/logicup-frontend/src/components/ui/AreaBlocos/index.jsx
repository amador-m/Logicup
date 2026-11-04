import { lazy, Suspense } from "react";
import { Janela, Barra } from "../CodigoJava/styles";
import { Carregando } from "./styles";

/*
 * Sprint 9 (ADR-001): a área de blocos dentro da mesma "janela" do editor e do CodigoJava.
 *
 * Carregamento SOB DEMANDA (lazy + Suspense), como o editor de código: o Blockly (~200 KB compactado)
 * vira um arquivo separado no build e só é baixado quando uma área de blocos aparece.
 * O ref passa direto para a área (React 19 aceita "ref" como prop comum).
 */
const AreaReal = lazy(() => import("./AreaBlocos"));

const AreaBlocos = ({ titulo = "Blocos", altura = 520, ...props }) => (
    <Janela style={{ "--altura": `${altura}px` }}>
        <Barra aria-hidden="true">
            <span className="bolinhas"><i /><i /><i /></span>
            <span className="aba">🧩 {titulo}</span>
        </Barra>
        <Suspense fallback={<Carregando role="status">Carregando blocos...</Carregando>}>
            <AreaReal {...props} />
        </Suspense>
    </Janela>
);

export default AreaBlocos;
