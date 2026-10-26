import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import { EVENTO_ABRIR_TUTORIAL, definirTutorialAberto, jaViuOnboarding, marcarOnboardingVisto } from "./controle";
import { pedirComemoracao } from "../../../util/gamificacao";
import { FINAL, PASSOS_ALUNO, PASSOS_PROFESSOR } from "./passos";
import { Dialogo, Figuras, Pontos, Botoes, BotaoPrincipal, BotaoTexto, Contador, Espaco } from "./styles";

const PASSOS_POR_PAPEL = { ALUNO: PASSOS_ALUNO, PROFESSOR: PASSOS_PROFESSOR };

// onde o tutorial NÃO abre sozinho: documentos legais e as telas de entrar/cadastrar
const ROTAS_SEM_TUTORIAL = ["/termos", "/privacidade", "/cookies", "/login", "/cadastro"];

/*
 * Sprint 9: onboarding com o Gattuccino (decisões O1 a O3).
 *
 * Decide SE aparece:
 *  - abre sozinho uma vez por usuário, depois do login, quando não há aceite pendente
 *    (a janela de aceite vem antes: o tutorial nunca disputa a tela com ela);
 *  - só para ALUNO e PROFESSOR (cada um com os seus passos);
 *  - o botão "Ver tutorial" do perfil abre de novo (evento EVENTO_ABRIR_TUTORIAL).
 */
const Onboarding = () => {
    const { usuario, carregando } = useAuth();
    const { pathname } = useLocation();
    const [pedidoManual, setPedidoManual] = useState(false);
    const [fechadoNestaVisita, setFechadoNestaVisita] = useState(false);

    useEffect(() => {
        const abrir = () => setPedidoManual(true);
        window.addEventListener(EVENTO_ABRIR_TUTORIAL, abrir);
        return () => window.removeEventListener(EVENTO_ABRIR_TUTORIAL, abrir);
    }, []);

    const passos = PASSOS_POR_PAPEL[usuario?.role];
    if (carregando || !usuario || usuario.aceitePendente || !passos) return null;

    const automatico = !fechadoNestaVisita && !jaViuOnboarding(usuario.id) && !ROTAS_SEM_TUTORIAL.includes(pathname);
    if (!pedidoManual && !automatico) return null;

    const fechar = () => {
        marcarOnboardingVisto(usuario.id);
        setFechadoNestaVisita(true);
        setPedidoManual(false);
    };

    // key: trocar de usuário recomeça do passo 1
    return <JanelaOnboarding key={usuario.id} passos={passos} final={FINAL[usuario.role]} aoFechar={fechar} />;
};

/** A janela em si: passo atual, voltar/avançar, pular. */
const JanelaOnboarding = ({ passos, final, aoFechar }) => {
    const dialogoRef = useRef(null);
    const tituloRef = useRef(null);
    const navigate = useNavigate();
    const [indice, setIndice] = useState(0);

    const passo = passos[indice];
    const ultimo = indice === passos.length - 1;

    useEffect(() => {
        const dialogo = dialogoRef.current;
        if (dialogo && !dialogo.open) dialogo.showModal();
        const overflowAnterior = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        definirTutorialAberto(true);
        return () => {
            document.body.style.overflow = overflowAnterior;
            if (dialogo?.open) dialogo.close();
            definirTutorialAberto(false);
            pedirComemoracao(); // Sprint 9: se houver conquista esperando, ela aparece agora
        };
    }, []);

    // a cada passo, o foco vai para o título: o leitor de tela lê o passo novo, e Tab continua de cima
    useEffect(() => {
        tituloRef.current?.focus();
    }, [indice]);

    const concluir = () => {
        aoFechar();
        navigate(final.destino);
    };

    return (
        <Dialogo
            ref={dialogoRef}
            aria-labelledby="titulo-onboarding"
            aria-describedby="texto-onboarding"
            onCancel={(evento) => { evento.preventDefault(); aoFechar(); }} // Esc = pular
        >
            <Contador>Passo {indice + 1} de {passos.length}</Contador>

            {/* key: a animação de entrada roda a cada passo */}
            <Figuras key={indice} aria-hidden="true">
                {passo.imagens.map((imagem) => <img key={imagem} src={imagem} alt="" />)}
            </Figuras>

            <h2 id="titulo-onboarding" ref={tituloRef} tabIndex={-1}>{passo.titulo}</h2>
            <p id="texto-onboarding">{passo.texto}</p>
            <Espaco />

            <Pontos aria-hidden="true">
                {passos.map((item, i) => <span key={item.titulo} className={i === indice ? "atual" : ""} />)}
            </Pontos>

            {/*
              * "Pular" (no último passo) e "Voltar" (no primeiro) não somem do layout: ficam invisíveis
              * (classe "oculto") e fora do Tab. Assim os pontos e os botões não mudam de lugar entre os passos.
              */}
            <Botoes>
                <BotaoTexto type="button" onClick={aoFechar} className={ultimo ? "oculto" : ""}
                            tabIndex={ultimo ? -1 : undefined} aria-hidden={ultimo || undefined}>
                    Pular
                </BotaoTexto>
                <div className="navegar">
                    <BotaoTexto type="button" onClick={() => setIndice((i) => i - 1)} className={indice === 0 ? "oculto" : ""}
                                tabIndex={indice === 0 ? -1 : undefined} aria-hidden={indice === 0 || undefined}>
                        Voltar
                    </BotaoTexto>
                    {ultimo
                        ? <BotaoPrincipal type="button" onClick={concluir}>{final.texto}</BotaoPrincipal>
                        : <BotaoPrincipal type="button" onClick={() => setIndice((i) => i + 1)}>Próximo</BotaoPrincipal>}
                </div>
            </Botoes>
        </Dialogo>
    );
};

export default Onboarding;
