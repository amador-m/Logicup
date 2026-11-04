"""
Sprint 9 (ADR-001, etapa 1 do Blockly): as questões de BLOCOS (formato BLOCOS).

Uma questão de blocos é corrigida igual a uma questão de código (CorretorBlocos -> CorretorCodigo): os blocos
viram Java no navegador (geradorJava.js) e esse Java roda nos casos de teste. Por isso cada questão tem os
mesmos campos das questões de código (modo, comparacao, teste, exemplo, casos, referencia, certas, erradas)
e mais:

  blocos_iniciais -> os blocos que aparecem na área quando o aluno abre a questão (formato JSON do Blockly).
                     Decisão 5-A (02/11): os blocos já vêm na área, SOLTOS e EMBARALHADOS, e não há caixa
                     de ferramentas: o aluno só precisa encaixá-los dentro do main, na ordem certa.
                     Nenhum bloco pode ser apagado ("deletable": false) nem editado ("editable": false), e os
                     valores de dentro (ex.: casa % 3 == 0) ficam presos no bloco ("movable": false).
  palavras_chave, minimo, resposta_modelo -> a reserva por palavras-chave (prévia quando o executor está fora
                     do ar) e a resposta exibida depois da correção, como nas questões de código.

A "referencia" é EXATAMENTE o Java que o geradorJava.js produz com os blocos montados na ordem certa
(conferido no teste do gerador, caso "F5.Q8 certa").
"""

# ------------------------------------------------------------------ ajudantes do JSON do Blockly
FIXO = {"deletable": False, "editable": False}            # bloco solto: arrasta, mas não apaga nem edita
PRESO = {"deletable": False, "editable": False, "movable": False}  # valor de dentro: não sai do bloco


def num(identificador, valor):
    return {"block": {"type": "java_numero", "id": identificador, "fields": {"NUM": str(valor)}, **PRESO}}


def var(identificador, variavel):
    return {"block": {"type": "java_variavel", "id": identificador, "fields": {"VAR": {"id": variavel}}, **PRESO}}


def conta(identificador, operador, a, b, tipo="java_aritmetica"):
    return {"block": {"type": tipo, "id": identificador, "fields": {"OP": operador}, "inputs": {"A": a, "B": b}, **PRESO}}


F5Q8_BLOCOS = {
    "blocks": {
        "languageVersion": 0,
        "blocks": [
            {"type": "java_main", "id": "main", "x": 20, "y": 20, "deletable": False},
            # embaralhados de propósito (nem a posição na tela entrega a ordem)
            {"type": "java_imprimir", "id": "imprimir", "x": 380, "y": 40, **FIXO,
             "fields": {"METODO": "println"},
             "inputs": {"TEXTO": conta("juntar", "+",
                                       {"block": {"type": "java_texto", "id": "texto", "fields": {"TEXTO": "Baús: "}, **PRESO}},
                                       var("baus_no_texto", "var_baus"))}},
            {"type": "java_if", "id": "se", "x": 40, "y": 190, **FIXO,
             "inputs": {"COND": conta("comparar", "==", conta("resto", "%", var("casa_no_if", "var_casa"), num("tres", 3)),
                                      num("zero_if", 0), tipo="java_comparacao")}},
            {"type": "java_declarar", "id": "declarar", "x": 400, "y": 150, **FIXO,
             "fields": {"TIPO": "int", "VAR": {"id": "var_baus"}}, "inputs": {"VALOR": num("zero", 0)}},
            {"type": "java_incremento", "id": "somar", "x": 60, "y": 330, **FIXO,
             "fields": {"VAR": {"id": "var_baus"}, "OP": "++"}},
            {"type": "java_for", "id": "para", "x": 330, "y": 270, **FIXO,
             "fields": {"VAR": {"id": "var_casa"}, "OP": "<=", "PASSO": "++"},
             "inputs": {"INICIO": num("um", 1), "FIM": num("vinte", 20)}},
        ],
    },
    "variables": [{"name": "baus", "id": "var_baus"}, {"name": "casa", "id": "var_casa"}],
}

PROGRAMA_F5Q8 = """public class Main {
    static void executar() {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        System.out.print("<<<LOGICUP-CASO>>>");
        executar();
    }
}
"""

QUESTOES_BLOCOS = {
    "F5.Q8": dict(
        modo="TRECHO", comparacao="EXATA",
        codigo_inicial="",
        teste=PROGRAMA_F5Q8,
        casos=["trilha de 20 casas"],
        exemplo=PROGRAMA_F5Q8.replace('        System.out.print("<<<LOGICUP-CASO>>>");\n', ""),
        exemplos=[],
        referencia="""int baus = 0;
for (int casa = 1; casa <= 20; casa++) {
    if (casa % 3 == 0) {
        baus++;
    }
}
System.out.println("Baús: " + baus);
""",
        certas=[],  # com só estes blocos, a montagem certa é única
        erradas={
            "baus++ fora do if (conta todas as casas)": """int baus = 0;
for (int casa = 1; casa <= 20; casa++) {
    if (casa % 3 == 0) {
    }
    baus++;
}
System.out.println("Baús: " + baus);
""",
            "println dentro do for (exibe 20 vezes)": """int baus = 0;
for (int casa = 1; casa <= 20; casa++) {
    if (casa % 3 == 0) {
        baus++;
    }
    System.out.println("Baús: " + baus);
}
""",
            "contador declarado dentro do for (não compila no println)": """for (int casa = 1; casa <= 20; casa++) {
    int baus = 0;
    if (casa % 3 == 0) {
        baus++;
    }
}
System.out.println("Baús: " + baus);
""",
            "println antes do for (exibe 0)": """int baus = 0;
System.out.println("Baús: " + baus);
for (int casa = 1; casa <= 20; casa++) {
    if (casa % 3 == 0) {
        baus++;
    }
}
""",
        },
        palavras_chave=[["int baus"], ["for"], ["if"], ["%"], ["baus++"], ["println"]],
        minimo=6,
        resposta_modelo="""int baus = 0;
for (int casa = 1; casa <= 20; casa++) {
    if (casa % 3 == 0) {
        baus++;
    }
}
System.out.println("Baús: " + baus);""",
        blocos_iniciais=F5Q8_BLOCOS,
    ),
}

# =========================================================================================================
# 03/11 — mais 5 questões em BLOCOS (decisões da Maria: 1-A blocos de linha, 2-B "Ver o Java" como
# curiosidade na Fase 1, 3 F2.Q4 mais difícil mantendo 15 XP, 4 F5.Q4 continua como ordenação).
#
# Campos novos (opcionais) de uma questão de blocos:
#   enunciado / explicacao / habilidade -> SUBSTITUEM os do documento (o texto dizia "organize as linhas")
#   sobram    -> True quando há blocos que NÃO devem ser usados (vai para alternativas: a tela não pede
#                para encaixar todos)
#   montagem  -> a montagem CERTA, como lista (pai, encaixe, filho). NÃO vai para o banco: é usada pelo
#                teste do gerador para conferir que os blocos montados geram exatamente a "referencia".
# =========================================================================================================

def linha(identificador, texto, x, y, tipo="java_linha"):
    return {"type": tipo, "id": identificador, "x": x, "y": y, **FIXO, "fields": {"TEXTO": texto}}


def bloco(identificador, texto, x, y):
    return linha(identificador, texto, x, y, tipo="java_bloco")


def raiz(identificador, texto):
    """O bloco de fora (classe ou método da questão): não encaixa em nada, como o main."""
    return linha(identificador, texto, 20, 20, tipo="java_raiz")


def passo(identificador, texto, x, y):
    return linha(identificador, texto, x, y, tipo="java_passo")


def area(blocos, variaveis=()):
    estado = {"blocks": {"languageVersion": 0, "blocks": blocos}}
    if variaveis:
        estado["variables"] = [{"name": nome, "id": f"var_{nome}"} for nome in variaveis]
    return estado


def programa_trecho(parametros="", chamadas=('executar();',)):
    """Programa de teste do modo TRECHO: o código do aluno vira o corpo de executar(...)."""
    linhas_main = "".join(f'        System.out.print("<<<LOGICUP-CASO>>>");\n        {c}\n' for c in chamadas)
    return ("public class Main {\n"
            f"    static void executar({parametros}) {{\n"
            "//@CODIGO_DO_ALUNO\n"
            "    }\n\n"
            "    public static void main(String[] args) {\n"
            f"{linhas_main}"
            "    }\n"
            "}\n")


def sem_marcas(programa):
    return programa.replace('        System.out.print("<<<LOGICUP-CASO>>>");\n', "")


# A classe Bug da Fase 9 (o enunciado das questões diz que ela "já existe")
BUG = """
class Bug {
    private String nome;
    private int vida;

    public Bug(String nome, int vida) {
        this.nome = nome;
        this.vida = vida;
    }

    public int atacar() {
        return 10;
    }

    public String getNome() {
        return nome;
    }

    public int getVida() {
        return vida;
    }
}
"""

# ------------------------------------------------------------------ F1.Q4 (Fase 1: passos em português)
CAFE = ["Ferver a água", "Adicionar pó de café", "Coar o café", "Servir"]
PROGRAMA_F1Q4 = programa_trecho()


def cafe(*passos):
    return "".join(f'System.out.println("{p}");\n' for p in passos)


QUESTOES_BLOCOS["F1.Q4"] = dict(
    modo="TRECHO", comparacao="EXATA",
    codigo_inicial="",
    teste=PROGRAMA_F1Q4,
    casos=["o café da manhã do Gattuccino"],
    exemplo=sem_marcas(PROGRAMA_F1Q4),
    exemplos=[],
    referencia=cafe(*CAFE),
    certas=[],
    erradas={
        "coar antes de pôr o pó": cafe("Ferver a água", "Coar o café", "Adicionar pó de café", "Servir"),
        "pó antes de ferver a água": cafe("Adicionar pó de café", "Ferver a água", "Coar o café", "Servir"),
        "servir antes de coar": cafe("Ferver a água", "Adicionar pó de café", "Servir", "Coar o café"),
        "faltou um passo": cafe("Ferver a água", "Adicionar pó de café", "Servir"),
    },
    palavras_chave=[[p] for p in CAFE],
    minimo=4,
    resposta_modelo=cafe(*CAFE).rstrip("\n"),
    enunciado="O Gattuccino não começa uma missão sem café. Encaixe os passos dentro do algoritmo, na ordem "
              "correta, para ele preparar o café da manhã:",
    blocos_iniciais=area([
        {"type": "java_algoritmo", "id": "algoritmo", "x": 20, "y": 20, **FIXO,
         "fields": {"TITULO": "café da manhã do Gattuccino ☕"}},
        passo("servir", "Servir", 400, 30),
        passo("po", "Adicionar pó de café", 380, 110),
        passo("coar", "Coar o café", 60, 200),
        passo("ferver", "Ferver a água", 330, 200),
    ]),
    montagem=[("algoritmo", "CORPO", "ferver"), ("ferver", "next", "po"), ("po", "next", "coar"),
              ("coar", "next", "servir")],
)

# ------------------------------------------------------------------ F2.Q4 (programa completo, mais difícil)
# O aluno monta a classe Main inteira. Quem chama o main dele é a nossa classe LogicupTeste (a 1ª do arquivo:
# o "java Main.java" do Piston executa a primeira classe). Se o main estiver com a forma errada, não compila.
PROGRAMA_F2Q4 = """public class LogicupTeste {
    public static void main(String[] args) {
        System.out.print("<<<LOGICUP-CASO>>>");
        Main.main(new String[0]);
    }
}

//@CODIGO_DO_ALUNO
"""

F2Q4_REFERENCIA = """public class Main {
    public static void main(String[] args) {
        // Mostra as boas-vindas e a fase atual
        System.out.println("Bem-vindo ao LogiCup!");
        System.out.print("Fase 2");
        System.out.println(" de 10");
    }
}
"""


def f2q4(*corpo_main, main="public static void main(String[] args) {", fora_do_main=()):
    dentro = "".join(f"        {c}\n" for c in corpo_main)
    fora = "".join(f"    {c}\n" for c in fora_do_main)
    return f"public class Main {{\n{fora}    {main}\n{dentro}    }}\n}}\n"


QUESTOES_BLOCOS["F2.Q4"] = dict(
    # PROGRAMA: modo novo do backend (MontadorPrograma), em que a classe Main faz parte da resposta
    modo="PROGRAMA", comparacao="EXATA",
    codigo_inicial="",
    teste=PROGRAMA_F2Q4,
    casos=["o programa completo"],
    exemplo=sem_marcas(PROGRAMA_F2Q4),
    exemplos=[],
    referencia=F2Q4_REFERENCIA,
    certas=[
        # o comentário é opcional e pode ficar em outro lugar
        f2q4('System.out.println("Bem-vindo ao LogiCup!");', 'System.out.print("Fase 2");',
             'System.out.println(" de 10");'),
        f2q4('System.out.println("Bem-vindo ao LogiCup!");', 'System.out.print("Fase 2");',
             'System.out.println(" de 10");', fora_do_main=["// Mostra as boas-vindas e a fase atual"]),
    ],
    erradas={
        "main com String args, sem os colchetes (não compila)": f2q4(
            'System.out.println("Bem-vindo ao LogiCup!");', 'System.out.print("Fase 2");',
            'System.out.println(" de 10");', main="public static void main(String args) {"),
        "println(\"Fase 2\") no lugar do print (quebra a linha)": f2q4(
            'System.out.println("Bem-vindo ao LogiCup!");', 'System.out.println("Fase 2");',
            'System.out.println(" de 10");'),
        "texto sem aspas (não compila)": f2q4(
            'System.out.println(Bem-vindo ao LogiCup!);', 'System.out.print("Fase 2");',
            'System.out.println(" de 10");'),
        "linhas fora de ordem": f2q4(
            'System.out.print("Fase 2");', 'System.out.println(" de 10");',
            'System.out.println("Bem-vindo ao LogiCup!");'),
        "comandos fora do main (não compila)": f2q4(
            fora_do_main=['System.out.println("Bem-vindo ao LogiCup!");']),
    },
    palavras_chave=[["class Main"], ["main(String[] args)"], ["\"Bem-vindo ao LogiCup!\""],
                    ["print(\"Fase 2\")"], ["println(\" de 10\")"]],
    minimo=5,
    resposta_modelo=F2Q4_REFERENCIA.rstrip("\n"),
    enunciado="Monte um programa Java completo que exiba exatamente estas duas linhas:\n\n"
              "Bem-vindo ao LogiCup!\nFase 2 de 10\n\n"
              "Atenção: alguns blocos parecem certos, mas têm erros, e nem todos os blocos devem ser usados.",
    habilidade="Montar um programa Java completo (classe, método main e instruções), reconhecendo erros de "
               "estrutura e de sintaxe e a diferença entre print e println.",
    explicacao="A classe Main envolve todo o programa, e o método main fica dentro dela: é por ele que a "
               "execução começa. O main precisa ter exatamente a forma public static void main(String[] args); "
               "com String args (sem os colchetes), o Java não reconhece o ponto de partida do programa. "
               "Os comandos ficam dentro do main, e todo texto precisa estar entre aspas. Como o print não "
               "pula linha, \"Fase 2\" e \" de 10\" aparecem na mesma linha; o println do final é que pula "
               "para a próxima. O comentário (//) é opcional: ele não muda nada na saída.",
    sobram=True,
    blocos_iniciais=area([
        raiz("classe", "public class Main {"),
        linha("fase_println", 'System.out.println("Fase 2");', 420, 20),
        bloco("main_falso", "public static void main(String args) {", 30, 160),
        linha("total", 'System.out.println(" de 10");', 420, 90),
        linha("sem_aspas", "System.out.println(Bem-vindo ao LogiCup!);", 420, 160),
        bloco("main_certo", "public static void main(String[] args) {", 30, 300),
        linha("comentario", "// Mostra as boas-vindas e a fase atual", 420, 230),
        linha("fase", 'System.out.print("Fase 2");', 420, 300),
        linha("boas_vindas", 'System.out.println("Bem-vindo ao LogiCup!");', 420, 370),
    ]),
    montagem=[("classe", "CORPO", "main_certo"), ("main_certo", "CORPO", "comentario"),
              ("comentario", "next", "boas_vindas"), ("boas_vindas", "next", "fase"), ("fase", "next", "total")],
)

# ------------------------------------------------------------------ F4.Q5 (if/else com os blocos de Java)
PROGRAMA_F4Q5 = """public class Main {
    static void executar(int xp) {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        int[] valores = {150, 100, 99, 0};
        for (int valor : valores) {
            System.out.print("<<<LOGICUP-CASO>>>");
            executar(valor);
        }
    }
}
"""
EXEMPLO_F4Q5 = """public class Main {
    static void executar(int xp) {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        System.out.println("--- Exemplo: xp = 120 ---");
        executar(120);
        System.out.println("--- Exemplo: xp = 40 ---");
        executar(40);
    }
}
"""


def nivel(dentro_if, dentro_else, depois=""):
    return (f"if (xp >= 100) {{\n{dentro_if}}} else {{\n{dentro_else}}}\n{depois}")


SUBIU = '    System.out.println("Subiu de nível!");\n'
CONTINUE = '    System.out.println("Continue praticando!");\n'

QUESTOES_BLOCOS["F4.Q5"] = dict(
    modo="TRECHO", comparacao="EXATA",
    codigo_inicial="",
    teste=PROGRAMA_F4Q5,
    casos=["xp = 150", "xp = 100 (exatamente o mínimo)", "xp = 99", "xp = 0"],
    exemplo=EXEMPLO_F4Q5,
    exemplos=["xp = 120", "xp = 40"],
    referencia=nivel(SUBIU, CONTINUE),
    certas=[],
    erradas={
        "mensagens trocadas": nivel(CONTINUE, SUBIU),
        "as duas mensagens dentro do if": nivel(SUBIU + CONTINUE, ""),
        "uma mensagem fora do if/else (sempre aparece)": nivel(SUBIU, "", 'System.out.println("Continue praticando!");\n'),
    },
    palavras_chave=[["if"], ["xp >= 100"], ["else"], ["Subiu de nível!"], ["Continue praticando!"]],
    minimo=5,
    resposta_modelo=nivel(SUBIU, CONTINUE).rstrip("\n"),
    enunciado="O Gattuccino precisa exibir “Subiu de nível!” quando o XP for de pelo menos 100 e “Continue "
              "praticando!” nos demais casos. Encaixe os blocos para montar esse trecho:",
    explicacao="A condição xp >= 100 vai no if. Dentro do if fica a mensagem do caso verdadeiro; dentro do "
               "else, a do caso falso. O else não tem condição: ele vale para todos os casos em que a condição "
               "do if deu false. Repare no >=: com exatamente 100 de XP, o Gattuccino já sobe de nível.",
    blocos_iniciais=area([
        {"type": "java_main", "id": "main", "x": 20, "y": 20, "deletable": False},
        {"type": "java_imprimir", "id": "continue", "x": 380, "y": 30, **FIXO,
         "fields": {"METODO": "println"},
         "inputs": {"TEXTO": {"block": {"type": "java_texto", "id": "texto_continue",
                                        "fields": {"TEXTO": "Continue praticando!"}, **PRESO}}}},
        {"type": "java_if_else", "id": "se_senao", "x": 40, "y": 160, "deletable": False},
        {"type": "java_imprimir", "id": "subiu", "x": 380, "y": 110, **FIXO,
         "fields": {"METODO": "println"},
         "inputs": {"TEXTO": {"block": {"type": "java_texto", "id": "texto_subiu",
                                        "fields": {"TEXTO": "Subiu de nível!"}, **PRESO}}}},
        {"type": "java_comparacao", "id": "condicao", "x": 400, "y": 200, **FIXO,
         "fields": {"OP": ">="}, "inputs": {"A": var("xp_na_condicao", "var_xp"), "B": num("cem", 100)}},
    ], variaveis=["xp"]),
    montagem=[("main", "CORPO", "se_senao"), ("se_senao", "COND", "condicao"), ("se_senao", "FAZER", "subiu"),
              ("se_senao", "SENAO", "continue")],
)

# ------------------------------------------------------------------ F9.R2.Q8 (subclasse, blocos de linha)
PROGRAMA_F9R2Q8 = """public class Main {
    public static void main(String[] args) throws Exception {
        BugBlindado casco = new BugBlindado("Casco", 8);
        Bug comoBug = new BugBlindado("Muralha", 3);
        System.out.print("<<<LOGICUP-CASO>>>");
        System.out.println(casco.atacar());
        System.out.print("<<<LOGICUP-CASO>>>");
        System.out.println(casco.getVida());
        System.out.print("<<<LOGICUP-CASO>>>");
        System.out.println(casco.getNome());
        System.out.print("<<<LOGICUP-CASO>>>");
        java.lang.reflect.Field campo = BugBlindado.class.getDeclaredField("escudo");
        campo.setAccessible(true);
        System.out.println(campo.get(casco));
        System.out.print("<<<LOGICUP-CASO>>>");
        System.out.println(comoBug.atacar() + " " + new Bug("Comum", 50).atacar());
    }
}

//@CODIGO_DO_ALUNO
""" + BUG
EXEMPLO_F9R2Q8 = """public class Main {
    public static void main(String[] args) {
        System.out.println("--- Exemplo: Bug casco = new BugBlindado(\\"Casco\\", 8) ---");
        Bug casco = new BugBlindado("Casco", 8);
        System.out.println("atacar(): " + casco.atacar());
        System.out.println("vida: " + casco.getVida());
    }
}

//@CODIGO_DO_ALUNO
""" + BUG


def blindado(atributo=True, construtor=("super(nome, 50);", "this.escudo = escudo;"), metodo=("return 5;",),
             override="@Override", ordem=("atributo", "construtor", "metodo")):
    partes = {
        "atributo": "    private int escudo;\n" if atributo else "",
        "construtor": "    public BugBlindado(String nome, int escudo) {\n"
                      + "".join(f"        {c}\n" for c in construtor) + "    }\n",
        "metodo": (f"    {override}\n" if override else "") + "    public int atacar() {\n"
                  + "".join(f"        {c}\n" for c in metodo) + "    }\n",
    }
    return "public class BugBlindado extends Bug {\n" + "".join(partes[p] for p in ordem) + "}\n"


QUESTOES_BLOCOS["F9.R2.Q8"] = dict(
    modo="CLASSES", comparacao="EXATA",
    codigo_inicial="",
    teste=PROGRAMA_F9R2Q8,
    casos=["new BugBlindado(\"Casco\", 8).atacar()", "vida inicial de new BugBlindado(\"Casco\", 8)",
           "nome de new BugBlindado(\"Casco\", 8)", "o construtor guarda o escudo recebido (8)",
           "Bug b = new BugBlindado(...) ataca com 5, e um Bug comum continua com 10"],
    exemplo=EXEMPLO_F9R2Q8,
    exemplos=["Bug casco = new BugBlindado(\"Casco\", 8)"],
    referencia=blindado(),
    # decisão 4-A: vale qualquer montagem que FUNCIONE. O Java aceita os membros da classe em qualquer ordem,
    # então a convenção (atributo, construtor, método) é recomendada no enunciado, mas não reprova
    certas=[blindado(ordem=("metodo", "atributo", "construtor"))],
    erradas={
        "this.escudo antes do super (não compila)": blindado(construtor=("this.escudo = escudo;", "super(nome, 50);")),
        "this.escudo dentro do atacar (o construtor não guarda o escudo)": blindado(
            construtor=("super(nome, 50);",), metodo=("this.escudo = escudo;", "return 5;")),
        "@Override em cima do atributo (não compila)": blindado(override="").replace(
            "    private int escudo;\n", "    @Override\n    private int escudo;\n"),
        "return 5 dentro do construtor (não compila)": blindado(
            construtor=("super(nome, 50);", "this.escudo = escudo;", "return 5;"), metodo=()),
    },
    palavras_chave=[["extends Bug"], ["private int escudo"], ["super(nome, 50)"], ["this.escudo = escudo"],
                    ["@Override"], ["return 5"]],
    minimo=6,
    resposta_modelo=blindado().rstrip("\n"),
    enunciado="Monte a classe BugBlindado encaixando os blocos. O Bug Blindado começa com 50 de vida, guarda o "
              "escudo que recebe no construtor e, por ser lento, ataca com apenas 5. No construtor, a chamada "
              "super(...) precisa ser a primeira instrução. Use a convenção da trilha: primeiro o atributo, "
              "depois o construtor e, por fim, o método.",
    blocos_iniciais=area([
        raiz("classe", "public class BugBlindado extends Bug {"),
        linha("this_escudo", "this.escudo = escudo;", 470, 20),
        bloco("metodo", "public int atacar() {", 30, 160),
        linha("super", "super(nome, 50);", 470, 90),
        linha("override", "@Override", 260, 160),
        linha("retorno", "return 5;", 470, 160),
        bloco("construtor", "public BugBlindado(String nome, int escudo) {", 30, 300),
        linha("atributo", "private int escudo;", 470, 230),
    ]),
    montagem=[("classe", "CORPO", "atributo"), ("atributo", "next", "construtor"),
              ("construtor", "CORPO", "super"), ("super", "next", "this_escudo"),
              ("construtor", "next", "override"), ("override", "next", "metodo"), ("metodo", "CORPO", "retorno")],
)

# ------------------------------------------------------------------ F10.Q5 (método, blocos de linha)
PROGRAMA_F10Q5 = """import java.util.*;

public class Main {
//@CODIGO_DO_ALUNO

    static ArrayList<Bug> criarHorda(Bug... bugs) {
        return new ArrayList<>(Arrays.asList(bugs));
    }

    public static void main(String[] args) {
        List<ArrayList<Bug>> hordas = new ArrayList<>();
        hordas.add(criarHorda(new Bug("Comum", 50), new BugVeloz("Zip"), new BugVeloz("Flash")));
        hordas.add(criarHorda());
        hordas.add(criarHorda(new Bug("Comum", 50), new BugBlindado("Casco")));
        hordas.add(criarHorda(new BugVeloz("A"), new Bug("B", 50), new BugVeloz("C"), new BugBlindado("D"),
                new BugVeloz("E")));
        for (ArrayList<Bug> horda : hordas) {
            System.out.print("<<<LOGICUP-CASO>>>");
            System.out.println(contarPerigosos(horda));
        }
    }
}
""" + BUG + """
class BugVeloz extends Bug {
    public BugVeloz(String nome) {
        super(nome, 20);
    }

    @Override
    public int atacar() {
        return 15;
    }
}

class BugBlindado extends Bug {
    public BugBlindado(String nome) {
        super(nome, 50);
    }

    @Override
    public int atacar() {
        return 5;
    }
}
"""
EXEMPLO_F10Q5 = PROGRAMA_F10Q5.replace(
    """        for (ArrayList<Bug> horda : hordas) {
            System.out.print("<<<LOGICUP-CASO>>>");
            System.out.println(contarPerigosos(horda));
        }""",
    """        System.out.println("--- Exemplo: Bug comum, BugVeloz e BugVeloz ---");
        System.out.println("perigosos: " + contarPerigosos(hordas.get(0)));""")


def perigosos(dentro_do_for=("if (horda.get(i).atacar() > 10) {", "    perigosos++;", "}"),
              depois_do_for=("return perigosos;",), antes_do_for=("int perigosos = 0;",)):
    antes = "".join(f"    {c}\n" for c in antes_do_for)
    dentro = "".join(f"        {c}\n" for c in dentro_do_for)
    depois = "".join(f"    {c}\n" for c in depois_do_for)
    return ("public static int contarPerigosos(ArrayList<Bug> horda) {\n" + antes
            + "    for (int i = 0; i < horda.size(); i++) {\n" + dentro + "    }\n" + depois + "}\n")


QUESTOES_BLOCOS["F10.Q5"] = dict(
    modo="MEMBRO", comparacao="EXATA",
    codigo_inicial="",
    teste=PROGRAMA_F10Q5,
    casos=["horda: Bug comum, BugVeloz e BugVeloz", "horda vazia", "horda: Bug comum e BugBlindado",
           "horda: 3 BugVeloz, 1 Bug comum e 1 BugBlindado"],
    exemplo=EXEMPLO_F10Q5,
    exemplos=["horda: Bug comum, BugVeloz e BugVeloz"],
    referencia=perigosos(),
    certas=[],
    erradas={
        "perigosos++ fora do if (conta todos)": perigosos(
            dentro_do_for=("if (horda.get(i).atacar() > 10) {", "}", "perigosos++;")),
        "return dentro do for (não compila: falta return no fim)": perigosos(
            dentro_do_for=("if (horda.get(i).atacar() > 10) {", "    perigosos++;", "}", "return perigosos;"),
            depois_do_for=()),
        "contador declarado dentro do for (não compila no return)": perigosos(
            antes_do_for=(), dentro_do_for=("int perigosos = 0;", "if (horda.get(i).atacar() > 10) {",
                                            "    perigosos++;", "}")),
        "if fora do for (o i não existe ali; não compila)": perigosos(
            dentro_do_for=(), depois_do_for=("if (horda.get(i).atacar() > 10) {", "    perigosos++;", "}",
                                             "return perigosos;")),
    },
    palavras_chave=[["int perigosos = 0"], ["for"], ["horda.size()"], ["atacar() > 10"], ["perigosos++"],
                    ["return perigosos"]],
    minimo=6,
    resposta_modelo=perigosos().rstrip("\n"),
    enunciado="Monte o método que conta quantos Bugs da horda atacam com força maior que 10 (lembre-se: cada "
              "subclasse de Bug pode ter a sua versão de atacar(), como na Fase 9). Encaixe os blocos dentro do "
              "método, prestando atenção em quem fica dentro de quem.",
    blocos_iniciais=area([
        raiz("metodo", "public static int contarPerigosos(ArrayList<Bug> horda) {"),
        linha("somar", "perigosos++;", 520, 20),
        bloco("se", "if (horda.get(i).atacar() > 10) {", 30, 160),
        linha("retorno", "return perigosos;", 520, 90),
        bloco("para", "for (int i = 0; i < horda.size(); i++) {", 30, 300),
        linha("contador", "int perigosos = 0;", 520, 160),
    ]),
    montagem=[("metodo", "CORPO", "contador"), ("contador", "next", "para"), ("para", "CORPO", "se"),
              ("se", "CORPO", "somar"), ("para", "next", "retorno")],
)

# montagem da F5.Q8 (só para o teste do gerador, como as de cima)
QUESTOES_BLOCOS["F5.Q8"]["montagem"] = [
    ("main", "CORPO", "declarar"), ("declarar", "next", "para"), ("para", "FAZER", "se"),
    ("se", "FAZER", "somar"), ("para", "next", "imprimir")]
