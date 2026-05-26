import { createGlobalStyle } from "styled-components";

export default createGlobalStyle`
    @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800&display=swap');

    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    :root {
        --yellow: #ecac0c;
        --orange: #e35c33;
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