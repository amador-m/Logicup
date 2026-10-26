import { Link } from "react-router-dom";
import { Container, Copyright, Item, LinksLegais } from "./styles";
import LogoImg from "../../../assets/images/LogoLight.png";

/*
 * Rodapé (Sprint 7): fundo escuro para "fechar" a página e SÓ links que levam a algum lugar.
 * Saíram os itens decorativos (redes sociais e tópicos sem página).
 * Sprint 8: os links legais voltaram, agora que as páginas de Termos, Privacidade e Cookies existem.
 */
const Footer = () => (
    <>
        <Container>
            <Item>
                <Link to="/">
                    <img src={LogoImg} alt="Página inicial do LogicUp" />
                </Link>
                <p>Aprenda Java avançando por fases, como num jogo.</p>
            </Item>

            <Item>
                <h3>A plataforma</h3>
                <ul>
                    <li><Link to="/">Início</Link></li>
                    <li><Link to="/trilha">Trilha de aprendizado</Link></li>
                    <li><Link to="/ranking">Ranking</Link></li>
                </ul>
            </Item>

            <Item>
                <h3>Sua conta</h3>
                <ul>
                    <li><Link to="/login">Entrar</Link></li>
                    <li><Link to="/cadastro">Criar conta</Link></li>
                </ul>
            </Item>

            <Item>
                <h3>Sobre o projeto</h3>
                <p>
                    Trabalho de Conclusão de Curso: uma plataforma gamificada para o ensino
                    de programação em Java.
                </p>
            </Item>
        </Container>

        <Copyright>
            <p>
                © 2026 LogicUp. Todos os direitos reservados.
                {/* Sprint 9 (E3): crédito exigido pela licença gratuita do pacote de ícones */}
                {" "}Ícones: <a href="https://www.flaticon.com/uicons" target="_blank" rel="noopener noreferrer">UIcons por Flaticon</a>.
            </p>
            <LinksLegais aria-label="Documentos legais">
                <Link to="/termos">Termos de Uso</Link>
                <Link to="/privacidade">Política de Privacidade</Link>
                <Link to="/cookies">Política de Cookies</Link>
            </LinksLegais>
        </Copyright>
    </>
);

export default Footer;
