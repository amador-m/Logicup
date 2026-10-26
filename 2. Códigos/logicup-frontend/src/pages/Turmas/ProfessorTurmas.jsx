import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { useAuth } from "../../context/useAuth";
import { turmaApi } from "../../services/api";
import { podeGerenciarTurmas } from "../../util/papeis";
import EstadoVazio from "../../components/ui/EstadoVazio";
import { plural } from "./formatos";
import {
    Page, Hero, Quadro, Aviso, BotaoAcao, Secao, Formulario, ErroCampo, ListaTurmas, CartaoTurma,
} from "./styles";

/*
 * Sprint 9: "Minhas turmas" do professor — criar turma e ver a lista.
 * Ao criar, vai direto para a turma nova (onde está o código para compartilhar).
 */
const ProfessorTurmas = () => {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const podeVer = podeGerenciarTurmas(usuario);

    const [turmas, setTurmas] = useState(null);
    const [erro, setErro] = useState(null);
    const [tentativa, setTentativa] = useState(0);

    const [nome, setNome] = useState("");
    const [erroNome, setErroNome] = useState("");
    const [criando, setCriando] = useState(false);

    useEffect(() => {
        if (!podeVer) return undefined;
        let ativo = true;
        turmaApi.listar()
            .then((dados) => ativo && setTurmas(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [podeVer, tentativa]);

    const criar = async (evento) => {
        evento.preventDefault();
        setErroNome("");
        setCriando(true);
        try {
            const turma = await turmaApi.criar(nome);
            navigate(`/professor/turmas/${turma.id}`);
        } catch (e) {
            setErroNome(e.campos?.nome ?? e.message);
            setCriando(false);
        }
    };

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Área do professor</span>
                        <h1>Minhas turmas</h1>
                        <p>
                            Crie uma turma e compartilhe o código de convite com os alunos. Quem entrar com o código
                            aparece aqui, com o progresso na trilha.
                        </p>
                    </div>
                </Hero>

                <Quadro aria-label="Turmas">
                    {!podeVer && (
                        <Aviso>
                            <strong>Esta área é exclusiva para professores.</strong>
                            <div><BotaoAcao type="button" onClick={() => navigate("/trilha")}>Ir para a trilha</BotaoAcao></div>
                        </Aviso>
                    )}

                    {podeVer && (
                        <Secao>
                            <h2>Nova turma</h2>
                            <Formulario onSubmit={criar} noValidate>
                                <label htmlFor="nova-turma-nome">
                                    Nome da turma
                                    <input id="nova-turma-nome" value={nome} onChange={(e) => setNome(e.target.value)}
                                           placeholder="Ex.: Lógica de Programação — Noite" maxLength={100}
                                           aria-invalid={erroNome ? true : undefined}
                                           aria-describedby={erroNome ? "nova-turma-erro" : undefined} />
                                </label>
                                <BotaoAcao type="submit" disabled={criando}>{criando ? "Criando..." : "Criar turma"}</BotaoAcao>
                            </Formulario>
                            {erroNome && <ErroCampo id="nova-turma-erro" role="alert">{erroNome}</ErroCampo>}
                        </Secao>
                    )}

                    {podeVer && erro && (
                        <Aviso role="alert" className="error">
                            <p>{erro}</p>
                            <BotaoAcao type="button" onClick={() => { setErro(null); setTentativa((n) => n + 1); }}>
                                Tentar novamente
                            </BotaoAcao>
                        </Aviso>
                    )}

                    {podeVer && !erro && !turmas && <Aviso role="status">Carregando turmas...</Aviso>}

                    {turmas && turmas.length === 0 && (
                        <Secao>
                            {/* Sprint 9 (E2): estado vazio com o Gattuccino dormindo */}
                            <EstadoVazio titulo="Você ainda não tem turmas.">
                                Crie a primeira acima. Cada turma ganha um código de 8 letras e números para os alunos entrarem.
                            </EstadoVazio>
                        </Secao>
                    )}

                    {turmas && turmas.length > 0 && (
                        <Secao>
                            <h2>Suas turmas</h2>
                            <ListaTurmas>
                                {turmas.map((turma) => (
                                    <CartaoTurma key={turma.id}>
                                        <h3>{turma.nome}</h3>
                                        <span className="info">
                                            {plural(turma.alunosAtivos, "aluno", "alunos")} • código {turma.codigo}
                                        </span>
                                        <Link to={`/professor/turmas/${turma.id}`}>Abrir turma</Link>
                                    </CartaoTurma>
                                ))}
                            </ListaTurmas>
                        </Secao>
                    )}
                </Quadro>
            </Page>
            <Footer />
        </>
    );
};

export default ProfessorTurmas;
