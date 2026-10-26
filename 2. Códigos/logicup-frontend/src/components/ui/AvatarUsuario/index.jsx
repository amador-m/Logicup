import styled from "styled-components";
import { imagemDoAvatar } from "../../../data/avatares";

/*
 * Sprint 9: o avatar de um usuário, sempre redondo. Usado no Header, perfil, trilha, rankings e turma.
 * tamanho em px. alt vazio por padrão: quase sempre o nome da pessoa já está escrito ao lado
 * (o leitor de tela não precisa ouvir "imagem" antes de cada nome do ranking).
 * loading="lazy": em listas longas (ranking), o navegador só baixa as imagens que aparecem na tela.
 */
const Imagem = styled.img`
  width: ${({ $tamanho }) => $tamanho}px;
  height: ${({ $tamanho }) => $tamanho}px;
  flex: 0 0 auto;
  border-radius: 50%;
  object-fit: cover;
  background: var(--cream);
  border: ${({ $borda }) => $borda}px solid var(--cream);
  display: block;
`;

const AvatarUsuario = ({ codigo, tamanho = 40, borda = 2, alt = "", className }) => (
    <Imagem src={imagemDoAvatar(codigo)} alt={alt} width={tamanho} height={tamanho} loading="lazy"
            $tamanho={tamanho} $borda={borda} className={className} />
);

export default AvatarUsuario;
