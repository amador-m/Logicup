/*
 * Sprint 10: lê o código secreto dos links enviados por e-mail.
 *
 * O link tem o formato https://logicup.dev.br/confirmar-email#CODIGO. Tudo depois do "#" é o
 * FRAGMENTO: o navegador NUNCA o envia ao servidor (nem no cabeçalho Referer). Assim o código não
 * aparece em log de servidor nenhum; quem o lê é este JavaScript, que o manda no CORPO de um POST.
 *
 * Depois de ler, apagamos o código da barra de endereços (history.replaceState): ele não fica no
 * histórico do navegador nem vai junto se a pessoa copiar o endereço para alguém.
 *
 * Guardado em uma variável do MÓDULO (e não só no estado do componente) porque, em desenvolvimento,
 * o StrictMode do React monta os componentes duas vezes: na segunda vez o "#" já teria sido apagado.
 */
let capturado = null; // { caminho, codigo }

export function lerCodigoDoLink() {
    const fragmento = window.location.hash.slice(1);
    if (fragmento) {
        capturado = { caminho: window.location.pathname, codigo: decodeURIComponent(fragmento) };
        window.history.replaceState(window.history.state, "", window.location.pathname + window.location.search);
    }
    return capturado?.caminho === window.location.pathname ? capturado.codigo : "";
}
