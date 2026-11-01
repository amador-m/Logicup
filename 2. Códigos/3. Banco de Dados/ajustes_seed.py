"""
Ajustes do seed: tudo o que o Banco de Questões NÃO diz de forma que um programa consiga ler,
mas que o banco de dados precisa saber. Fica SEPARADO do gerador para ficar claro, na banca,
o que veio do documento e o que foi decisão de implementação.

Cada chave é o código da questão. Campos possíveis:
  lacunas               -> RESPOSTA_CURTA com várias lacunas (D3, Sprint 9): uma lista de
                           respostas aceitas POR lacuna, na ordem em que aparecem no código.
  aceitas               -> RESPOSTA_CURTA: lista completa das respostas aceitas (substitui a do documento).
  aceitas_extras        -> RESPOSTA_CURTA: outras respostas equivalentes aceitas.
  diferenciarMaiusculas -> RESPOSTA_CURTA: padrão True (Java diferencia maiúsculas: "int" != "Int").
  icones                -> ESCOLHA_UNICA: {texto da alternativa: ícone de fluxograma} (Sprint 7).
  uma_linha_por_item    -> ESCOLHA_UNICA: o documento pede "uma linha por mensagem/valor";
                           o separador " / " das opções vira quebra de linha.

O gerador PARA se aqui houver uma questão que não existe no documento (evita ajuste esquecido).
"""

AJUSTES = {
    # Sprint 7: cada alternativa mostra o símbolo de fluxograma (mesmas formas da teoria).
    # Valores aceitos pelo frontend: inicio-fim | fluxo | entrada | processamento | decisao | saida
    "F1.Q8": {"icones": {"Retângulo (processamento)": "processamento", "Losango (decisão)": "decisao",
                         "Oval (início/fim)": "inicio-fim", "Seta (fluxo)": "fluxo"}},

    # Sprint 9 (feedback da Maria): print e println exibem a mensagem pedida ("exibir"), e o enunciado
    # não exige a quebra de linha. As duas respostas mostram o mesmo conhecimento -> as duas são aceitas.
    "F2.Q3": {"aceitas_extras": ["System.out.print"]},

    # "Resposta esperada: “int” e “18” (o esperado é int idade = 18;)"
    "F3.Q3": {"lacunas": [["int"], ["18"]]},

    # "Resposta esperada: -- (ou seja, i--)". A lacuna fica depois do "i" (i___), então o esperado
    # é "--"; aceitamos também "i--", que mostra o mesmo entendimento.
    "F5.Q3": {"aceitas": ["--", "i--"]},

    # Saídas de várias linhas: o documento separa as linhas com " / ".
    # F3.Q10, F4.Q10 e F5.Q1 têm a nota explícita "(Na plataforma, cada opção é exibida com uma
    # linha por valor/mensagem.)"; as demais seguem a mesma convenção (opções de PREVER_SAIDA/Boss).
    # ATENÇÃO: F6.Q7 NÃO entra: lá " / " é a divisão do Java.
    **{codigo: {"uma_linha_por_item": True} for codigo in [
        "F3.Q10", "F4.Q10", "F5.Q1", "F5.Q7", "F5.Q10", "F7.Q1", "F7.Q6", "F7.Q10",
        "F8.Q10", "F9.R1.Q10", "F9.R2.Q10", "F10.Q6", "F10.Q10"]},
}

# Parágrafos da teoria que não fazem sentido no site (legenda de imagem que o frontend já desenha:
# a Fase 1, Rodada 2, mostra os símbolos de fluxograma pelo componente SimbolosFluxograma).
PARAGRAFOS_IGNORADOS_NA_TEORIA = {"Símbolos de fluxograma"}
