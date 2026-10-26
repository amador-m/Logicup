import { useEffect, useState } from "react";
import { avatarApi } from "../../../services/api";
import { ouvirProgresso } from "../../../util/gamificacao";

/*
 * Sprint 9 (F5): saldo de gemas do aluno logado, sempre atualizado.
 * Busca no GET /api/avatares (que já devolve o saldo: nenhuma mudança no backend foi necessária)
 * e busca DE NOVO a cada aviso de progresso (rodada, turma, compra).
 * ativo = false (professor, admin, visitante, aceite pendente) -> não chama a API e devolve null.
 */
export function useSaldoGemas(ativo) {
    const [gemas, setGemas] = useState(null);

    useEffect(() => {
        if (!ativo) return undefined;
        let vivo = true; // evita atualizar o estado depois que o componente saiu da tela
        const buscar = () => avatarApi.catalogo()
            .then((dados) => { if (vivo) setGemas(dados.gemas); })
            .catch(() => {}); // contador é enfeite: se falhar, a página continua funcionando
        buscar();
        const pararDeOuvir = ouvirProgresso(buscar);
        return () => {
            vivo = false;
            pararDeOuvir();
        };
    }, [ativo]);

    return ativo ? gemas : null;
}
