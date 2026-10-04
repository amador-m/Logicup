import Global from "./styles/Global";
import AppRoutes from "./routes/AppRoutes";
import { AuthProvider } from "./context/AuthContext";

function App() {
    return (
        <AuthProvider>
            <Global />
            <AppRoutes />
        </AuthProvider>
    );
}

export default App;
