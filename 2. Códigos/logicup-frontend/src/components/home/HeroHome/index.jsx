import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import MascoteImg from "../../../assets/images/MascoteRecortado.png";
import {
    Container,
    Texto,
    Botoes,
    BotaoPrincipal,
    BotaoSecundario,
    Destaques,
    Cena,
    CartaoCodigo,
    Mascote,
    Selo,
} from "./styles";

/*
 * Topo da Home (Sprint 7). Substitui o antigo Banner (que dependia de uma imagem externa).
 * Os botões mudam conforme a pessoa estar logada ou não:
 *   visitante -> "Começar agora" (cadastro) e "Já tenho conta" (login)
 *   logado    -> "Continuar trilha" e "Ver ranking"
 */
const HeroHome = () => {
    const navigate = useNavigate();
    const { usuario } = useAuth();

    return (
        <Container>
            <Texto>
                <span>Plataforma gamificada de Java</span>
                <h1>
                    Aprenda Java <em>avançando por fases</em>, como num jogo.
                </h1>
                <p>
                    Teoria curta, desafios com feedback na hora, XP a cada acerto e um ranking
                    para acompanhar sua evolução. Do primeiro algoritmo até a Programação
                    Orientada a Objetos.
                </p>

                <Botoes>
                    {usuario ? (
                        <>
                            <BotaoPrincipal type="button" onClick={() => navigate("/trilha")}>
                                Continuar trilha →
                            </BotaoPrincipal>
                            <BotaoSecundario type="button" onClick={() => navigate("/ranking")}>
                                Ver ranking
                            </BotaoSecundario>
                        </>
                    ) : (
                        <>
                            <BotaoPrincipal type="button" onClick={() => navigate("/cadastro")}>
                                Começar agora
                            </BotaoPrincipal>
                            <BotaoSecundario type="button" onClick={() => navigate("/login")}>
                                Já tenho conta
                            </BotaoSecundario>
                        </>
                    )}
                </Botoes>

                <Destaques>
                    <li><strong>10 fases</strong>da lógica à POO</li>
                    <li><strong>12 tipos</strong>de desafio</li>
                    <li><strong>XP e ranking</strong>para medir o progresso</li>
                </Destaques>
            </Texto>

            {/* lado direito: só ilustração (escondido de leitores de tela) */}
            <Cena aria-hidden="true">
                <CartaoCodigo>
                    <span className="kw">public class</span> <span className="ty">Aluno</span> {"{\n"}
                    {"  "}<span className="kw">int</span> xp = <span className="ty">0</span>;{"\n\n"}
                    {"  "}<span className="kw">void</span> responder(<span className="kw">boolean</span> acertou) {"{\n"}
                    {"    "}<span className="kw">if</span> (acertou) {"{\n"}
                    {"      "}xp += <span className="ty">25</span>; <span className="cm">// +XP!</span>{"\n"}
                    {"      "}System.out.println(<span className="st">"Mandou bem!"</span>);{"\n"}
                    {"    }\n  }\n}"}
                </CartaoCodigo>
                <Selo className="xp">+25 XP</Selo>
                <Selo className="ofensiva">🔥 3 dias seguidos</Selo>
                <Selo className="fase">🔓 Fase 2 liberada</Selo>
                <Mascote src={MascoteImg} alt="" />
            </Cena>
        </Container>
    );
};

export default HeroHome;
