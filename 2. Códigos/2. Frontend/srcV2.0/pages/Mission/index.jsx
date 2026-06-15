import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { phaseOneRounds } from "../../data/phaseOne";

import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";

import {
    Page,
    HeaderSection,
    BackButton,
    PhaseTag,
    ProgressArea,
    ProgressBar,
    RoundTabs,
    RoundTab,
    Card,
    CardHeader,
    TheoryContent,
    ExampleBox,
    CodeBox,
    ExerciseList,
    ExerciseItem,
    OptionButton,
    CompleteCode,
    FeedbackBox,
    CorrectionBox,
    BottomActions,
    PrimaryButton,
    SecondaryButton,
    FlowchartSymbols,
    SymbolCard,
    Oval,
    FlowArrow,
    Parallelogram,
    Rectangle,
    Diamond,
    DocumentShape,
    FlowExample,
    FlowStep,
} from "./styles";

const rounds = phaseOneRounds;

const Mission = () => {
    const navigate = useNavigate();

    const [currentRound, setCurrentRound] = useState(0);
    const [step, setStep] = useState("theory");
    const [answers, setAnswers] = useState({});
    const [showResult, setShowResult] = useState(false);
    const [showCorrection, setShowCorrection] = useState(false);
    const [unlockedRounds, setUnlockedRounds] = useState([1]);

    const round = rounds[currentRound];

    const handleAnswer = (exerciseIndex, selectedAnswer) => {
        setAnswers({
            ...answers,
            [exerciseIndex]: selectedAnswer,
        });
    };

    const isExerciseCorrect = (exercise, index) => {
        const userAnswer = answers[index];

        if (!userAnswer) return false;

        if (exercise.textAnswer) {
            const normalizedAnswer = userAnswer.toLowerCase();

            const matchedKeywords = exercise.keywords.filter((keyword) =>
                normalizedAnswer.includes(keyword.toLowerCase())
            );

            return matchedKeywords.length >= 3;
        }

        return userAnswer === exercise.answer;
    };

    const allCorrect = round.exercises.every((exercise, index) =>
        isExerciseCorrect(exercise, index)
    );

    const verifyAnswers = () => {
        setShowResult(true);

        if (allCorrect) {
            const nextRoundId = round.id + 1;

            if (nextRoundId <= rounds.length && !unlockedRounds.includes(nextRoundId)) {
                setUnlockedRounds([...unlockedRounds, nextRoundId]);
            }
        }
    };

    const resetPractice = () => {
        setAnswers({});
        setShowResult(false);
        setShowCorrection(false);
    };

    const goToRound = (index) => {
        const selectedRound = rounds[index];

        if (!unlockedRounds.includes(selectedRound.id)) return;

        setCurrentRound(index);
        setStep("theory");
        setAnswers({});
        setShowResult(false);
        setShowCorrection(false);
    };

    const goToNextRound = () => {
        if (currentRound < rounds.length - 1) {
            setCurrentRound(currentRound + 1);
            setStep("theory");
            setAnswers({});
            setShowResult(false);
            setShowCorrection(false);
        }
    };

    return (
        <>
            <Header />

            <Page>
                <HeaderSection>
                    <BackButton onClick={() => navigate("/trilha")}>
                        ← Voltar para trilha
                    </BackButton>

                    <PhaseTag>Fase 1 • Lógica de Programação</PhaseTag>

                    <h1>{round.title}</h1>

                    <p>
                        Estude a teoria, veja exemplos e resolva os desafios práticos para
                        liberar o próximo round da fase.
                    </p>

                    <ProgressArea>
            <span>
              Round {round.id} de {rounds.length}
            </span>

                        <ProgressBar>
                            <div
                                style={{
                                    width: `${(unlockedRounds.length / rounds.length) * 100}%`,
                                }}
                            />
                        </ProgressBar>
                    </ProgressArea>

                    <RoundTabs>
                        {rounds.map((item, index) => (
                            <RoundTab
                                key={item.id}
                                className={currentRound === index ? "active" : ""}
                                disabled={!unlockedRounds.includes(item.id)}
                                onClick={() => goToRound(index)}
                            >
                                {unlockedRounds.includes(item.id) ? `Round ${item.id}` : "🔒"}
                            </RoundTab>
                        ))}
                    </RoundTabs>
                </HeaderSection>

                <Card>
                    <CardHeader>
                        <div>
              <span>
                {step === "theory" ? "Teoria + exemplos" : "Desafios práticos"}
              </span>
                            <h2>{round.title}</h2>
                        </div>

                        <div>
                            <SecondaryButton onClick={() => setStep("theory")}>
                                Ver teoria
                            </SecondaryButton>

                            <PrimaryButton onClick={() => setStep("practice")}>
                                Ir para exercícios
                            </PrimaryButton>
                        </div>
                    </CardHeader>

                    {step === "theory" && (
                        <TheoryContent>
                            <h3>Conceito</h3>
                            <p>{round.theory.concept}</p>

                            <ExampleBox>
                                <h3>{round.theory.exampleTitle}</h3>
                                <p>{round.theory.exampleText}</p>

                                <CodeBox>{round.theory.code}</CodeBox>
                            </ExampleBox>

                            {round.id === 1 && (
                                <>
                                    <ExampleBox>
                                        <h3>Principais símbolos dos fluxogramas</h3>

                                        <p>
                                            Fluxogramas usam símbolos para representar cada etapa de um
                                            algoritmo. Cada forma tem um significado diferente.
                                        </p>

                                        <FlowchartSymbols>
                                            <SymbolCard>
                                                <Oval />
                                                <div>
                                                    <h4>Início/Fim</h4>
                                                    <p>Marca onde o algoritmo começa ou termina.</p>
                                                </div>
                                            </SymbolCard>

                                            <SymbolCard>
                                                <FlowArrow>↓</FlowArrow>
                                                <div>
                                                    <h4>Fluxo</h4>
                                                    <p>Mostra a direção em que os passos acontecem.</p>
                                                </div>
                                            </SymbolCard>

                                            <SymbolCard>
                                                <Parallelogram />
                                                <div>
                                                    <h4>Entrada de dados</h4>
                                                    <p>Representa informações recebidas pelo algoritmo.</p>
                                                </div>
                                            </SymbolCard>

                                            <SymbolCard>
                                                <Rectangle />
                                                <div>
                                                    <h4>Processamento</h4>
                                                    <p>Indica cálculos, ações ou atribuições de valores.</p>
                                                </div>
                                            </SymbolCard>

                                            <SymbolCard>
                                                <Diamond />
                                                <div>
                                                    <h4>Decisão</h4>
                                                    <p>
                                                        Representa escolhas, como perguntas de sim ou não.
                                                    </p>
                                                </div>
                                            </SymbolCard>

                                            <SymbolCard>
                                                <DocumentShape />
                                                <div>
                                                    <h4>Saída de dados</h4>
                                                    <p>
                                                        Mostra uma resposta ou resultado apresentado ao
                                                        usuário.
                                                    </p>
                                                </div>
                                            </SymbolCard>
                                        </FlowchartSymbols>
                                    </ExampleBox>

                                    <ExampleBox>
                                        <h3>Exemplo de fluxograma: fazer café</h3>

                                        <FlowExample>
                                            <FlowStep className="terminal">Início</FlowStep>
                                            <FlowArrow>↓</FlowArrow>
                                            <FlowStep>Ferver água</FlowStep>
                                            <FlowArrow>↓</FlowArrow>
                                            <FlowStep>Adicionar pó</FlowStep>
                                            <FlowArrow>↓</FlowArrow>
                                            <FlowStep>Coar café</FlowStep>
                                            <FlowArrow>↓</FlowArrow>
                                            <FlowStep className="document">Servir</FlowStep>
                                            <FlowArrow>↓</FlowArrow>
                                            <FlowStep className="terminal">Fim</FlowStep>
                                        </FlowExample>
                                    </ExampleBox>
                                </>
                            )}

                            <BottomActions>
                                <PrimaryButton onClick={() => setStep("practice")}>
                                    Avançar para desafios
                                </PrimaryButton>
                            </BottomActions>
                        </TheoryContent>
                    )}

                    {step === "practice" && (
                        <>
                            <ExerciseList>
                                {round.exercises.map((exercise, index) => (
                                    <ExerciseItem key={index}>
                                        <span>{exercise.type.toUpperCase()}</span>

                                        <h3>
                                            {index + 1}. {exercise.question}
                                        </h3>

                                        {exercise.code && <CodeBox>{exercise.code}</CodeBox>}

                                        {exercise.type === "complete" && (
                                            <CompleteCode>
                                                <span>Primeiro eu devo:</span>
                                            </CompleteCode>
                                        )}

                                        {exercise.textAnswer && (
                                            <textarea
                                                placeholder="Digite seu algoritmo aqui..."
                                                value={answers[index] || ""}
                                                onChange={(event) =>
                                                    handleAnswer(index, event.target.value)
                                                }
                                            />
                                        )}

                                        {exercise.options?.map((option) => (
                                            <OptionButton
                                                key={option}
                                                type="button"
                                                className={answers[index] === option ? "selected" : ""}
                                                onClick={() => handleAnswer(index, option)}
                                            >
                                                {option}
                                            </OptionButton>
                                        ))}

                                        {showCorrection && (
                                            <CorrectionBox>
                                                {exercise.correction ? (
                                                    exercise.correction
                                                ) : (
                                                    <>
                                                        Correção: a resposta correta é{" "}
                                                        <strong>{exercise.answer}</strong>.
                                                    </>
                                                )}
                                            </CorrectionBox>
                                        )}
                                    </ExerciseItem>
                                ))}
                            </ExerciseList>

                            {showResult && (
                                <FeedbackBox className={allCorrect ? "success" : "error"}>
                                    {allCorrect
                                        ? "Parabéns! Você passou nesse round e liberou o próximo desafio."
                                        : "Ainda não foi dessa vez. Revise suas respostas, tente novamente ou veja a correção."}
                                </FeedbackBox>
                            )}

                            <BottomActions>
                                <SecondaryButton onClick={() => setStep("theory")}>
                                    Voltar para teoria
                                </SecondaryButton>

                                {!showResult && (
                                    <PrimaryButton onClick={verifyAnswers}>
                                        Verificar
                                    </PrimaryButton>
                                )}

                                {showResult && !allCorrect && (
                                    <>
                                        <SecondaryButton onClick={resetPractice}>
                                            Tentar novamente
                                        </SecondaryButton>

                                        <PrimaryButton onClick={() => setShowCorrection(true)}>
                                            Ver correção
                                        </PrimaryButton>
                                    </>
                                )}

                                {showResult && allCorrect && currentRound < rounds.length - 1 && (
                                    <PrimaryButton onClick={goToNextRound}>
                                        Avançar para próximo round
                                    </PrimaryButton>
                                )}

                                {showResult && allCorrect && currentRound === rounds.length - 1 && (
                                    <PrimaryButton onClick={() => navigate("/trilha")}>
                                        Finalizar missão
                                    </PrimaryButton>
                                )}
                            </BottomActions>
                        </>
                    )}
                </Card>
            </Page>

            <Footer />
        </>
    );
};

export default Mission;