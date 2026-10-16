import {
    Container,
    Menu,
    MenuItem,
    Actions,
    LoginButton,
    RegisterButton,
    Img,
    Greeting,
    Navegacao,
    BotaoMenu,
} from "./styles";
import LogoImg from "../../../assets/images/LogoLight.png";
import { useEffect, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";

const Header = () => {
    const { usuario, logout } = useAuth();
    const navigate = useNavigate();

    /*
     * Sprint 7: menu hambúrguer (só aparece abaixo de 900px, pelo CSS).
     * "aberto" controla se o painel com os links está visível no celular.
     * No desktop esse estado não tem efeito: o painel é sempre exibido em linha.
     */
    const [aberto, setAberto] = useState(false);

    // Esc fecha o menu (o listener só existe enquanto o menu está aberto)
    useEffect(() => {
        if (!aberto) return undefined;
        const aoTeclar = (evento) => {
            if (evento.key === "Escape") setAberto(false);
        };
        document.addEventListener("keydown", aoTeclar);
        return () => document.removeEventListener("keydown", aoTeclar);
    }, [aberto]);

    // clicar em qualquer link ou botão DENTRO do painel (navegar, entrar, sair) fecha o menu
    const fecharAoEscolher = (evento) => {
        if (evento.target.closest("a, button")) setAberto(false);
    };

    const handleLogout = async () => {
        /*
         * F3: depois de sair, vai para a Home.
         * A ORDEM importa: primeiro sai da página protegida, depois encerra a sessão.
         * Se fosse ao contrário, o PrivateRoute da página atual veria "ninguém logado"
         * e redirecionaria para /login, disputando com a navegação para a Home.
         */
        navigate("/", { replace: true });
        await logout();
    };

    // primeiro nome, para a saudação não ficar longa
    const primeiroNome = usuario?.nome.split(" ")[0];

    return (
        <Container>
            <Link to="/"><Img><img src={LogoImg} alt="Página inicial do LogicUp" /></Img></Link>

            {/* botão ☰ / ✕: só visível no celular e tablet (CSS) */}
            <BotaoMenu
                type="button"
                className={aberto ? "aberto" : ""}
                aria-expanded={aberto}
                aria-controls="navegacao-principal"
                aria-label={aberto ? "Fechar menu" : "Abrir menu"}
                onClick={() => setAberto((valor) => !valor)}
            >
                <span /><span /><span />
            </BotaoMenu>

            {/*
              * Navegacao agrupa menu + botões da conta. No desktop ela "some" do layout
              * (display: contents) e tudo fica em linha como antes; no celular vira o painel do hambúrguer.
              */}
            <Navegacao id="navegacao-principal" className={aberto ? "aberto" : ""} onClick={fecharAoEscolher}>
            {/*
              * NavLink (react-router) recebe a classe "active" quando a URL atual é a do link:
              * é assim que o item da página atual fica destacado. "end" no Início evita que "/"
              * fique ativo em todas as páginas (toda URL começa com "/").
              */}
            <Menu>
                <MenuItem><NavLink to="/" end>Início</NavLink></MenuItem>
                <MenuItem><NavLink to="/trilha">Trilha</NavLink></MenuItem>
                <MenuItem><NavLink to="/ranking">Ranking</NavLink></MenuItem>
                <MenuItem className="em-breve" aria-disabled="true">
                    Recompensas <small>em breve</small>
                </MenuItem>
            </Menu>

            <Actions>
                {usuario ? (
                    <>
                        {/* Sprint 7: a saudação leva para o perfil */}
                        <Greeting as={NavLink} to="/perfil" title="Meu perfil">Olá, {primeiroNome}</Greeting>
                        <LoginButton type="button" onClick={handleLogout}>Sair</LoginButton>
                    </>
                ) : (
                    <>
                        <LoginButton as={Link} to="/login">Entrar</LoginButton>
                        <RegisterButton as={Link} to="/cadastro">Cadastrar</RegisterButton>
                    </>
                )}
            </Actions>
            </Navegacao>
        </Container>
    );
};

export default Header;
