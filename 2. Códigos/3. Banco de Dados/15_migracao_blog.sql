-- =====================================================================
-- LogiCup — Migração 15 (Sprint 11, 17/11/2026): BLOG com dicas de programação.
--
-- O QUE É: textos curtos sobre lógica e Java, SEMPRE embasados em livros e autores reconhecidos
--   (cada post tem a lista de referências, obrigatória). O admin escreve, edita e publica pela área do
--   admin; qualquer pessoa (mesmo sem conta) lê os posts PUBLICADOS em /blog.
--
-- TABELA NOVA: post_blog
--   publicado_em NULL = rascunho (só o admin vê). Guardar a DATA, e não só sim/não, já diz quando saiu.
--   slug = o pedaço da URL (/blog/<slug>): único, só letras minúsculas, números e hífen.
--   conteudo = texto com uma marcação simples (## subtítulo, - lista, ``` bloco de código, `código`, **negrito**).
--     O site NUNCA interpreta HTML vindo daqui: o texto é desenhado como texto (sem risco de XSS).
--   Sem ligação com a tabela usuario: os posts são assinados pela "Equipe LogiCup"; quem editou fica no log.
--
-- CONTEÚDO: 5 RASCUNHOS (não publicados), para a equipe revisar antes de publicar.
--
-- O que NÃO muda: nenhuma tabela existente.
-- Também serve para instalação nova (CREATE TABLE IF NOT EXISTS; os rascunhos só entram com a tabela vazia).
-- ATENÇÃO: CREATE TABLE é DDL (sem ROLLBACK). Para voltar: 15_reverter_blog.sql.
-- =====================================================================
USE Logicup;
-- acentos: o arquivo é UTF-8; sem isto, um cliente configurado em latin1 gravaria "Ã©" no lugar de "é"
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `post_blog` (
  `id_post` int NOT NULL AUTO_INCREMENT,
  `slug` varchar(120) NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `resumo` varchar(300) NOT NULL,
  `tag` varchar(40) NOT NULL,
  `conteudo` mediumtext NOT NULL,
  `referencias` text NOT NULL COMMENT 'uma referência (ABNT) por linha; obrigatória',
  `publicado_em` datetime DEFAULT NULL COMMENT 'NULL = rascunho',
  `criado_em` datetime NOT NULL,
  `atualizado_em` datetime NOT NULL,
  PRIMARY KEY (`id_post`),
  UNIQUE KEY `uk_post_blog_slug` (`slug`),
  KEY `idx_post_blog_publicado` (`publicado_em`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- os 5 rascunhos. Rodar de novo não duplica nem sobrescreve: o slug é único e, se já existir,
-- o ON DUPLICATE KEY UPDATE não muda nada (id_post = id_post). Edições feitas pelo admin ficam como estão.
INSERT INTO post_blog (slug, titulo, resumo, tag, conteudo, referencias, publicado_em, criado_em, atualizado_em) VALUES
  ('algoritmo-e-a-ordem-dos-passos', 'Algoritmo é receita: por que a ordem dos passos importa', 'Um algoritmo é uma sequência finita de passos bem definidos. Veja, com um exemplo em Java, como trocar dois passos de lugar muda o resultado.', 'Lógica', 'Todo programa começa antes do código: começa com a ideia de como resolver o problema, passo a passo. Essa sequência de passos é o que chamamos de **algoritmo**.

## O que é um algoritmo

Um algoritmo é uma sequência **finita** de passos **bem definidos** que resolve um problema. Finita porque precisa terminar. Bem definidos porque cada passo tem que ser claro o bastante para ser seguido sem adivinhação, seja por uma pessoa, seja por um computador.

Uma receita de bolo é o exemplo clássico: tem os ingredientes (a entrada), o modo de preparo (os passos) e o bolo pronto (a saída). E, como em qualquer receita, a ordem importa: não dá para assar antes de misturar.

## Quando a ordem muda o resultado

Imagine que você quer trocar os valores de duas variáveis: `a` deve ficar com o valor de `b`, e `b` com o valor de `a`. Uma primeira tentativa costuma ser assim:

```java
int a = 5;
int b = 8;

a = b; // a passa a valer 8
b = a; // b recebe 8 também: o 5 se perdeu!
```

O problema não é a sintaxe: o programa compila e roda. O problema é o **algoritmo**. Na primeira atribuição, o valor antigo de `a` é sobrescrito e some. A solução é guardar esse valor antes, numa variável auxiliar:

```java
int a = 5;
int b = 8;

int aux = a; // guarda o 5
a = b;       // a = 8
b = aux;     // b = 5
```

## Pense antes de digitar

Uma boa prática é escrever os passos em português antes de escrever em Java. Livros de lógica de programação, como o de Forbellone e Eberspächer, usam justamente o **pseudocódigo** para isso: uma forma de descrever o algoritmo sem se preocupar com os detalhes de uma linguagem.

Essa forma de pensar, quebrando um problema em passos que um computador consegue executar, é chamada de **pensamento computacional**. Jeannette Wing popularizou o termo em 2006 e defendeu que ele é uma habilidade útil para todo mundo, e não só para quem trabalha com computação.

## Para treinar

- Escreva, em português, os passos para descobrir o maior de três números.
- Depois, troque a ordem de dois passos e veja se o resultado continua certo.
- Só então passe para o Java.', 'FORBELLONE, André Luiz Villar; EBERSPÄCHER, Henri Frederico. Lógica de programação: a construção de algoritmos e estruturas de dados. 3. ed. São Paulo: Pearson Prentice Hall, 2005.
WING, Jeannette M. Computational thinking. Communications of the ACM, v. 49, n. 3, p. 33-35, 2006.', NULL, NOW(), NOW()),
  ('nomes-que-explicam', 'Nomes que explicam: variáveis que dispensam comentários', 'Um bom nome de variável conta para que ela serve. Veja como nomes claros deixam o código mais fácil de ler, de corrigir e de explicar.', 'Boas práticas', 'Quando você está aprendendo, é comum dar nomes curtos às variáveis: `x`, `n`, `aux2`. Funciona, mas cobra um preço depois: daqui a uma semana, nem você vai lembrar o que `x` guardava.

## O nome é a primeira documentação

Compare estas duas linhas:

```java
int d = 7;
int diasSemAcessar = 7;
```

As duas guardam o mesmo valor. Mas só a segunda diz **o que** esse valor significa. Quem lê não precisa procurar um comentário nem adivinhar.

Em **Código Limpo**, Robert C. Martin dedica um capítulo inteiro aos nomes. A ideia central é simples: o nome de uma variável, de um método ou de uma classe deve revelar a intenção de quem escreveu. Se você precisa de um comentário para explicar o que uma variável é, provavelmente o nome pode melhorar.

## Algumas dicas práticas

- Prefira palavras inteiras a abreviações: `quantidadeDeAlunos` em vez de `qtdAl`.
- Use nomes que dê para pronunciar: fica mais fácil conversar sobre o código com a equipe.
- Evite nomes que enganam: uma variável chamada `listaDeNotas` que guarda um único número confunde quem lê.
- Para valores verdadeiro/falso (`boolean`), nomes como `estaAprovado` ou `temDesconto` leem como uma pergunta.

## As convenções do Java

Além de escolher bem as palavras, o Java tem convenções de escrita que todo programador segue, e que livros como o dos Deitel usam em todos os exemplos:

- variáveis e métodos em **camelCase**, começando com letra minúscula: `notaFinal`, `calcularMedia()`;
- classes começando com letra maiúscula: `Aluno`, `ContaBancaria`;
- constantes em letras maiúsculas, separadas por `_`: `NOTA_MINIMA`.

```java
final double NOTA_MINIMA = 6.0;
double notaFinal = 7.5;
boolean estaAprovado = notaFinal >= NOTA_MINIMA;
```

Repare como dá para ler a última linha quase como uma frase: "está aprovado se a nota final for maior ou igual à nota mínima".

## Para treinar

Pegue um exercício antigo seu e renomeie as variáveis. Se o código ficar mais fácil de entender sem nenhum comentário, você acertou.', 'MARTIN, Robert C. Código limpo: habilidades práticas do Agile Software. Rio de Janeiro: Alta Books, 2009.
DEITEL, Paul; DEITEL, Harvey. Java: como programar. 10. ed. São Paulo: Pearson Education do Brasil, 2017.', NULL, NOW(), NOW()),
  ('igual-igual-ou-equals', '== ou equals? Como comparar textos em Java', 'Por que comparar duas Strings com == às vezes funciona e às vezes não? Entenda a diferença entre comparar referências e comparar conteúdo.', 'Java', 'Esse é um dos erros mais comuns de quem começa em Java: comparar textos com `==`. O pior é que, às vezes, funciona, e isso faz o erro passar despercebido.

## Tipos primitivos e objetos

Em Java, `int`, `double`, `char` e `boolean` são **tipos primitivos**: a variável guarda o próprio valor. Com eles, `==` compara os valores, e está tudo certo:

```java
int a = 10;
int b = 10;
System.out.println(a == b); // true
```

Já `String` não é um tipo primitivo: é uma **classe**, e cada texto é um **objeto**. A variável não guarda o texto em si, e sim uma **referência**, algo como o endereço de onde o objeto está na memória.

## O que o == faz com objetos

Com objetos, `==` compara as **referências**: responde se as duas variáveis apontam para o **mesmo objeto**. Não olha o conteúdo.

```java
String nome1 = new String("Java");
String nome2 = new String("Java");

System.out.println(nome1 == nome2);      // false: são dois objetos diferentes
System.out.println(nome1.equals(nome2)); // true: o conteúdo é igual
```

O método `equals` compara o **conteúdo** dos textos, letra por letra. É ele que você quer quase sempre.

## Por que às vezes funciona

Quando você escreve o mesmo texto entre aspas duas vezes no código (`"Java"` e `"Java"`), o Java pode reaproveitar o mesmo objeto para os dois. Aí o `==` dá `true` por coincidência. Mas basta o texto vir de outro lugar, como o que o usuário digitou num `Scanner`, para o resultado mudar:

```java
Scanner entrada = new Scanner(System.in);
String resposta = entrada.nextLine(); // o usuário digita: sim

if (resposta == "sim") {      // provavelmente false!
    System.out.println("Confirmado");
}
if (resposta.equals("sim")) { // true
    System.out.println("Confirmado");
}
```

## Resumindo

- Tipos primitivos (`int`, `double`...): compare com `==`.
- Objetos, incluindo `String`: compare o conteúdo com `equals`.
- Para ignorar maiúsculas e minúsculas, existe `equalsIgnoreCase`.

Essa diferença entre o valor e a referência aparece em vários livros de introdução ao Java, como **Java: como programar**, dos Deitel, e **Use a Cabeça! Java**, de Sierra e Bates. Entender isso agora ajuda muito quando você começar a criar as suas próprias classes.', 'DEITEL, Paul; DEITEL, Harvey. Java: como programar. 10. ed. São Paulo: Pearson Education do Brasil, 2017.
SIERRA, Kathy; BATES, Bert. Use a cabeça! Java. 2. ed. Rio de Janeiro: Alta Books, 2009.', NULL, NOW(), NOW()),
  ('por-que-7-dividido-por-2-da-3', 'Por que 7 / 2 dá 3 em Java?', 'Divisão entre inteiros em Java descarta a parte decimal. Veja quando isso acontece, como usar o resto (%) a seu favor e como obter 3.5.', 'Java', 'Se você já escreveu `System.out.println(7 / 2);` esperando ver `3.5` e apareceu `3`, não é defeito do Java. É uma regra da linguagem, e entender essa regra evita vários bugs.

## Divisão inteira

Quando os **dois** lados de uma divisão são inteiros (`int` ou `long`), o resultado também é inteiro. A parte decimal é simplesmente **descartada**, sem arredondar:

```java
System.out.println(7 / 2);  // 3
System.out.println(9 / 10); // 0
System.out.println(-7 / 2); // -3 (descarta o decimal: vai em direção ao zero)
```

Repare no `9 / 10`: o resultado "matemático" seria 0,9, mas como a parte decimal é descartada, sobra 0.

## O resto da divisão: %

A parte que "sobrou" da divisão inteira pode ser obtida com o operador `%`, chamado de **resto** (ou módulo):

```java
System.out.println(7 % 2);  // 1
System.out.println(10 % 2); // 0
```

Isso é muito útil: um número é **par** quando o resto da divisão por 2 é zero.

```java
int numero = 7;
if (numero % 2 == 0) {
    System.out.println("Par");
} else {
    System.out.println("Ímpar");
}
```

## Como obter 3.5

Para a divisão ter casas decimais, pelo menos um dos lados precisa ser de ponto flutuante (`double`):

```java
System.out.println(7.0 / 2);         // 3.5
System.out.println((double) 7 / 2);  // 3.5: o 7 vira double antes de dividir
```

Cuidado com a ordem, porque a conversão precisa acontecer **antes** da divisão:

```java
System.out.println((double) (7 / 2)); // 3.0: a divisão inteira já aconteceu e deu 3
```

## Um bug clássico: a média

```java
int soma = 15;
int quantidade = 2;
double media = soma / quantidade; // 7.0, e não 7.5!
```

Guardar o resultado numa variável `double` não resolve: a divisão entre dois `int` acontece primeiro, e só depois o 7 é convertido para 7.0. A correção é converter antes: `(double) soma / quantidade`.

Esse comportamento dos operadores aritméticos é explicado logo nos primeiros capítulos de **Java: como programar**, dos Deitel, junto com a precedência dos operadores.', 'DEITEL, Paul; DEITEL, Harvey. Java: como programar. 10. ed. São Paulo: Pearson Education do Brasil, 2017.', NULL, NOW(), NOW()),
  ('encapsulamento-atributos-private', 'Encapsulamento: por que os atributos são private', 'Deixar os atributos private e expor métodos protege os dados de um objeto. Veja um exemplo com uma conta bancária que não aceita saldo inválido.', 'Orientação a objetos', 'Quando você começa a criar classes, uma pergunta aparece logo: por que quase todo exemplo deixa os atributos `private` e cria métodos para acessá-los? Não seria mais simples deixar tudo `public`?

## O problema de deixar tudo aberto

Veja esta classe:

```java
public class ContaBancaria {
    public double saldo;
}
```

Qualquer parte do programa pode fazer isto:

```java
ContaBancaria conta = new ContaBancaria();
conta.saldo = -1000; // ninguém impediu!
```

Nada garante que a conta continue fazendo sentido. Se esse valor estiver errado, o bug pode ter vindo de qualquer lugar do código que mexe em `saldo`.

## Encapsular: esconder os dados e expor comportamentos

**Encapsulamento** é deixar os dados do objeto escondidos (`private`) e oferecer métodos que mexem neles **seguindo regras**:

```java
public class ContaBancaria {
    private double saldo;

    public double getSaldo() {
        return saldo;
    }

    public void depositar(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O depósito precisa ser positivo.");
        }
        saldo += valor;
    }

    public void sacar(double valor) {
        if (valor <= 0 || valor > saldo) {
            throw new IllegalArgumentException("Saque inválido.");
        }
        saldo -= valor;
    }
}
```

Agora o único jeito de mudar o saldo é por `depositar` e `sacar`, e os dois conferem o valor antes. A regra "o saldo nunca fica negativo" fica num lugar só, dentro da própria classe.

## Não é só criar get e set para tudo

Repare que a classe acima **não tem** um `setSaldo`. Criar automaticamente um `get` e um `set` para cada atributo devolve o problema do começo: qualquer um volta a poder colocar qualquer valor. O encapsulamento está em pensar **quais operações fazem sentido** para o objeto.

Em **Java efetivo**, Joshua Bloch recomenda deixar cada classe e cada membro **o menos acessível possível**: comece com `private` e só abra o que for realmente necessário. Os Deitel, em **Java: como programar**, apresentam a mesma ideia ao introduzir classes e objetos: os atributos ficam privados e são acessados por métodos públicos.

## Por que isso importa

- **Proteção**: o objeto nunca fica num estado inválido.
- **Manutenção**: se a regra mudar (por exemplo, permitir cheque especial), você altera um lugar só.
- **Liberdade para mudar por dentro**: quem usa a classe depende só dos métodos públicos, e não de como os dados são guardados.

## Para treinar

Crie uma classe `Aluno` com o atributo `private double nota` e um método que só aceite notas entre 0 e 10.', 'DEITEL, Paul; DEITEL, Harvey. Java: como programar. 10. ed. São Paulo: Pearson Education do Brasil, 2017.
BLOCH, Joshua. Java efetivo: as melhores práticas para a plataforma Java. 3. ed. Rio de Janeiro: Alta Books, 2019.', NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE id_post = id_post;

-- CONFERÊNCIA: tem que mostrar 5 posts, todos como rascunho
SELECT slug, tag, CHAR_LENGTH(conteudo) AS caracteres, IF(publicado_em IS NULL, 'rascunho', 'publicado') AS situacao
  FROM post_blog ORDER BY id_post;
