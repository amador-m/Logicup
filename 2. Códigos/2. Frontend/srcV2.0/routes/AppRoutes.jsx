import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "../pages/Home";
import Login from "../pages/Login";
import Register from "../pages/Register";
import LearningPath from "../pages/LearningPath";
import Mission from "../pages/Mission";

const AppRoutes = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/cadastro" element={<Register />} />
                <Route path="/trilha" element={<LearningPath />} />
                <Route path="/missao/1" element={<Mission />} />
            </Routes>
        </BrowserRouter>
    );
};

export default AppRoutes;