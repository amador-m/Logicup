import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { useAuth } from "../../context/useAuth";
import { perfilApi, rankingApi, trilhaApi } from "../../services/api";
import { STATUS_FASE, coresDoIcone, montarMapa } from "../../util/trilha";
import { iconeFase } from "../../assets/fases";
import { abrirTutorial } from "../../components/ui/Onboarding/controle";
import AvatarUsuario from "../../components/ui/AvatarUsuario";
import LojaAvatares from "../../components/ui/LojaAvatares";
import Confirmacao from "../../components/ui/Confirmacao";
import { useSaldoGemas } from "../../components/ui/Gemas/useSaldoGemas";
import { podeVerConquistas } from "../../util/papeis";
import { InputGroup, Button, ErrorMessage, FieldError } from "../Login/styles";
import {
    Page, Hero, Cartao, MolduraAvatar, Stats, StatCard, Paineis, Painel, Form, Sucesso,
    SecaoFases, ListaFases, LinhaFase, Barra, BotaoTutorial, BotaoTrocarAvatar, ZonaPerigo, BotaoExcluir,
} from "./styles";

const SENHAS_VAZIAS = { senhaAtual: "", novaSenha: "", confirmacao: "" };

/* mesma ajuda do cadastro: minúsculas e sem espaços enquanto digita (quem GARANTE a regra é o backend) */
const ajustarUsername = (valor) => valor.toLowerCase().replace(/\s/g, "");

const NOME_PAPEL = { ALUNO: "Aluno", PROFESSOR: "Professor", ADMIN: "Administrador" };

/*
 * Resumo do aluno (XP, fase, ofensiva, ranking). Vem de /trilha e /ranking, que já existem.
 * Professor/admin não têm esses números (aluno = null): o resumo simplesmente não aparece.
 * Se uma das chamadas falhar, o perfil continua funcionando (só o resumo fica sem aquele número).
 */
function useResumo() {
    const [resumo, setResumo] = useState(null);

    useEffect(() => {
        let ativo = true;
        Promise.allSettled([trilhaApi.trilha(), rankingApi.ranking()]).then(([trilha, ranking]) => {
            if (!ativo) return;
            const aluno = trilha.status === "fulfilled" ? trilha.value.aluno : null;
            const posicao = ranking.status === "fulfilled" ? ranking.value.voce?.posicao ?? null : null;
            // Sprint 7: as 10 fases (catálogo + API), a MESMA montagem usada no mapa da trilha
            const fases = aluno ? montarMapa(trilha.value.fases) : [];
            setResumo(aluno ? { ...aluno, posicao, fases } : null);
        });
        return () => {
            ativo = false;
        };
    }, []);

    return resumo;
}

/** Texto do selo de cada fase. "Em andamento" = disponível e com pelo menos uma rodada aprovada. */
function seloDaFase(fase) {
    switch (fase.status) {
        case STATUS_FASE.CONCLUIDA: return { texto: "Concluída", classe: "concluida" };
        case STATUS_FASE.DISPONIVEL:
            return fase.rodadasAprovadas > 0
                ? { texto: "Em andamento", classe: "andamento" }
                : { texto: "Disponível", classe: "disponivel" };
        case STATUS_FASE.BLOQUEADA: return { texto: "🔒 Bloqueada", classe: "bloqueada" };
        default: return { texto: "Em breve", classe: "em-breve" };
    }
}

/*
 * Sprint 7: progresso por fase. A barra mede RODADAS aprovadas (a regra de avanço da trilha);
 * o XP aparece ao lado, como embaixo dos nós do mapa. Fase "Em breve" (ainda não existe no banco) não tem barra.
 */
const ProgressoFases = ({ fases }) => (
    <SecaoFases aria-labelledby="titulo-progresso">
        <h2 id="titulo-progresso">Progresso por fase</h2>
        <ListaFases>
            {fases.map((fase) => {
                const selo = seloDaFase(fase);
                const percentual = fase.existe && fase.totalRodadas > 0
                    ? Math.round((fase.rodadasAprovadas / fase.totalRodadas) * 100)
                    : 0;
                return (
                    <LinhaFase key={fase.ordem} className={fase.status}>
                        <img src={iconeFase(fase.icone, coresDoIcone(fase).normal)} alt="" aria-hidden="true" />
                        <div className="info">
                            <div className="topo">
                                <span className="numero">Fase {fase.ordem}</span>
                                <span className={`selo ${selo.classe}`}>{selo.texto}</span>
                            </div>
                            <h3>{fase.titulo}</h3>
                            {fase.existe && (
                                <>
                                    <Barra
                                        role="progressbar"
                                        aria-label={`Rodadas aprovadas na fase ${fase.ordem}`}
                                        aria-valuemin={0}
                                        aria-valuemax={fase.totalRodadas}
                                        aria-valuenow={fase.rodadasAprovadas}
                                    >
                                        <span style={{ width: `${percentual}%` }} />
                                    </Barra>
                                    <span className="numeros">
                                        {fase.rodadasAprovadas}/{fase.totalRodadas} rodadas aprovadas{" "}
                                        <span className="xp">• {fase.xpGanho}/{fase.xpTotal} XP</span>
                                    </span>
                                </>
                            )}
                        </div>
                    </LinhaFase>
                );
            })}
        </ListaFases>
    </SecaoFases>
);

/** Formulário de nome + username. O e-mail aparece, mas não é editável. */
const DadosConta = ({ usuario, aoSalvar }) => {
    const [dados, setDados] = useState({ nome: usuario.nome, username: usuario.username });
    const [errosCampos, setErrosCampos] = useState({});
    const [erro, setErro] = useState("");
    const [sucesso, setSucesso] = useState("");
    const [enviando, setEnviando] = useState(false);

    const semMudanca = dados.nome.trim() === usuario.nome && dados.username === usuario.username;

    const handleChange = (evento) => {
        const { name, value } = evento.target;
        setSucesso("");
        setDados((anteriores) => ({ ...anteriores, [name]: name === "username" ? ajustarUsername(value) : value }));
    };

    const handleSubmit = async (evento) => {
        evento.preventDefault();
        setErro("");
        setErrosCampos({});
        setSucesso("");
        setEnviando(true);
        try {
            const atualizado = await perfilApi.atualizar(dados);
            aoSalvar(atualizado);          // atualiza o AuthContext -> Header muda na hora
            setDados({ nome: atualizado.nome, username: atualizado.username });
            setSucesso("Dados atualizados!");
        } catch (error) {
            const campos = error.campos ?? {};
            setErrosCampos(campos);
            setErro(Object.keys(campos).length > 0 ? "" : error.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <Painel aria-labelledby="titulo-dados">
            <h2 id="titulo-dados">Dados da conta</h2>
            <Form onSubmit={handleSubmit}>
                <InputGroup>
                    <label htmlFor="perfil-nome">Nome</label>
                    <input id="perfil-nome" name="nome" type="text" autoComplete="name"
                           required minLength={2} maxLength={150}
                           value={dados.nome} onChange={handleChange} />
                    {errosCampos.nome && <FieldError>{errosCampos.nome}</FieldError>}
                </InputGroup>

                <InputGroup>
                    <label htmlFor="perfil-username">Nome de usuário</label>
                    <input id="perfil-username" name="username" type="text" autoComplete="username"
                           autoCapitalize="none" spellCheck={false}
                           required minLength={3} maxLength={20} pattern="[a-z0-9._]{3,20}"
                           aria-describedby="perfil-username-regra"
                           value={dados.username} onChange={handleChange} />
                    {errosCampos.username ? (
                        <FieldError>{errosCampos.username}</FieldError>
                    ) : (
                        <small id="perfil-username-regra">
                            Aparece no ranking e serve para entrar. 3 a 20 caracteres: letras minúsculas, números, ponto ou sublinhado.
                        </small>
                    )}
                </InputGroup>

                <InputGroup>
                    <label htmlFor="perfil-email">E-mail</label>
                    <input id="perfil-email" type="email" value={usuario.email} disabled
                           aria-describedby="perfil-email-ajuda" />
                    <small id="perfil-email-ajuda">O e-mail não pode ser alterado.</small>
                </InputGroup>

                {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}
                {sucesso && <Sucesso role="status">✓ {sucesso}</Sucesso>}

                <Button type="submit" disabled={enviando || semMudanca}>
                    {enviando ? "Salvando..." : "Salvar alterações"}
                </Button>
            </Form>
        </Painel>
    );
};

/** Troca de senha: exige a senha atual. A confirmação é conferida só aqui no navegador. */
const AlterarSenha = () => {
    const [senhas, setSenhas] = useState(SENHAS_VAZIAS);
    const [errosCampos, setErrosCampos] = useState({});
    const [erro, setErro] = useState("");
    const [sucesso, setSucesso] = useState("");
    const [enviando, setEnviando] = useState(false);

    const handleChange = (evento) => {
        const { name, value } = evento.target;
        setSucesso("");
        setSenhas((anteriores) => ({ ...anteriores, [name]: value }));
    };

    const handleSubmit = async (evento) => {
        evento.preventDefault();
        setErro("");
        setSucesso("");

        // a confirmação não vai para o backend: serve só para evitar erro de digitação
        if (senhas.novaSenha !== senhas.confirmacao) {
            setErrosCampos({ confirmacao: "As senhas não conferem." });
            return;
        }
        setErrosCampos({});
        setEnviando(true);
        try {
            await perfilApi.alterarSenha({ senhaAtual: senhas.senhaAtual, novaSenha: senhas.novaSenha });
            setSenhas(SENHAS_VAZIAS);
            setSucesso("Senha alterada! Use a nova senha no próximo login.");
        } catch (error) {
            const campos = error.campos ?? {};
            setErrosCampos(campos);
            setErro(Object.keys(campos).length > 0 ? "" : error.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <Painel aria-labelledby="titulo-senha">
            <h2 id="titulo-senha">Alterar senha</h2>
            <Form onSubmit={handleSubmit}>
                <InputGroup>
                    <label htmlFor="perfil-senha-atual">Senha atual</label>
                    <input id="perfil-senha-atual" name="senhaAtual" type="password" autoComplete="current-password"
                           required value={senhas.senhaAtual} onChange={handleChange} />
                    {errosCampos.senhaAtual && <FieldError>{errosCampos.senhaAtual}</FieldError>}
                </InputGroup>

                <InputGroup>
                    <label htmlFor="perfil-senha-nova">Nova senha</label>
                    <input id="perfil-senha-nova" name="novaSenha" type="password" autoComplete="new-password"
                           placeholder="Mínimo 8 caracteres" required minLength={8} maxLength={72}
                           value={senhas.novaSenha} onChange={handleChange} />
                    {errosCampos.novaSenha && <FieldError>{errosCampos.novaSenha}</FieldError>}
                </InputGroup>

                <InputGroup>
                    <label htmlFor="perfil-senha-confirmacao">Confirmar nova senha</label>
                    <input id="perfil-senha-confirmacao" name="confirmacao" type="password" autoComplete="new-password"
                           required minLength={8} maxLength={72}
                           value={senhas.confirmacao} onChange={handleChange} />
                    {errosCampos.confirmacao && <FieldError>{errosCampos.confirmacao}</FieldError>}
                </InputGroup>

                {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}
                {sucesso && <Sucesso role="status">✓ {sucesso}</Sucesso>}

                <Button type="submit" disabled={enviando}>
                    {enviando ? "Alterando..." : "Alterar senha"}
                </Button>
            </Form>
        </Painel>
    );
};

/*
 * Sprint 10 (LGPD): excluir a própria conta.
 *  - quem tem senha: janela de confirmação pedindo a senha -> exclui na hora;
 *  - quem entra só pelo Google (temSenha = false): manda um link para o e-mail (vale 30 minutos);
 *  - administrador: não exclui por aqui (decisão da equipe). Professor com turmas: o backend recusa e
 *    explica (a regra mora no backend; aqui só mostramos a mensagem).
 */
const ExcluirConta = ({ usuario }) => {
    const navigate = useNavigate();
    const [confirmando, setConfirmando] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState("");
    const [linkEnviado, setLinkEnviado] = useState(false);

    if (usuario.role === "ADMIN") {
        return (
            <ZonaPerigo aria-labelledby="titulo-excluir">
                <h2 id="titulo-excluir">Excluir conta</h2>
                <p>Contas de administrador não podem ser excluídas pelo perfil.</p>
            </ZonaPerigo>
        );
    }

    const excluirComSenha = async (senha) => {
        try {
            await perfilApi.excluirConta(senha);
        } catch (error) {
            // senha errada vem em campos.senhaAtual; o resto (ex.: professor com turmas) vem em message
            throw new Error(error.campos?.senhaAtual ?? error.message, { cause: error });
        }
        /*
         * Quem "esquece" a pessoa no AuthContext é a página /excluir-conta, DEPOIS de aparecer.
         * Se fosse aqui, o PrivateRoute do /perfil veria "ninguém logado" antes da troca de página
         * (o React Router faz a navegação como uma atualização de prioridade mais baixa) e mandaria
         * para o /login. Encontrado no teste de ponta a ponta.
         */
        navigate("/excluir-conta", { replace: true, state: { excluida: true } });
    };

    const pedirLink = async () => {
        setErro("");
        setEnviando(true);
        try {
            await perfilApi.pedirLinkDeExclusao();
            setLinkEnviado(true);
        } catch (error) {
            setErro(error.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <ZonaPerigo aria-labelledby="titulo-excluir">
            <h2 id="titulo-excluir">Excluir conta</h2>
            <p>
                Ao excluir a sua conta, apagamos <strong>tudo</strong>: dados, progresso, XP, gemas, conquistas,
                avatares e participação em turmas. Isso não pode ser desfeito.
            </p>
            {usuario.role === "PROFESSOR" && (
                <p>Se você tem turmas, exclua as turmas antes, para nenhum aluno ficar sem professor.</p>
            )}

            {usuario.temSenha ? (
                <BotaoExcluir type="button" onClick={() => setConfirmando(true)}>Excluir minha conta</BotaoExcluir>
            ) : linkEnviado ? (
                <Sucesso role="status">
                    ✓ Enviamos um link para {usuario.email}. Abra o e-mail e confirme em até 30 minutos.
                </Sucesso>
            ) : (
                <>
                    <p>Como você entra pelo Google, a confirmação é feita por um link enviado ao seu e-mail.</p>
                    <BotaoExcluir type="button" onClick={pedirLink} disabled={enviando}>
                        {enviando ? "Enviando..." : "Enviar link de exclusão"}
                    </BotaoExcluir>
                </>
            )}
            {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}

            {confirmando && (
                <Confirmacao
                    titulo="Excluir sua conta?"
                    mensagem="Todo o seu progresso será apagado para sempre. Não dá para desfazer."
                    textoConfirmar="Excluir para sempre"
                    pedirSenha
                    aoConfirmar={excluirComSenha}
                    aoCancelar={() => setConfirmando(false)}
                />
            )}
        </ZonaPerigo>
    );
};

/*
 * Sprint 7: perfil de quem está logado.
 * Mostra os dados (de /auth/me, já guardados no AuthContext) e o resumo do aluno; permite editar
 * nome, username e senha. Sprint 9: o avatar escolhido e a loja de avatares (decisão F3).
 */
const Profile = () => {
    const { usuario, atualizarUsuario } = useAuth();
    const resumo = useResumo();
    const aluno = podeVerConquistas(usuario);
    const gemas = useSaldoGemas(aluno);
    const [lojaAberta, setLojaAberta] = useState(false);

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Meu perfil</span>
                        <h1>{usuario.nome}</h1>
                        <p>@{usuario.username} • {NOME_PAPEL[usuario.role] ?? usuario.role}</p>
                    </div>

                    <Cartao>
                        <MolduraAvatar>
                            <AvatarUsuario codigo={usuario.avatar} tamanho={132} borda={7} alt="Seu avatar" />
                        </MolduraAvatar>
                        <BotaoTrocarAvatar type="button" onClick={() => setLojaAberta(true)}>
                            {aluno ? "Trocar avatar / loja" : "Trocar avatar"}
                        </BotaoTrocarAvatar>
                        {/* Sprint 9: rever o onboarding (decisão O2) */}
                        <BotaoTutorial type="button" onClick={abrirTutorial}>Ver tutorial</BotaoTutorial>
                    </Cartao>
                </Hero>

                {resumo && (
                    <Stats aria-label="Seu progresso">
                        <StatCard><strong>{resumo.xp}</strong><span>XP</span></StatCard>
                        <StatCard><strong>{resumo.nivel}</strong><span>Fase atual</span></StatCard>
                        <StatCard><strong>{resumo.streak}</strong><span>Ofensiva</span></StatCard>
                        {gemas != null && <StatCard><strong>{gemas}</strong><span>Gemas</span></StatCard>}
                        <StatCard>
                            <strong>{resumo.posicao ? `${resumo.posicao}º` : "—"}</strong>
                            <span>{resumo.posicao ? "No ranking" : "Ainda sem ranking"}</span>
                        </StatCard>
                    </Stats>
                )}

                {resumo && <ProgressoFases fases={resumo.fases} />}

                <Paineis>
                    {/* key: se o usuário mudar (outro login), o formulário recomeça com os dados novos */}
                    <DadosConta key={usuario.id} usuario={usuario} aoSalvar={atualizarUsuario} />
                    <AlterarSenha />
                </Paineis>

                <ExcluirConta usuario={usuario} />
            </Page>
            <Footer />

            {lojaAberta && <LojaAvatares aoFechar={() => setLojaAberta(false)} />}
        </>
    );
};

export default Profile;
