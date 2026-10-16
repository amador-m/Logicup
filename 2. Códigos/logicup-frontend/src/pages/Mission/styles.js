import styled, { keyframes } from "styled-components";

export const Card = styled.article`
  scroll-margin-top: 16px;
  max-width: 1000px;
  margin: 0 auto;
  background: var(--cream);
  border: 4px solid var(--yellow);
  border-radius: 34px;
  padding: 34px;
  box-shadow: 0 18px 45px rgba(4, 4, 35, 0.28);

  /* Sprint 7 (responsividade): menos respiro interno no celular */
  @media (max-width: 900px) {
    padding: 22px 18px;
    border-radius: 26px;
  }
`;

export const CardHeader = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 22px;
  margin-bottom: 28px;

  span {
    color: var(--orange);
    font-weight: 900;
    text-transform: uppercase;
    font-size: 13px;
  }

  h2 {
    font-size: 32px;
    color: var(--dark);
    margin-top: 6px;
  }

  > div:last-child {
    display: flex;
    gap: 12px;
  }

  /* celular: "Rever teoria" desce para baixo do título quando não cabe ao lado */
  @media (max-width: 900px) {
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      overflow-wrap: anywhere;
    }
  }
`;

export const RoundTabs = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
`;

export const RoundTab = styled.button`
  background: var(--cream);
  color: var(--dark);
  border: 2px solid var(--yellow);
  padding: 11px 18px;
  border-radius: 999px;
  font-weight: 800;
  cursor: pointer;

  &.active {
    background: var(--yellow);
  }

  &:disabled {
    background: rgba(241, 240, 226, 0.25);
    color: rgba(241, 240, 226, 0.6);
    border-color: rgba(241, 240, 226, 0.25);
    cursor: not-allowed;
  }
    &:hover:not(:disabled) {
        background-color: var(--green);
        color: var(--white);
    }
`;

export const TheoryContent = styled.div`
  h3 {
    color: var(--green);
    margin: 20px 0 10px;
  }

  p,
  li {
    color: var(--brown-dark);
    line-height: 1.8;
  }
`;

export const OptionButton = styled.button`
  width: 100%;
  background: var(--cream);
  color: var(--dark);
  border: 2px solid rgba(40, 128, 120, 0.35);
  padding: 13px 16px;
  border-radius: 16px;
  font-weight: 700;
  text-align: left;
  margin-top: 9px;
  transition: 0.3s;
    cursor: pointer;

    &:hover {
        background-color: var(--green);
        color: var(--white);
    }

  &.selected {
    background: var(--yellow);
    border-color: var(--orange);
  }

  /* Sprint 7: alternativa com símbolo de fluxograma ao lado do texto */
  &.com-icone {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.7;

    &:hover {
      background-color: var(--cream);
      color: var(--dark);
    }
  }
`;

export const FeedbackBox = styled.div`
  padding: 18px;
  border-radius: 18px;
  font-weight: 800;
  margin-top: 24px;

  &.success {
    background: rgba(40, 128, 120, 0.16);
    color: var(--green);
    border: 2px solid var(--green);
  }

  &.error {
    background: rgba(227, 92, 51, 0.14);
    color: var(--orange);
    border: 2px solid var(--orange);
  }
`;

export const CorrectionBox = styled.div`
  margin-top: 12px;
  background: rgba(40, 128, 120, 0.12);
  border-left: 6px solid var(--green);
  color: var(--brown-dark);
  padding: 14px;
  border-radius: 14px;
  line-height: 1.5;
    cursor: pointer;

  strong {
    color: var(--green);
  }
`;

export const Page = styled.main`
  min-height: calc(100vh - 82px);
  background:
    radial-gradient(circle at 15% 12%, rgba(236, 172, 12, 0.22), transparent 24%),
    radial-gradient(circle at 90% 20%, rgba(227, 92, 51, 0.2), transparent 24%),
    linear-gradient(180deg, var(--dark), var(--green));
  padding: 50px 120px 70px;

  /* Sprint 7 (responsividade): no celular, 120px de cada lado deixavam ~150px para o conteúdo */
  @media (max-width: 1100px) {
    padding: 40px 40px 60px;
  }

  @media (max-width: 900px) {
    padding: 28px 16px 48px;
  }
`;

export const HeaderSection = styled.section`
  max-width: 1100px;
  margin: 0 auto 38px;
  color: var(--cream);

  h1 {
    font-size: 46px;
    line-height: 1.1;
    margin: 18px 0;
    max-width: 850px;
  }

  p {
    max-width: 760px;
    line-height: 1.7;
    color: rgba(241, 240, 226, 0.86);
  }

  @media (max-width: 900px) {
    margin-bottom: 26px;

    h1 {
      font-size: 32px;
      overflow-wrap: anywhere;
    }
  }
`;

export const BackButton = styled.button`
    display: inline-block;
    background: var(--yellow);
    color: var(--dark);
    padding: 9px 18px;
    border-radius: 999px;
    font-weight: 800;
    font-size: 14px;
    cursor: pointer;

    &:hover {
        background-color: var(--green);
        color: var(--white);
    }
`;

export const PhaseTag = styled.span`
    margin-left: 14px;
    background: transparent;
    border: none;
    color: var(--yellow);
    font-weight: 800;
    font-size: 15px;
    margin-bottom: 22px;

    /* celular: a etiqueta da fase quebra para baixo do botão "Voltar" sem recuo */
    @media (max-width: 900px) {
        display: block;
        margin: 12px 0 0;
    }
`;

export const ProgressArea = styled.div`
  margin-top: 28px;
  max-width: 520px;

  span {
    display: block;
    margin-bottom: 10px;
    font-weight: 700;
  }
`;

export const ProgressBar = styled.div`
  width: 100%;
  height: 18px;
  background: rgba(241, 240, 226, 0.3);
  border-radius: 999px;
  overflow: hidden;

  div {
    width: 35%;
    height: 100%;
    background: linear-gradient(90deg, var(--yellow), var(--orange));
    border-radius: 999px;
  }
`;

export const ContentGrid = styled.section`
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 28px;
  align-items: start;
`;

export const TheoryCard = styled.article`
  background: var(--cream);
  border: 4px solid var(--yellow);
  border-radius: 32px;
  padding: 34px;
  box-shadow: 0 18px 45px rgba(4, 4, 35, 0.28);

  @media (max-width: 900px) {
    padding: 22px 18px;
  }

  > span {
    color: var(--orange);
    font-weight: 900;
    text-transform: uppercase;
    font-size: 13px;
  }

  h2 {
    font-size: 32px;
    color: var(--dark);
    margin: 10px 0 18px;
  }

  h3 {
    color: var(--green);
    margin: 24px 0 12px;
  }

  p,
  li {
    color: var(--brown-dark);
    line-height: 1.7;
  }

  ul,
  ol {
    padding-left: 22px;
  }
`;

export const ExampleBox = styled.div`
  background: var(--white);
  border-left: 7px solid var(--orange);
  border-radius: 22px;
  padding: 22px;
  margin-top: 22px;
`;

export const CodeBox = styled.pre`
  background: var(--dark);
  color: var(--cream);
  padding: 18px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  overflow-x: auto;
  margin-top: 12px;
`;

export const PracticeCard = styled.article`
  background: var(--cream);
  border: 4px solid var(--green);
  border-radius: 32px;
  padding: 34px;
  box-shadow: 0 18px 45px rgba(4, 4, 35, 0.28);

  @media (max-width: 900px) {
    padding: 22px 18px;
  }

  > span {
    color: var(--green);
    font-weight: 900;
    text-transform: uppercase;
    font-size: 13px;
  }

  h2 {
    font-size: 32px;
    color: var(--dark);
    margin: 10px 0 18px;
  }
`;

export const ExerciseList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 22px;
`;

export const ExerciseItem = styled.div`
  background: var(--white);
  border-radius: 24px;
  padding: 22px;
  border: 2px solid rgba(236, 172, 12, 0.35);

    textarea {
        width: 100%;
        min-height: 140px;
        background: var(--cream);
        color: var(--dark);
        border: 2px solid rgba(40, 128, 120, 0.35);
        padding: 16px;
        border-radius: 16px;
        font-family: 'Poppins', sans-serif;
        font-size: 15px;
        outline: none;
        resize: vertical;

        &:focus {
            border-color: var(--orange);
        }
    }

  h3 {
    color: var(--orange);
    margin-bottom: 8px;
  }

  p {
    color: var(--brown-dark);
    line-height: 1.5;
    margin-bottom: 14px;
  }

  .row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
`;

export const CompleteCode = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--dark);
  color: var(--cream);
  padding: 16px;
  border-radius: 18px;
  flex-wrap: wrap;

  input {
    width: 80px;
    padding: 10px;
    border-radius: 12px;
    border: 2px solid var(--yellow);
    outline: none;
    text-align: center;
    font-weight: 800;
  }
`;

export const BottomActions = styled.div`
  max-width: 1100px;
  margin: 32px auto 0;
  display: flex;
  justify-content: flex-end;
  gap: 14px;
`;

export const PrimaryButton = styled.button`
  background: var(--yellow);
  color: var(--dark);
  border: none;
    cursor: pointer;
  padding: 15px 28px;
  border-radius: 18px;
  font-weight: 900;

    &:disabled {
        opacity: 0.55;
        cursor: not-allowed;

        &:hover {
            background-color: var(--yellow);
            color: var(--dark);
        }
    }
    
    &:hover {
        background-color: var(--green);
        color: var(--white);
    }
`;

export const SecondaryButton = styled.button`
  background: transparent;
  color: var(--cream);
  border: 2px solid var(--cream);
    cursor: pointer;
  padding: 13px 28px;
  border-radius: 18px;
  font-weight: 800;

    &:hover {
        background-color: var(--green);
        color: var(--white);
    }
`;

export const FlowchartSymbols = styled.div`
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  margin-top: 20px;
`;

export const SymbolCard = styled.div`
  background: var(--cream);
  border: 2px solid rgba(236, 172, 12, 0.45);
  border-radius: 18px;
  padding: 16px;
  display: grid;
  grid-template-columns: 110px 1fr;
  align-items: center;
  gap: 20px;

  h4 {
    color: var(--dark);
    margin-bottom: 4px;
  }

  p {
    color: var(--brown-dark);
    line-height: 1.5;
  }
`;

export const Oval = styled.div`
  width: 88px;
  height: 42px;
  background: rgba(236, 172, 12, 0.22);
  border: 3px solid var(--yellow);
  border-radius: 999px;
`;

export const FlowArrow = styled.div`
  color: var(--dark);
  font-size: 34px;
  font-weight: 900;
  text-align: center;
`;

export const Parallelogram = styled.div`
  width: 88px;
  height: 42px;
  background: rgba(236, 172, 12, 0.22);
  border: 3px solid var(--yellow);
  transform: skew(-20deg);
`;

export const Rectangle = styled.div`
  width: 88px;
  height: 42px;
  background: rgba(236, 172, 12, 0.22);
  border: 3px solid var(--yellow);
`;

export const Diamond = styled.div`
  width: 56px;
  height: 56px;
  margin-left: 16px;
  background: rgba(236, 172, 12, 0.22);
  border: 3px solid var(--yellow);
  transform: rotate(45deg);
`;

export const DocumentShape = styled.div`
  width: 88px;
  height: 48px;
  background: rgba(236, 172, 12, 0.22);
  border: 3px solid var(--yellow);
  border-radius: 2px 2px 45% 45%;
`;

/*
 * Sprint 7: caixinha do símbolo dentro da alternativa. Usa as MESMAS formas da teoria,
 * só que reduzidas (scale), para caber na altura do botão.
 */
export const IconeAlternativa = styled.span`
  flex-shrink: 0;
  width: 64px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;

  > span {
    display: flex;
    align-items: center;
    justify-content: center;
    transform: scale(0.62);
  }

  /* o losango da teoria tem margem para alinhar no card; aqui ele fica centralizado */
  > span > div {
    margin: 0;
  }

  /* alternativa selecionada (fundo amarelo): contorno escuro para a forma não "sumir" */
  .selected & > span > div {
    border-color: var(--brown-dark);
    color: var(--dark);
  }

  /* hover (fundo verde): a seta acompanha a cor do texto */
  button:hover & > span > div {
    color: inherit;
  }
`;

export const FlowExample = styled.div`
  margin-top: 20px;
  background: var(--cream);
  border: 2px dashed var(--orange);
  border-radius: 22px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

export const FlowStep = styled.div`
  min-width: 190px;
  background: rgba(236, 172, 12, 0.22);
  border: 3px solid var(--yellow);
  color: var(--dark);
  padding: 12px 18px;
  border-radius: 10px;
  text-align: center;
  font-weight: 800;

  &.terminal {
    border-radius: 999px;
  }

  &.document {
    border-radius: 2px 2px 45% 45%;
  }
`;
/* ---------- Sprint 6: integração com a API ---------- */

/* texto da teoria vindo do banco: respeita as quebras de linha ("\n- item") */
export const TheoryText = styled.p`
  white-space: pre-line;
`;

export const ObjectivesBox = styled.div`
  background: rgba(40, 128, 120, 0.1);
  border-radius: 22px;
  padding: 20px 24px;
  margin-bottom: 8px;

  h3 {
    margin-top: 0;
  }

  ul {
    padding-left: 4px;
  }

  li::before {
    content: "• ";
    color: var(--orange);
    font-weight: 900;
  }
`;

/* botão com contorno, legível sobre o fundo creme do card */
export const OutlineButton = styled.button`
  background: transparent;
  color: var(--green);
  border: 2px solid var(--green);
  cursor: pointer;
  padding: 13px 24px;
  border-radius: 18px;
  font-weight: 800;

  &:hover:not(:disabled),
  &.active {
    background-color: var(--green);
    color: var(--white);
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
`;

export const ExerciseMeta = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;

  span {
    background: rgba(236, 172, 12, 0.2);
    color: var(--brown-dark);
    padding: 5px 12px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 800;
  }

  span.done {
    background: var(--green);
    color: var(--cream);
  }
`;

export const HintButton = styled.button`
  margin-top: 14px;
  background: none;
  border: none;
  color: var(--orange);
  font-weight: 800;
  cursor: pointer;
  padding: 4px 0;
  text-decoration: underline;
`;

export const HintBox = styled.div`
  margin-top: 12px;
  background: rgba(236, 172, 12, 0.16);
  border-left: 6px solid var(--yellow);
  color: var(--brown-dark);
  padding: 12px 14px;
  border-radius: 14px;
  line-height: 1.5;
`;

export const StatusMessage = styled.div`
  text-align: center;
  color: var(--dark);
  font-weight: 700;
  padding: 24px 12px;
  line-height: 1.6;

  &.error {
    color: var(--orange);
  }
`;

/* ---------- Passo 4: áreas de resposta ---------- */

/* ORDENACAO */
export const OrderList = styled.ol`
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin-top: 6px;
`;

export const OrderItem = styled.li`
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--cream);
  border: 2px solid rgba(40, 128, 120, 0.35);
  border-radius: 16px;
  padding: 10px 12px;

  > strong {
    min-width: 30px;
    height: 30px;
    border-radius: 50%;
    background: var(--yellow);
    color: var(--dark);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  > span {
    flex: 1;
    color: var(--dark);
    font-weight: 600;
  }
`;

export const OrderControls = styled.div`
  display: flex;
  gap: 6px;

  button {
    width: 40px;
    height: 40px; /* área de toque confortável no celular */
    border-radius: 12px;
    border: 2px solid var(--green);
    background: var(--white);
    color: var(--green);
    font-size: 18px;
    font-weight: 900;
    cursor: pointer;

    &:hover:not(:disabled) {
      background: var(--green);
      color: var(--white);
    }

    &:disabled {
      opacity: 0.35;
      cursor: not-allowed;
    }
  }
`;

/* texto só para leitores de tela (anúncio após mover um item) */
export const VisuallyHidden = styled.span`
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
`;

/* ASSOCIACAO */
export const MatchRow = styled.div`
  display: grid;
  grid-template-columns: 1fr minmax(180px, 240px);
  gap: 12px;
  align-items: center;
  background: var(--cream);
  border-radius: 16px;
  padding: 12px 14px;
  margin-top: 9px;

  label {
    color: var(--dark);
    font-weight: 600;
    line-height: 1.4;
  }

  select {
    width: 100%;
    padding: 10px;
    border-radius: 12px;
    border: 2px solid rgba(40, 128, 120, 0.45);
    background: var(--white);
    color: var(--dark);
    font-family: inherit;
    font-weight: 600;

    &:focus {
      outline: none;
      border-color: var(--orange);
    }
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
`;

/* RESPOSTA_CURTA */
export const ShortAnswerInput = styled.input`
  width: 100%;
  max-width: 420px;
  background: var(--cream);
  color: var(--dark);
  border: 2px solid rgba(40, 128, 120, 0.35);
  padding: 13px 16px;
  border-radius: 16px;
  font-family: inherit;
  font-size: 15px;

  &:focus {
    outline: none;
    border-color: var(--orange);
  }
`;

/* ---------- Passo 5: envio e resultado ---------- */

export const QuestionFeedback = styled.div`
  margin-top: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  line-height: 1.6;
  color: var(--brown-dark);

  &.success {
    background: rgba(40, 128, 120, 0.12);
    border-left: 6px solid var(--green);
  }

  &.error {
    background: rgba(227, 92, 51, 0.1);
    border-left: 6px solid var(--orange);
  }

  .verdict {
    display: block;
    font-size: 17px;
    margin-bottom: 6px;
  }

  &.success .verdict {
    color: var(--green);
  }

  &.error .verdict {
    color: var(--orange);
  }

  .verdict span {
    margin-left: 8px;
    background: var(--yellow);
    color: var(--dark);
    padding: 2px 10px;
    border-radius: 999px;
    font-size: 13px;
  }

  p + p,
  p + ol,
  p + ul {
    margin-top: 6px;
  }

  ol,
  ul {
    padding-left: 22px;
  }

  ol {
    list-style: decimal;
  }

  .retry {
    font-weight: 700;
    color: var(--orange);
  }

  .note {
    font-size: 14px;
    font-style: italic;
  }
`;

export const RoundSummary = styled.section`
  margin-top: 28px;
  padding: 24px;
  border-radius: 24px;
  background: var(--white);
  border: 3px solid var(--yellow);
  text-align: center;

  &.success {
    border-color: var(--green);
    background: rgba(40, 128, 120, 0.08);
  }

  h3 {
    color: var(--dark);
    font-size: 24px;
    margin-bottom: 16px;
  }

  p {
    color: var(--brown-dark);
    margin-top: 14px;
    line-height: 1.5;
  }

  .note {
    font-size: 14px;
  }

  button {
    margin-top: 18px;
  }

  /* mascote centralizado no topo do resultado da rodada */
  > div:first-child {
    justify-content: center;
    margin-bottom: 12px;
  }
`;

export const SummaryStats = styled.div`
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 14px;

  div {
    min-width: 130px;
    background: var(--cream);
    border-radius: 18px;
    padding: 14px;
  }

  strong {
    display: block;
    font-size: 26px;
    color: var(--orange);
  }

  span {
    font-size: 13px;
    font-weight: 700;
    color: var(--brown-dark);
  }
`;

export const SubmitInfo = styled.p`
  margin-top: 24px;
  text-align: right;
  font-size: 14px;
  font-weight: 600;
  color: var(--brown-dark);
`;

/* ---------- Sprint 7: uma questão por vez, mascote e conclusão ---------- */

/* Barra FIXA da rodada: "gruda" no topo enquanto o aluno rola a questão (position: sticky) */
export const BarraRodada = styled.div`
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin: 0 -34px 24px;
  padding: 14px 34px;
  background: var(--dark);
  color: var(--cream);
  border-radius: 0 0 22px 22px;
  box-shadow: 0 10px 20px rgba(4, 4, 35, 0.18);
  scroll-margin-top: 0;

  strong {
    display: block;
    font-size: 16px;
  }

  span {
    font-size: 13px;
    font-weight: 700;
    color: var(--yellow);
  }

  ol {
    display: flex;
    gap: 8px;
    list-style: none;
  }

  @media (max-width: 600px) {
    margin: 0 -18px 20px;
    padding: 12px 18px;
  }
`;

/* um ponto por questão: verde = concluída, laranja = errada, vazio = não respondida */
export const PontoQuestao = styled.li`
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid var(--cream);
  background: transparent;
  transition: transform 0.2s;

  &.correta {
    background: var(--green);
    border-color: var(--green);
  }

  &.errada {
    background: var(--orange);
    border-color: var(--orange);
  }

  &.atual {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
    transform: scale(1.15);
  }

  .sr {
    position: absolute;
    width: 1px;
    height: 1px;
    overflow: hidden;
    clip: rect(0 0 0 0);
    white-space: nowrap;
  }
`;

export const FeedbackComMascote = styled.div`
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 18px;
  align-items: start;
  margin-top: 18px;

  > div:last-child {
    outline: none;
  }

  > div:last-child > div {
    margin-top: 0;
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
`;

const entrar = keyframes`
  from { opacity: 0; transform: translateY(16px) scale(0.9); }
  to   { opacity: 1; transform: none; }
`;

const flutuar = keyframes`
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-6px); }
`;

const pular = keyframes`
  0%   { transform: translateY(0) scale(1); }
  30%  { transform: translateY(-18px) scale(1.06) rotate(-4deg); }
  55%  { transform: translateY(0) scale(0.97); }
  75%  { transform: translateY(-7px) rotate(3deg); }
  100% { transform: translateY(0); }
`;

const balancar = keyframes`
  0%, 100% { transform: rotate(0); }
  20%      { transform: rotate(-8deg); }
  40%      { transform: rotate(7deg); }
  60%      { transform: rotate(-5deg); }
  80%      { transform: rotate(3deg); }
`;

/* Mascote + balão de fala. O tamanho vem da variável CSS --tamanho (definida no componente). */
export const MascoteBox = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;

  img {
    width: var(--tamanho, 96px);
    height: auto;
    transform-origin: bottom center;
    animation: ${entrar} 0.4s ease-out, ${flutuar} 3s ease-in-out 0.4s infinite;
  }

  &.acerto img {
    animation: ${pular} 0.8s ease-out;
  }

  &.erro img {
    animation: ${balancar} 0.6s ease-in-out;
  }

  p {
    position: relative;
    background: var(--white);
    border: 2px solid var(--yellow);
    color: var(--dark);
    font-weight: 700;
    font-size: 14px;
    padding: 8px 12px;
    border-radius: 14px;
    max-width: 180px;
  }

  &.acerto p {
    border-color: var(--green);
  }

  &.erro p {
    border-color: var(--orange);
  }

  /* acessibilidade: quem pediu menos movimento no sistema não vê as animações */
  @media (prefers-reduced-motion: reduce) {
    img,
    &.acerto img,
    &.erro img {
      animation: none;
    }
  }
`;

export const AcoesConclusao = styled.div`
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;

  button {
    margin-top: 0;
  }
`;

const aparecer = keyframes`
  from { opacity: 0; transform: scale(0.94); }
  to   { opacity: 1; transform: none; }
`;

/* Destaque "Fase concluída" (dentro da própria página da missão) */
export const Celebracao = styled.section`
  margin-top: 8px;
  padding: 30px 24px;
  border-radius: 28px;
  text-align: center;
  background:
    radial-gradient(circle at 20% 15%, rgba(236, 172, 12, 0.35), transparent 40%),
    radial-gradient(circle at 85% 20%, rgba(227, 92, 51, 0.25), transparent 40%),
    var(--white);
  border: 4px solid var(--green);
  animation: ${aparecer} 0.5s ease-out;

  video,
  img {
    width: 150px;
    height: auto;
    /* o vídeo tem fundo branco: "multiply" faz o branco sumir sobre o fundo claro do card */
    mix-blend-mode: multiply;
  }

  > span {
    display: block;
    margin-top: 6px;
    color: var(--orange);
    font-weight: 900;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.06em;
  }

  h3 {
    font-size: 34px;
    color: var(--green);
    margin: 4px 0 8px;
  }

  p {
    color: var(--brown-dark);
    margin-bottom: 18px;
    line-height: 1.5;
  }

  p.note {
    margin: 14px 0 0;
    font-size: 14px;
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

/* botão com aparência de link (ações secundárias, ex.: "Rever teoria") */
export const LinkButton = styled.button`
  background: none;
  border: none;
  color: var(--green);
  font-weight: 800;
  text-decoration: underline;
  cursor: pointer;
  padding: 6px 2px;

  &:hover {
    color: var(--orange);
  }
`;
