import { createGlobalStyle } from "styled-components";

export default createGlobalStyle`
    /* Sprint 8: a fonte Poppins é carregada no main.jsx a partir do próprio site (@fontsource), sem Google Fonts */

    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    :root {
        --yellow: #ecac0c;
        --orange: #e35c33;
        /* Sprint 11 (acessibilidade): o laranja da marca com texto branco tem contraste 3,6:1, abaixo dos 4,5:1
           da WCAG AA para texto comum. Este tom mais escuro (5,5:1 com branco e 4,8:1 sobre o creme) vai onde há
           TEXTO: botões, mensagens de erro, números. O --orange continua nas decorações e sobre fundo escuro. */
        --orange-texto: #b8401c;
        --cream: #f1f0e2;
        --dark: #040423;
        --green: #288078;
        --brown-light: #a4745c;
        --brown-dark: #764935;
        --white: #ffffff;
    }

    body {
        background-color: var(--cream);
        color: var(--dark);
        font-family: 'Poppins', sans-serif;
    }

    ul, nav {
        list-style: none;
    }

    a {
        text-decoration: none;
        color: inherit;
    }

    button, input {
        font-family: 'Poppins', sans-serif;
    }
`;