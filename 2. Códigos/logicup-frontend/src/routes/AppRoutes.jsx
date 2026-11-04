import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "../pages/Home";
import Login from "../pages/Login";
import Register from "../pages/Register";
import LearningPath from "../pages/LearningPath";
import Mission from "../pages/Mission";
import Ranking from "../pages/Ranking";
import Profile from "../pages/Profile";
import Ementa from "../pages/Teacher";
import PrivateRoute from "./PrivateRoute";
import PublicOnlyRoute from "./PublicOnlyRoute";
import ModalAuth from "../components/ui/ModalAuth";
import Legal from "../pages/Legal";
import ProfessorTurmas from "../pages/Turmas/ProfessorTurmas";
import ProfessorTurma from "../pages/Turmas/ProfessorTurma";
import MinhaTurma from "../pages/Turmas/MinhaTurma";
import AvisoCookies from "../components/layout/AvisoCookies";
import ModalAceite from "../components/ui/ModalAceite";
import Onboarding from "../components/ui/Onboarding";
import Conquistas from "../pages/Conquistas";
import Laboratorio from "../pages/Laboratorio";
import NovaConquista from "../components/ui/NovaConquista";

const AppRoutes = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />
                {/*
                  * Sprint 7: login e cadastro são MODAIS sobre a Home. A URL continua /login e /cadastro
                  * (links, favoritos e o redirecionamento do PrivateRoute seguem funcionando).
                  * O PublicOnlyRoute continua mandando quem já está logado para a trilha.
                  */}
                <Route path="/login" element={
                    <>
                        <Home />
                        <PublicOnlyRoute>
                            <ModalAuth tituloId="titulo-login"><Login /></ModalAuth>
                        </PublicOnlyRoute>
                    </>
                } />
                <Route path="/cadastro" element={
                    <>
                        <Home />
                        <PublicOnlyRoute>
                            <ModalAuth tituloId="titulo-cadastro"><Register /></ModalAuth>
                        </PublicOnlyRoute>
                    </>
                } />
                <Route path="/trilha" element={<PrivateRoute><LearningPath /></PrivateRoute>} />
                <Route path="/missao/:faseId" element={<PrivateRoute><Mission /></PrivateRoute>} />
                <Route path="/ranking" element={<PrivateRoute><Ranking /></PrivateRoute>} />
                <Route path="/perfil" element={<PrivateRoute><Profile /></PrivateRoute>} />
                {/* Sprint 7: ementa do professor (a página avisa o aluno; o backend devolve 403 para ele) */}
                <Route path="/ementa" element={<PrivateRoute><Ementa /></PrivateRoute>} />
                {/* Sprint 9: turmas — o professor gerencia; o aluno vê a dele (a página avisa quem não tem o papel certo) */}
                <Route path="/professor/turmas" element={<PrivateRoute><ProfessorTurmas /></PrivateRoute>} />
                <Route path="/professor/turmas/:id" element={<PrivateRoute><ProfessorTurma /></PrivateRoute>} />
                <Route path="/turma" element={<PrivateRoute><MinhaTurma /></PrivateRoute>} />
                {/* Sprint 9: conquistas e gemas (a página explica se um professor abrir) */}
                <Route path="/conquistas" element={<PrivateRoute><Conquistas /></PrivateRoute>} />
                {/* Sprint 9 (ADR-001): Laboratório de Blocos — livre, sem XP, para qualquer pessoa logada */}
                <Route path="/laboratorio" element={<PrivateRoute><Laboratorio /></PrivateRoute>} />
                {/* Sprint 8: documentos legais, PÚBLICOS (lidos antes do cadastro e por quem tem aceite pendente) */}
                <Route path="/termos" element={<Legal documento="termos" />} />
                <Route path="/privacidade" element={<Legal documento="privacidade" />} />
                <Route path="/cookies" element={<Legal documento="cookies" />} />
            </Routes>
            {/* Sprint 8: fora das <Routes> para aparecer em qualquer página (e dentro do Router, por causa do <Link>) */}
            <AvisoCookies />
            {/* Sprint 8: logado sem o aceite da versão vigente -> janela de aceite (menos nas páginas dos documentos) */}
            <ModalAceite />
            {/* Sprint 9: tutorial com o Gattuccino (uma vez por usuário; depois, pelo botão do perfil) */}
            <Onboarding />
            {/* Sprint 9: comemoração "Nova conquista!" (nunca por cima do aceite ou do tutorial) */}
            <NovaConquista />
        </BrowserRouter>
    );
};

export default AppRoutes;
