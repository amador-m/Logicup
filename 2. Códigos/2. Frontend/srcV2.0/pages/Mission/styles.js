import styled from "styled-components";

export const Card = styled.article`
  max-width: 1000px;
  margin: 0 auto;
  background: var(--cream);
  border: 4px solid var(--yellow);
  border-radius: 34px;
  padding: 34px;
  box-shadow: 0 18px 45px rgba(4, 4, 35, 0.28);
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
    &:hover {
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