/*
 * Sprint 9: controle do onboarding (decisão O2: guardado no localStorage, por usuário).
 *
 * A chave leva o id do usuário: se duas pessoas usam o mesmo navegador, cada uma vê o seu tutorial.
 * O valor é a VERSÃO do tutorial: se um dia o conteúdo mudar bastante, trocar a versão faz todos verem de novo.
 * O localStorage pode falhar (aba anônima, armazenamento bloqueado): nesse caso o tutorial aparece de novo
 * na próxima visita — incômodo pequeno, sem quebrar nada.
 */
export const VERSAO_ONBOARDING = "1";
export const EVENTO_ABRIR_TUTORIAL = "logicup:abrir-tutorial";

const chave = (usuarioId) => `logicup.onboarding.${usuarioId}`;

export function jaViuOnboarding(usuarioId) {
    try {
        return localStorage.getItem(chave(usuarioId)) === VERSAO_ONBOARDING;
    } catch {
        return false;
    }
}

export function marcarOnboardingVisto(usuarioId) {
    try {
        localStorage.setItem(chave(usuarioId), VERSAO_ONBOARDING);
    } catch {
        // sem armazenamento: só nesta visita
    }
}

/*
 * Sprint 9: a comemoração "Nova conquista!" não pode aparecer POR CIMA do tutorial.
 * A janela do tutorial avisa aqui quando abre e fecha; a comemoração consulta antes de aparecer.
 * (Variável do módulo: existe uma só no app inteiro, como um "interruptor" compartilhado.)
 */
let tutorialAberto = false;

export function definirTutorialAberto(aberto) {
    tutorialAberto = aberto;
}

export function tutorialEstaAberto() {
    return tutorialAberto;
}

/** Botão "Ver tutorial" (no perfil): avisa o componente Onboarding, que está montado no AppRoutes. */
export function abrirTutorial() {
    window.dispatchEvent(new Event(EVENTO_ABRIR_TUTORIAL));
}
