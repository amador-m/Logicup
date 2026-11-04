import * as Blockly from "blockly/core";
import { CORES } from "./cores";

export { CORES };

const estilo = (cor) => ({ colourPrimary: cor });

export const temaLogicup = Blockly.Theme.defineTheme("logicup", {
    name: "logicup",
    base: Blockly.Themes.Classic,
    blockStyles: {
        estilo_programa: estilo(CORES.programa),
        estilo_variaveis: estilo(CORES.variaveis),
        estilo_saida: estilo(CORES.saida),
        estilo_valores: estilo(CORES.valores),
        estilo_operadores: estilo(CORES.operadores),
        estilo_condicionais: estilo(CORES.condicionais),
        estilo_repeticao: estilo(CORES.repeticao),
        estilo_passo: estilo(CORES.passo),
        estilo_linha: estilo(CORES.linha),
        estilo_bloco: estilo(CORES.bloco),
    },
    categoryStyles: {
        categoria_variaveis: { colour: CORES.variaveis },
        categoria_saida: { colour: CORES.saida },
        categoria_valores: { colour: CORES.valores },
        categoria_operadores: { colour: CORES.operadores },
        categoria_condicionais: { colour: CORES.condicionais },
        categoria_repeticao: { colour: CORES.repeticao },
    },
    componentStyles: {
        workspaceBackgroundColour: "#fbf9ef",
        // caixa de ferramentas clara, no tom do "caderno" do laboratório; os nomes em marrom-escuro
        toolboxBackgroundColour: "#f3ecd9",
        toolboxForegroundColour: "#40261b",
        flyoutBackgroundColour: "#efe9d6",
        flyoutForegroundColour: "#040423",
        flyoutOpacity: 0.97,
        scrollbarColour: "#a4745c",
        insertionMarkerColour: "#040423",
        insertionMarkerOpacity: 0.3,
    },
    fontStyle: { family: "Poppins, sans-serif", weight: "600", size: 11 },
});
