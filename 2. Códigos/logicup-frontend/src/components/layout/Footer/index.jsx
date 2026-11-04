import { Link } from "react-router-dom";
import { FaInstagram } from "react-icons/fa6";
import { FiMail } from "react-icons/fi";
import { Container, Contatos, Copyright, Item, LinksLegais } from "./styles";
import LogoImg from "../../../assets/images/LogoLight.png";

/*
 * Rodapé (Sprint 7): fundo escuro para "fechar" a página e SÓ links que levam a algum lugar.
 * Saíram os itens decorativos (redes sociais e tópicos sem página).
 * Sprint 8: os links legais voltaram, agora que as páginas de Termos, Privacidade e Cookies existem.
 * Deploy (04/11): contatos oficiais. O endereço de e-mail fica escrito por extenso (não só no link), porque
 * nem todo mundo tem um programa de e-mail configurado para abrir o "mailto:".
 */
const INSTAGRAM = "https://www.instagram.com/logicup.oficial/";
const EMAIL_CONTATO = "contato@logicup.dev.br";

const Footer = () => (
    <>
        <Container>
            <Item>
                <Link to="/">
                    <img src={LogoImg} alt="Página inicial do LogiCup" />
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
                <Contatos aria-label="Contatos do LogiCup">
                    <li>
                        <a href={INSTAGRAM} target="_blank" rel="noopener noreferrer">
                            <FaInstagram aria-hidden="true" /> @logicup.oficial
                        </a>
                    </li>
                    <li>
                        <a href={`mailto:${EMAIL_CONTATO}`}>
                            <FiMail aria-hidden="true" /> {EMAIL_CONTATO}
                        </a>
                    </li>
                </Contatos>
            </Item>
        </Container>

        <Copyright>
            <p>
                © 2026 LogiCup. Todos os direitos reservados.
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
