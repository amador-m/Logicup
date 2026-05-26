import Global from "./styles/Global";
//import Home from "./pages/Home";
import Register from "./pages/Register/index.jsx";
import Home from "./pages/Home/index.jsx";
import Login from "./pages/Login/index.jsx";

function App() {
  return (
      <>
        <Global />
        <Home />
          <Register />
          <Login />

        {/* Para testar depois: */}
        {/* <Login /> */}
        {/* <Register /> */}
      </>
  );
}

export default App;