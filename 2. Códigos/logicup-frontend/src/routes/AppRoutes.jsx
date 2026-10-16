import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "../pages/Home";
import Login from "../pages/Login";
import Register from "../pages/Register";
import LearningPath from "../pages/LearningPath";
import Mission from "../pages/Mission";
import Ranking from "../pages/Ranking";
import Profile from "../pages/Profile";
import PrivateRoute from "./PrivateRoute";
import PublicOnlyRoute from "./PublicOnlyRoute";
import ModalAuth from "../components/ui/ModalAuth";

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
            </Routes>
        </BrowserRouter>
    );
};

export default AppRoutes;
