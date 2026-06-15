import { Fragment } from "react";
import { Link } from "react-router-dom";
import { Container, Copyright, Item } from "./styles";
import LogoImg from "../../../assets/images/LogoDark.png";
import { FaFacebook, FaInstagram, FaWhatsapp } from "react-icons/fa";

const Footer = () => {
    return (
        <Fragment>
            <Container>
                <Item>
                    <Link to="/">
                        <img src={LogoImg} alt="Logo Logicup" />
                    </Link>

                    <p>Continue aprendendo!</p>

                    <nav>
                        <li>
                            <span><FaFacebook /></span>
                        </li>
                        <li>
                            <span><FaInstagram /></span>
                        </li>
                        <li>
                            <span><FaWhatsapp /></span>
                        </li>
                    </nav>
                </Item>

                <Item>
                    <h3>Mapa de fases</h3>
                    <ul>
                        <li>
                            <Link to="/trilha">Trilha de aprendizado</Link>
                        </li>
                        <li>
                            <Link to="/missao/1">Fases desbloqueadas</Link>
                        </li>
                        <li>
                            <span>Fases bloqueadas </span>
                        </li>
                    </ul>
                </Item>

                <Item>
                    <h3>Gotas de saber</h3>
                    <ul>
                        <li>
                            <Link to="/missao/1">Lógica de programação</Link>
                        </li>
                        <li>
                            <span>Variáveis</span>
                        </li>
                        <li>
                            <span>Operadores aritméticos</span>
                        </li>
                    </ul>
                </Item>

                <Item>
                    <h3>Entre em sua conta</h3>
                    <ul>
                        <li>
                            <Link to="/login">Estudante</Link>
                        </li>
                        <li>
                            <Link to="/login">Professor</Link>
                        </li>
                        <li>
                            <Link to="/cadastro">Criar conta</Link>
                        </li>
                    </ul>
                </Item>
            </Container>

            <Copyright>
                <p>©Copyright 2026 - Logicup. Todos os direitos reservados.</p>

                <ul>
                    <li><span>Termos de Uso</span></li>
                    <li><span>Política de Privacidade</span></li>
                    <li><span>Política de Cookies</span></li>
                </ul>
            </Copyright>
        </Fragment>
    );
};

export default Footer;