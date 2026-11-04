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
    IconeMenu,
} from "./styles";
import iconeInicio from "../../../assets/icones/home.png";
import iconeTrilha from "../../../assets/icones/map-marker.png";
import iconeRanking from "../../../assets/icones/trofeu.svg"; // provisório: trocar pelo "trophy" do mesmo pacote
import iconeTurma from "../../../assets/icones/users.png";
import iconeEmenta from "../../../assets/icones/clipboard-list-check.png";
import iconeConquistas from "../../../assets/icones/diamond.png";
import iconeBlocos from "../../../assets/icones/blocos.svg";
import LogoImg from "../../../assets/images/LogoLight.png";
import { useEffect, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import { podeVerEmenta, podeGerenciarTurmas, podeEntrarEmTurma, podeVerConquistas } from "../../../util/papeis";
import AvatarUsuario from "../../ui/AvatarUsuario";
import ContadorGemas from "../../ui/Gemas/ContadorGemas";
import { useSaldoGemas } from "../../ui/Gemas/useSaldoGemas";

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

    // Sprint 7: o link da ementa só aparece para professor e admin
    const mostrarEmenta = podeVerEmenta(usuario);

    // primeiro nome, para a saudação não ficar longa
    const primeiroNome = usuario?.nome.split(" ")[0];

    // Sprint 9 (F5): contador de gemas, só para aluno com o aceite em dia
    const aluno = podeVerConquistas(usuario);
    const gemas = useSaldoGemas(aluno && !usuario.aceitePendente);

    return (
        <Container>
            <Link to="/"><Img><img src={LogoImg} alt="Página inicial do LogiCup" /></Img></Link>

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
                <MenuItem><NavLink to="/" end><IconeMenu $icone={iconeInicio} />Início</NavLink></MenuItem>
                <MenuItem><NavLink to="/trilha"><IconeMenu $icone={iconeTrilha} />Trilha</NavLink></MenuItem>
                {/* Sprint 9 (ADR-001): laboratório livre de blocos, ao lado da trilha (para todos os logados) */}
                {usuario && <MenuItem><NavLink to="/laboratorio"><IconeMenu $icone={iconeBlocos} />Laboratório</NavLink></MenuItem>}
                <MenuItem><NavLink to="/ranking"><IconeMenu $icone={iconeRanking} />Ranking</NavLink></MenuItem>
                {mostrarEmenta && <MenuItem><NavLink to="/ementa"><IconeMenu $icone={iconeEmenta} />Ementa</NavLink></MenuItem>}
                {/* Sprint 9: turmas — o professor gerencia as dele; o aluno vê a sua */}
                {podeGerenciarTurmas(usuario) && <MenuItem><NavLink to="/professor/turmas"><IconeMenu $icone={iconeTurma} />Turmas</NavLink></MenuItem>}
                {podeEntrarEmTurma(usuario) && <MenuItem><NavLink to="/turma"><IconeMenu $icone={iconeTurma} />Minha turma</NavLink></MenuItem>}
                {/* Sprint 9: o antigo "Recompensas em breve" virou a tela de conquistas (só aluno) */}
                {aluno && <MenuItem><NavLink to="/conquistas"><IconeMenu $icone={iconeConquistas} />Conquistas</NavLink></MenuItem>}
            </Menu>

            <Actions>
                {usuario ? (
                    <>
                        <ContadorGemas gemas={gemas} />
                        {/* Sprint 7: a saudação leva para o perfil. Sprint 9: com o avatar */}
                        <Greeting as={NavLink} to="/perfil" title="Meu perfil">
                            <AvatarUsuario codigo={usuario.avatar} tamanho={34} />
                            <span className="texto-saudacao">Olá, {primeiroNome}</span>
                        </Greeting>
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
