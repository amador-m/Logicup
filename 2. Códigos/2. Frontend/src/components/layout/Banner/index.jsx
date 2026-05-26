import {
    Container,
    Overlay,
    Content,
    Tag,
    Title,
    Text,
    Buttons,
    PrimaryButton,
    SecondaryButton,
} from "./styles";

const Banner = () => {
    return (
        <Container>
            <Overlay />

            <Content>
                <Tag>Plataforma gamificada de Java</Tag>

                <Title>
                    Aprenda programação avançando por missões!
                </Title>

                <Text>
                    O Logicup transforma o estudo de lógica e Java em uma trilha de
                    aprendizado com fases, pontos, ranking, recompensas e desafios
                </Text>

                <Buttons>
                    <PrimaryButton>Começar agora</PrimaryButton>
                    <SecondaryButton>Ver trilha</SecondaryButton>
                </Buttons>
            </Content>
        </Container>
    );
};

export default Banner;