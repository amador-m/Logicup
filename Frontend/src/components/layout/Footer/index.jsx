import {Fragment} from 'react';
import {Container, Copyright, Item} from "./styles";
import LogoImg from "../../../assets/images/LogoDark.png";
import {FaFacebook, FaInstagram, FaWhatsapp} from "react-icons/fa";

const Footer = () => {
    return (
        <Fragment>
            <Container>
                <Item>
                    <img src={LogoImg} alt="" />
                    <p>Continue aprendendo conosco!</p>
                    <nav>
                        <li><span><FaFacebook /></span></li>
                        <li><span><FaInstagram /></span></li>
                        <li><span><FaWhatsapp /></span></li>
                    </nav>
                </Item>
                <Item>
                    <h3>Mapa de fases</h3>
                    <ul>
                        <li><span>Nível 1</span></li>
                        <li><span>Nível 2</span></li>
                        <li><span>Nível 3</span></li>
                    </ul>
                </Item>
                <Item>
                    <h3>Gotas de saber</h3>
                    <ul>
                        <li><span>Variáveis</span></li>
                        <li><span>Lígica de programação</span></li>
                        <li><span>Operadores aritméticos</span></li>
                    </ul>
                </Item>
                <Item>
                    <h3>Entre em sua conta</h3>
                    <ul>
                        <li><span>Estudante</span></li>
                        <li><span>Professor</span></li>
                        <li><span>Administrador</span></li>
                    </ul>
                </Item>
            </Container>
            <Copyright>
                <p>©Copyright 2022 - Workside Tecnologia All Rights Reserved.</p>
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