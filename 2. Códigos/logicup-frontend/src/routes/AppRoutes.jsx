import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "../pages/Home";
import Login from "../pages/Login";
import Register from "../pages/Register";
import LearningPath from "../pages/LearningPath";
import Mission from "../pages/Mission";
import PrivateRoute from "./PrivateRoute";
import PublicOnlyRoute from "./PublicOnlyRoute";

const AppRoutes = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<PublicOnlyRoute><Login /></PublicOnlyRoute>} />
                <Route path="/cadastro" element={<PublicOnlyRoute><Register /></PublicOnlyRoute>} />
                <Route path="/trilha" element={<PrivateRoute><LearningPath /></PrivateRoute>} />
                <Route path="/missao/:faseId" element={<PrivateRoute><Mission /></PrivateRoute>} />
            </Routes>
        </BrowserRouter>
    );
};

export default AppRoutes;
