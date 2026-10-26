import { Linha } from "../../../pages/Ranking/styles";
import AvatarUsuario from "../AvatarUsuario";

const MEDALHAS = { 1: "🥇", 2: "🥈", 3: "🥉" };

/*
 * Uma linha do ranking. Empates aparecem com a mesma posição (a regra é do backend).
 * Sprint 9: saiu da página do Ranking para ser usada também no ranking da turma (mesma aparência).
 */
const LinhaRanking = ({ item }) => {
    const medalha = MEDALHAS[item.posicao];
    return (
        <Linha className={`${item.posicao === 1 ? "podio-1" : ""} ${item.voce ? "voce" : ""}`}
               aria-current={item.voce ? "true" : undefined}>
            <span className="posicao" aria-label={`${item.posicao}º lugar`}>
                {medalha ?? `${item.posicao}º`}
            </span>
            {/* Sprint 9: o avatar escolhido (antes: as 2 primeiras letras do username) */}
            <AvatarUsuario className="avatar" codigo={item.avatar} tamanho={46} />
            <span className="nome">
                {item.username}
                {item.voce && <small>você</small>}
            </span>
            <span className="nivel">Fase {item.nivel}</span>
            <span className="xp">{item.xp} XP</span>
        </Linha>
    );
};

export default LinhaRanking;
