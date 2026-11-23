import { useState } from "react";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { useAuth } from "../../context/useAuth";
import { ehAdmin } from "../../util/papeis";
import AdminUsuarios from "./AdminUsuarios";
import AdminConteudo from "./AdminConteudo";
import { Page, Hero, Quadro, Abas, Info } from "./styles";

const ABAS = [
    { id: "usuarios", rotulo: "Usuários" },
    { id: "conteudo", rotulo: "Conteúdo" },
];

/*
 * Sprint 11: ÁREA DO ADMINISTRADOR. Cada aba é um componente separado (usuários e conteúdos).
 * A proteção de verdade é o backend (/api/admin/** só para ADMIN); aqui só escondemos a tela de quem não é.
 */
const Admin = () => {
    const { usuario } = useAuth();
    const [aba, setAba] = useState(ABAS[0].id);

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Área do administrador</span>
                        <h1>Administração</h1>
                        <p>Gerencie os usuários e os conteúdos da plataforma.</p>
                    </div>
                </Hero>

                <Quadro aria-label="Administração">
                    {!ehAdmin(usuario) ? (
                        <Info role="alert">Esta área é exclusiva para administradores.</Info>
                    ) : (
                        <>
                            <Abas role="tablist" aria-label="Seções da administração">
                                {ABAS.map((item) => (
                                    <button key={item.id} type="button" role="tab" id={`aba-${item.id}`}
                                            aria-selected={aba === item.id} aria-controls={`painel-${item.id}`}
                                            onClick={() => setAba(item.id)}>
                                        {item.rotulo}
                                    </button>
                                ))}
                            </Abas>
                            <div role="tabpanel" id={`painel-${aba}`} aria-labelledby={`aba-${aba}`}>
                                {aba === "usuarios" && <AdminUsuarios adminId={usuario.id} />}
                                {aba === "conteudo" && <AdminConteudo />}
                            </div>
                        </>
                    )}
                </Quadro>
            </Page>
            <Footer />
        </>
    );
};

export default Admin;
