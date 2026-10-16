/*
 * Ícones das fases (arte do Davi), já cortados e reduzidos para a web.
 * Nome do arquivo: <tipo>-<estado>.png
 *   tipo   = puzzle, code, complete, truefalse, order, multichoice, predict, associate, blocks, boss
 *   estado = yellow (disponível), green (concluída), block (bloqueada / em breve), orange (Boss disponível)
 *            + a versão "press" (yellowpress, greenpress, orangepress) para o efeito de botão pressionado.
 *
 * import.meta.glob é um recurso do Vite: em tempo de build ele encontra todos os PNGs desta pasta
 * e devolve { "./puzzle-yellow.png": "/assets/puzzle-yellow-abc123.png", ... } (URL final de cada um).
 * Assim não é preciso escrever 52 "import" à mão.
 */
const arquivos = import.meta.glob("./*.png", { eager: true, import: "default" });

/** URL do ícone; se a combinação não existir, cai no ícone de puzzle (nunca quebra a tela). */
export function iconeFase(tipo, estado) {
    return arquivos[`./${tipo}-${estado}.png`] ?? arquivos[`./puzzle-${estado}.png`];
}
