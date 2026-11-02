"""
Sprint 9 (ADR-001): as 9 questões "Escrever código" que passam a ser corrigidas EXECUTANDO o código.

Para cada questão:
  modo        -> onde o código do aluno entra no programa de teste:
                 TRECHO  = dentro de um método nosso (as variáveis "já declaradas" viram parâmetros)
                 MEMBRO  = dentro da classe Main (o aluno escreve um método)
                 CLASSES = depois da classe Main (o aluno escreve uma ou mais classes)
  comparacao  -> como comparar a saída de cada caso:
                 EXATA         = o texto, linha a linha (ignorando espaços no fim e linhas em branco nas pontas)
                 NUMEROS       = só a sequência de números inteiros (aceita "Missão 2" ou "2")
                 ULTIMO_NUMERO = só o último número exibido (aceita "Total: 3" ou "3")
  teste       -> programa com os casos OCULTOS. Antes de cada caso, imprime a marca <<<LOGICUP-CASO>>>.
  exemplo     -> programa do botão "Executar" (exemplos PÚBLICOS, sem marcas, mostra a saída "crua").
  casos       -> a descrição de cada caso oculto (só aparece para o aluno o PRIMEIRO caso que falhar).
  exemplos    -> a descrição dos exemplos públicos (aparecem na tela).
  codigo_inicial -> o que o editor mostra quando o aluno abre a questão.
  referencia  -> solução usada para GERAR as saídas esperadas (precisa compilar; a respostaModelo
                 do banco às vezes tem duas soluções com "ou" no meio, então não serve para isso).
  certas      -> outras soluções corretas que PRECISAM passar (validação do gerador).
  erradas     -> erros típicos que PRECISAM reprovar (validação do gerador).

O marcador //@CODIGO_DO_ALUNO é onde o código do aluno é colado.
Os programas usam só recursos do Java 15 (versão do Piston).
"""

MARCA = "<<<LOGICUP-CASO>>>"
SILENCIO = "new java.io.PrintStream(java.io.OutputStream.nullOutputStream())"

# Classe Item usada nas questões da Fase 10 (o enunciado diz que ela "já existe").
ITEM = """
class Item {
    private String nome;
    private int valor;

    public Item(String nome, int valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public int getValor() {
        return valor;
    }
}
"""

QUESTOES = {
    # ------------------------------------------------------------------ F4.Q9
    "F4.Q9": dict(
        modo="TRECHO", comparacao="EXATA",
        codigo_inicial="// A variável energia já existe. Escreva aqui o if / else if / else:\n",
        teste="""import java.util.*;

public class Main {
    static void executar(int energia) {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        int[] valores = {0, 1, 3, 4, 5, 7, 10};
        for (int valor : valores) {
            System.out.print("<<<LOGICUP-CASO>>>");
            executar(valor);
        }
    }
}
""",
        casos=["energia = 0", "energia = 1", "energia = 3", "energia = 4", "energia = 5", "energia = 7", "energia = 10"],
        exemplo="""import java.util.*;

public class Main {
    static void executar(int energia) {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        System.out.println("--- Exemplo: energia = 0 ---");
        executar(0);
        System.out.println("--- Exemplo: energia = 7 ---");
        executar(7);
    }
}
""",
        exemplos=["energia = 0", "energia = 7"],
        referencia="""if (energia == 0) {
    System.out.println("Exausto: hora do café!");
} else if (energia < 5) {
    System.out.println("Energia baixa");
} else {
    System.out.println("Pronto para a Arena!");
}""",
        certas=[
            # faixa completa, como a explicação do banco diz que também é correto
            """if (energia == 0) {
    System.out.println("Exausto: hora do café!");
} else if (energia >= 1 && energia <= 4) {
    System.out.println("Energia baixa");
} else {
    System.out.println("Pronto para a Arena!");
}""",
            # ordem diferente das condições
            """if (energia >= 5) System.out.println("Pronto para a Arena!");
else if (energia > 0) System.out.println("Energia baixa");
else System.out.println("Exausto: hora do café!");""",
        ],
        erradas={
            "limite errado (< 4)": """if (energia == 0) {
    System.out.println("Exausto: hora do café!");
} else if (energia < 4) {
    System.out.println("Energia baixa");
} else {
    System.out.println("Pronto para a Arena!");
}""",
            "ifs separados (imprime duas mensagens)": """if (energia == 0) System.out.println("Exausto: hora do café!");
if (energia < 5) System.out.println("Energia baixa");
else System.out.println("Pronto para a Arena!");""",
            "redeclara a variável": """int energia = 5;
System.out.println("Pronto para a Arena!");""",
        },
    ),

    # ------------------------------------------------------------------ F5.Q9
    "F5.Q9": dict(
        modo="TRECHO", comparacao="NUMEROS",
        codigo_inicial="// Escreva aqui o laço que exibe as missões bônus:\n",
        teste="""import java.util.*;

public class Main {
    static void executar() {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        System.out.print("<<<LOGICUP-CASO>>>");
        executar();
    }
}
""",
        casos=["missões bônus de 2 a 20"],
        exemplo="""import java.util.*;

public class Main {
    static void executar() {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        executar();
    }
}
""",
        exemplos=["exibe as missões bônus"],
        referencia="""for (int missao = 2; missao <= 20; missao += 2) {
    System.out.println(missao);
}""",
        certas=[
            """for (int missao = 1; missao <= 20; missao++) {
    if (missao % 2 == 0) {
        System.out.println(missao);
    }
}""",
            """int m = 2;
while (m <= 20) {
    System.out.println("Missão " + m);
    m = m + 2;
}""",
        ],
        erradas={
            "esquece a missão 20 (< 20)": """for (int missao = 2; missao < 20; missao += 2) {
    System.out.println(missao);
}""",
            "exibe os ímpares": """for (int missao = 1; missao <= 20; missao += 2) {
    System.out.println(missao);
}""",
        },
    ),

    # ------------------------------------------------------------------ F6.Q9
    "F6.Q9": dict(
        modo="TRECHO", comparacao="ULTIMO_NUMERO",
        codigo_inicial="// O vetor pontos já existe. Conte as missões com pontuação >= 7 e exiba o total:\n",
        teste="""import java.util.*;

public class Main {
    static void executar(int[] pontos) {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        int[][] casos = {{7, 3, 10, 6, 7}, {}, {6, 6, 6}, {9}, {7, 7, 7, 7}, {10, 0, 8, 6}};
        for (int[] pontos : casos) {
            System.out.print("<<<LOGICUP-CASO>>>");
            executar(pontos);
        }
    }
}
""",
        casos=["pontos = {7, 3, 10, 6, 7}", "pontos = {} (nenhuma missão)", "pontos = {6, 6, 6}",
               "pontos = {9}", "pontos = {7, 7, 7, 7}", "pontos = {10, 0, 8, 6}"],
        exemplo="""import java.util.*;

public class Main {
    static void executar(int[] pontos) {
//@CODIGO_DO_ALUNO
    }

    public static void main(String[] args) {
        System.out.println("--- Exemplo: pontos = {7, 3, 10, 6, 7} ---");
        executar(new int[]{7, 3, 10, 6, 7});
    }
}
""",
        exemplos=["pontos = {7, 3, 10, 6, 7}"],
        referencia="""int quantidade = 0;
for (int i = 0; i < pontos.length; i++) {
    if (pontos[i] >= 7) {
        quantidade++;
    }
}
System.out.println(quantidade);""",
        certas=[
            """int total = 0;
for (int p : pontos) {
    if (p > 6) total += 1;
}
System.out.println("Missões com nota alta: " + total);""",
        ],
        erradas={
            "usa > 7 (exclui o 7)": """int q = 0;
for (int i = 0; i < pontos.length; i++) {
    if (pontos[i] > 7) q++;
}
System.out.println(q);""",
            "println dentro do laço": """int q = 0;
for (int i = 0; i < pontos.length; i++) {
    if (pontos[i] >= 7) q++;
    System.out.println(q);
}""",
            "laço até <= length (estoura o vetor)": """int q = 0;
for (int i = 0; i <= pontos.length; i++) {
    if (pontos[i] >= 7) q++;
}
System.out.println(q);""",
        },
    ),

    # ------------------------------------------------------------------ F7.Q9
    "F7.Q9": dict(
        modo="MEMBRO", comparacao="EXATA",
        codigo_inicial="// Escreva aqui o método maiorDano:\n",
        teste="""import java.util.*;

public class Main {
//@CODIGO_DO_ALUNO

    public static void main(String[] args) {
        java.io.PrintStream logicupSaida = System.out;
        Main logicupObjeto = new Main();
        int[][] casos = {{3, 9, 2}, {5}, {-4, -2, -9}, {30, 1, 2}, {1, 2, 30}, {7, 7}};
        for (int[] danos : casos) {
            logicupSaida.print("<<<LOGICUP-CASO>>>");
            System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
            var resultado = logicupObjeto.maiorDano(danos);
            System.setOut(logicupSaida);
            System.out.println(resultado);
        }
    }
}
""",
        casos=["maiorDano({3, 9, 2})", "maiorDano({5})", "maiorDano({-4, -2, -9}) (só valores negativos)",
               "maiorDano({30, 1, 2}) (maior no início)", "maiorDano({1, 2, 30}) (maior no fim)", "maiorDano({7, 7})"],
        exemplo="""import java.util.*;

public class Main {
//@CODIGO_DO_ALUNO

    public static void main(String[] args) {
        System.out.println("--- Exemplo: maiorDano({3, 9, 2}) ---");
        System.out.println("Valor devolvido: " + new Main().maiorDano(new int[]{3, 9, 2}));
    }
}
""",
        exemplos=["maiorDano({3, 9, 2})"],
        referencia="""public static int maiorDano(int[] danos) {
    int maior = danos[0];
    for (int i = 1; i < danos.length; i++) {
        if (danos[i] > maior) {
            maior = danos[i];
        }
    }
    return maior;
}""",
        certas=[
            # sem static: o programa de teste chama pelo objeto, então também funciona
            """int maiorDano(int[] danos) {
    int maior = danos[0];
    for (int d : danos) if (d > maior) maior = d;
    return maior;
}""",
            # com println de depuração dentro do método: é ignorado (só o valor devolvido conta)
            """public static int maiorDano(int[] danos) {
    int maior = danos[0];
    for (int i = 0; i < danos.length; i++) {
        System.out.println("olhando " + danos[i]);
        if (danos[i] > maior) maior = danos[i];
    }
    return maior;
}""",
        ],
        erradas={
            "começa o maior em 0 (falha com negativos)": """public static int maiorDano(int[] danos) {
    int maior = 0;
    for (int i = 0; i < danos.length; i++) {
        if (danos[i] > maior) maior = danos[i];
    }
    return maior;
}""",
            "exibe em vez de devolver": """public static void maiorDano(int[] danos) {
    int maior = danos[0];
    for (int d : danos) if (d > maior) maior = d;
    System.out.println(maior);
}""",
            "devolve o último": """public static int maiorDano(int[] danos) {
    return danos[danos.length - 1];
}""",
        },
    ),

    # ------------------------------------------------------------------ F8.Q9
    "F8.Q9": dict(
        modo="MEMBRO", comparacao="EXATA",
        codigo_inicial="import java.util.ArrayList;\n\n// Escreva aqui o método golpesCriticos:\n",
        teste="""import java.util.*;

public class Main {
//@CODIGO_DO_ALUNO

    public static void main(String[] args) {
        java.io.PrintStream logicupSaida = System.out;
        Main logicupObjeto = new Main();
        List<List<Integer>> casos = List.of(List.of(10, 50, 41, 40, 99), List.of(), List.of(40, 40),
                List.of(41), List.of(100, 20, 60));
        for (List<Integer> caso : casos) {
            logicupSaida.print("<<<LOGICUP-CASO>>>");
            System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
            var resultado = logicupObjeto.golpesCriticos(new ArrayList<>(caso));
            System.setOut(logicupSaida);
            System.out.println(resultado);
        }
    }
}
""",
        casos=["golpesCriticos([10, 50, 41, 40, 99])", "golpesCriticos([]) (lista vazia)",
               "golpesCriticos([40, 40]) (exatamente 40 não é crítico)", "golpesCriticos([41])",
               "golpesCriticos([100, 20, 60])"],
        exemplo="""import java.util.*;

public class Main {
//@CODIGO_DO_ALUNO

    public static void main(String[] args) {
        System.out.println("--- Exemplo: golpesCriticos([10, 50, 41, 40, 99]) ---");
        System.out.println("Valor devolvido: "
                + new Main().golpesCriticos(new ArrayList<>(List.of(10, 50, 41, 40, 99))));
    }
}
""",
        exemplos=["golpesCriticos([10, 50, 41, 40, 99])"],
        referencia="""public static ArrayList<Integer> golpesCriticos(ArrayList<Integer> danos) {
    ArrayList<Integer> criticos = new ArrayList<>();
    for (int i = 0; i < danos.size(); i++) {
        if (danos.get(i) > 40) {
            criticos.add(danos.get(i));
        }
    }
    return criticos;
}""",
        certas=[
            """import java.util.ArrayList;

public static ArrayList<Integer> golpesCriticos(ArrayList<Integer> danos) {
    ArrayList<Integer> criticos = new ArrayList<Integer>();
    for (Integer d : danos) {
        if (d >= 41) criticos.add(d);
    }
    return criticos;
}""",
        ],
        erradas={
            "usa >= 40": """public static ArrayList<Integer> golpesCriticos(ArrayList<Integer> danos) {
    ArrayList<Integer> c = new ArrayList<>();
    for (int i = 0; i < danos.size(); i++) if (danos.get(i) >= 40) c.add(danos.get(i));
    return c;
}""",
            "devolve a lista original": """public static ArrayList<Integer> golpesCriticos(ArrayList<Integer> danos) {
    return danos;
}""",
        },
    ),

    # ------------------------------------------------------------------ F9.R1.Q9
    "F9.R1.Q9": dict(
        modo="CLASSES", comparacao="EXATA",
        codigo_inicial="// Escreva aqui a classe Bau:\n",
        teste="""import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        java.io.PrintStream logicupSaida = System.out;
        int[] valores = {30, 0, 125};
        for (int valor : valores) {
            logicupSaida.print("<<<LOGICUP-CASO>>>");
            System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
            Bau bau = new Bau(valor);
            var resultado = bau.getGemas();
            System.setOut(logicupSaida);
            System.out.println(resultado);
        }
        logicupSaida.print("<<<LOGICUP-CASO>>>");
        java.lang.reflect.Field atributo = Bau.class.getDeclaredField("gemas");
        String visibilidade = java.lang.reflect.Modifier.isPrivate(atributo.getModifiers()) ? "private" : "NÃO private";
        System.out.println(visibilidade + " " + atributo.getType().getName() + " gemas");
    }
}

//@CODIGO_DO_ALUNO
""",
        casos=["new Bau(30).getGemas()", "new Bau(0).getGemas()", "new Bau(125).getGemas()",
               "o atributo gemas é private e do tipo int"],
        exemplo="""import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Exemplo: new Bau(30).getGemas() ---");
        Bau bau = new Bau(30);
        System.out.println("Valor devolvido: " + bau.getGemas());
    }
}

//@CODIGO_DO_ALUNO
""",
        exemplos=["new Bau(30).getGemas()"],
        referencia="""public class Bau {
    private int gemas;

    public Bau(int gemas) {
        this.gemas = gemas;
    }

    public int getGemas() {
        return gemas;
    }
}""",
        certas=[
            """class Bau {
    private int gemas;
    Bau(int quantidadeInicial) { gemas = quantidadeInicial; }
    int getGemas() { return this.gemas; }
}""",
        ],
        erradas={
            "atributo público": """public class Bau {
    public int gemas;
    public Bau(int gemas) { this.gemas = gemas; }
    public int getGemas() { return gemas; }
}""",
            "esquece o this (parâmetro com o mesmo nome)": """public class Bau {
    private int gemas;
    public Bau(int gemas) { gemas = gemas; }
    public int getGemas() { return gemas; }
}""",
        },
    ),

    # ------------------------------------------------------------------ F9.R2.Q9
    "F9.R2.Q9": dict(
        modo="CLASSES", comparacao="EXATA",
        codigo_inicial="// A classe Bug já existe. Escreva aqui a classe BugVeloz:\n",
        teste="""import java.util.*;

public class Main {
    public static void main(String[] args) {
        java.io.PrintStream logicupSaida = System.out;
        System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
        Bug zip = new BugVeloz("Zip");
        Bug flash = new BugVeloz("Flash");
        System.setOut(logicupSaida);

        logicupSaida.print("<<<LOGICUP-CASO>>>");
        System.out.println(zip.atacar());
        logicupSaida.print("<<<LOGICUP-CASO>>>");
        System.out.println(zip.getVida());
        logicupSaida.print("<<<LOGICUP-CASO>>>");
        System.out.println(zip.getNome());
        logicupSaida.print("<<<LOGICUP-CASO>>>");
        System.out.println(flash.getNome() + " " + flash.getVida());
        logicupSaida.print("<<<LOGICUP-CASO>>>");
        System.out.println(new Bug("Comum", 50).atacar());
    }
}

//@CODIGO_DO_ALUNO

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
""",
        casos=["Bug zip = new BugVeloz(\"Zip\"); zip.atacar()", "vida inicial de new BugVeloz(\"Zip\")",
               "nome de new BugVeloz(\"Zip\")", "nome e vida de new BugVeloz(\"Flash\")",
               "um Bug comum continua atacando com 10"],
        exemplo="""import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Exemplo: Bug zip = new BugVeloz(\\"Zip\\") ---");
        Bug zip = new BugVeloz("Zip");
        System.out.println("atacar(): " + zip.atacar());
        System.out.println("vida: " + zip.getVida());
    }
}

//@CODIGO_DO_ALUNO

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
""",
        exemplos=["Bug zip = new BugVeloz(\"Zip\")"],
        referencia="""public class BugVeloz extends Bug {
    public BugVeloz(String nome) {
        super(nome, 20);
    }

    @Override
    public int atacar() {
        return 15;
    }
}""",
        certas=[
            """class BugVeloz extends Bug {
    BugVeloz(String n) { super(n, 20); }
    public int atacar() { return 15; }
}""",
        ],
        erradas={
            "vida errada": """public class BugVeloz extends Bug {
    public BugVeloz(String nome) { super(nome, 10); }
    @Override public int atacar() { return 15; }
}""",
            "não sobrescreve atacar": """public class BugVeloz extends Bug {
    public BugVeloz(String nome) { super(nome, 20); }
}""",
        },
    ),

    # ------------------------------------------------------------------ F10.Q8
    "F10.Q8": dict(
        modo="MEMBRO", comparacao="EXATA",
        codigo_inicial="import java.util.ArrayList;\n\n// A classe Item já existe. Escreva aqui o método valorTotal:\n",
        teste="""import java.util.*;

public class Main {
//@CODIGO_DO_ALUNO

    public static void main(String[] args) {
        java.io.PrintStream logicupSaida = System.out;
        Main logicupObjeto = new Main();
        List<List<Item>> casos = List.of(
                List.of(new Item("Espada", 10), new Item("Escudo", 20), new Item("Poção", 30)),
                List.of(),
                List.of(new Item("Moeda", 7)),
                List.of(new Item("Pedra", 10), new LogicupItemDobrado("Coroa", 20)));
        for (List<Item> caso : casos) {
            logicupSaida.print("<<<LOGICUP-CASO>>>");
            System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
            var resultado = logicupObjeto.valorTotal(new ArrayList<>(caso));
            System.setOut(logicupSaida);
            System.out.println(resultado);
        }
    }
}

class Item {
    private String nome;
    private int valor;

    public Item(String nome, int valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public int getValor() {
        return valor;
    }
}

class LogicupItemDobrado extends Item {
    LogicupItemDobrado(String nome, int valor) {
        super(nome, valor);
    }

    @Override
    public int getValor() {
        return super.getValor() * 2;
    }
}
""",
        casos=["itens com valores 10, 20 e 30", "lista vazia", "um item com valor 7",
               "um item comum (10) e um item raro (20, que vale o dobro)"],
        exemplo="""import java.util.*;

public class Main {
//@CODIGO_DO_ALUNO

    public static void main(String[] args) {
        System.out.println("--- Exemplo: itens com valores 10, 20 e 30 ---");
        ArrayList<Item> itens = new ArrayList<>(List.of(new Item("Espada", 10), new Item("Escudo", 20),
                new Item("Poção", 30)));
        System.out.println("Valor devolvido: " + new Main().valorTotal(itens));
    }
}

class Item {
    private String nome;
    private int valor;

    public Item(String nome, int valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public int getValor() {
        return valor;
    }
}
""",
        exemplos=["itens com valores 10, 20 e 30"],
        referencia="""public static int valorTotal(ArrayList<Item> itens) {
    int total = 0;
    for (int i = 0; i < itens.size(); i++) {
        total += itens.get(i).getValor();
    }
    return total;
}""",
        certas=[
            """static int valorTotal(ArrayList<Item> itens) {
    int soma = 0;
    for (Item item : itens) soma = soma + item.getValor();
    return soma;
}""",
        ],
        erradas={
            "esquece o primeiro item": """public static int valorTotal(ArrayList<Item> itens) {
    int total = 0;
    for (int i = 1; i < itens.size(); i++) total += itens.get(i).getValor();
    return total;
}""",
            "conta os itens em vez de somar": """public static int valorTotal(ArrayList<Item> itens) {
    return itens.size();
}""",
        },
    ),

    # ------------------------------------------------------------------ F10.Q9
    "F10.Q9": dict(
        modo="CLASSES", comparacao="EXATA",
        codigo_inicial="// A classe Item já existe. Escreva aqui a classe ItemRaro:\n",
        teste="""import java.util.*;

public class Main {
    public static void main(String[] args) {
        java.io.PrintStream logicupSaida = System.out;
        Object[][] casos = {{"Coroa", 50}, {"Anel", 0}, {"Gema", 7}};
        for (Object[] caso : casos) {
            logicupSaida.print("<<<LOGICUP-CASO>>>");
            System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
            Item item = new ItemRaro((String) caso[0], (Integer) caso[1]);
            var resultado = item.getValor();
            System.setOut(logicupSaida);
            System.out.println(resultado);
        }
        logicupSaida.print("<<<LOGICUP-CASO>>>");
        System.out.println(new ItemRaro("Coroa", 50).getNome());
        logicupSaida.print("<<<LOGICUP-CASO>>>");
        System.out.println(new Item("Pedra", 50).getValor());
    }
}

//@CODIGO_DO_ALUNO
""" + ITEM,
        casos=["Item coroa = new ItemRaro(\"Coroa\", 50); coroa.getValor()", "new ItemRaro(\"Anel\", 0).getValor()",
               "new ItemRaro(\"Gema\", 7).getValor()", "o nome é repassado para Item",
               "um Item comum continua valendo o valor original"],
        exemplo="""import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Exemplo: Item coroa = new ItemRaro(\\"Coroa\\", 50) ---");
        Item coroa = new ItemRaro("Coroa", 50);
        System.out.println("getValor(): " + coroa.getValor());
    }
}

//@CODIGO_DO_ALUNO
""" + ITEM,
        exemplos=["Item coroa = new ItemRaro(\"Coroa\", 50)"],
        referencia="""public class ItemRaro extends Item {
    public ItemRaro(String nome, int valor) {
        super(nome, valor);
    }

    @Override
    public int getValor() {
        return super.getValor() * 2;
    }
}""",
        certas=[
            """class ItemRaro extends Item {
    ItemRaro(String nome, int valor) { super(nome, valor); }
    public int getValor() { return 2 * super.getValor(); }
}""",
        ],
        erradas={
            "chama getValor() sem super (estoura a pilha)": """public class ItemRaro extends Item {
    public ItemRaro(String nome, int valor) { super(nome, valor); }
    @Override public int getValor() { return getValor() * 2; }
}""",
            "não dobra": """public class ItemRaro extends Item {
    public ItemRaro(String nome, int valor) { super(nome, valor); }
}""",
        },
    ),
}
