import gatoClaro from "../assets/avatares/gato-claro.webp";
import gatoEscuro from "../assets/avatares/gato-escuro.webp";
import cachorroClaro from "../assets/avatares/cachorro-claro.webp";
import cachorroEscuro from "../assets/avatares/cachorro-escuro.webp";
import baristaClaro from "../assets/avatares/barista-claro.webp";
import baristaEscuro from "../assets/avatares/barista-escuro.webp";
import passaroPao from "../assets/avatares/passaro-pao.webp";
import peixeBiscoito from "../assets/avatares/peixe-biscoito.webp";

/*
 * Sprint 9: a IMAGEM de cada avatar (arte do Davi, pasta "Avatares usuários", reduzida para 256px em WebP).
 * O código (GATO_CLARO...) é o mesmo do enum Avatar do backend: nome, preço e "quem possui" vêm da API.
 * Avatar novo do Davi = imagem nesta pasta + uma linha aqui + uma constante no enum do backend.
 */
export const IMAGEM_AVATAR = {
    GATO_CLARO: gatoClaro,
    GATO_ESCURO: gatoEscuro,
    CACHORRO_CLARO: cachorroClaro,
    CACHORRO_ESCURO: cachorroEscuro,
    BARISTA_CLARO: baristaClaro,
    BARISTA_ESCURO: baristaEscuro,
    PASSARO_PAO: passaroPao,
    PEIXE_BISCOITO: peixeBiscoito,
};

export const AVATAR_PADRAO = "GATO_CLARO";

/** Código desconhecido (ex.: avatar novo no backend antes do frontend) cai no padrão: a tela nunca quebra. */
export function imagemDoAvatar(codigo) {
    return IMAGEM_AVATAR[codigo] ?? IMAGEM_AVATAR[AVATAR_PADRAO];
}
