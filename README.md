# Propostas comerciais com Builder e Singleton (Java 17)

Aplicação executável em Java 17 puro (sem frameworks) que usa **Builder** para montar propostas
comerciais válidas e **Singleton** (Initialization-on-demand holder) para a configuração comercial.

## Estrutura

```
propostas/
├── README.md
├── docs/
│   ├── diagrama-uml.png      # diagrama de classes
│   ├── captura-saida.png     # captura da saída do programa
│   └── saida.txt             # saída completa em texto
└── src/propostas/
    ├── Aplicacao.java                        # main e ponto de composição
    ├── configuracao/ConfiguracaoComercial.java   # Singleton (holder idiom)
    └── proposta/
        ├── ItemProposta.java                 # record: descrição, quantidade, valor unitário
        ├── PropostaComercial.java            # produto imutável
        ├── PropostaBuilder.java              # contrato das etapas
        ├── PropostaPadraoBuilder.java        # Builder concreto (valida e reinicia)
        ├── DiretorPropostas.java             # receitas básica e completa
        └── PropostaInvalidaException.java    # falha de validação
```

## Compilar e executar

Requer JDK 17 ou superior. A partir da pasta raiz do projeto:

```bash
mkdir out
javac -encoding UTF-8 --release 17 -d out $(find src -name '*.java')
java -Dfile.encoding=UTF-8 -cp out propostas.Aplicacao
```

No Windows (PowerShell):

```powershell
mkdir out
javac -encoding UTF-8 --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -Dfile.encoding=UTF-8 -cp out propostas.Aplicacao
```

## Decisões principais

- **Composição no `main`:** `ConfiguracaoComercial.getInstancia()` é chamado apenas em `Aplicacao`.
  Moeda e limite de desconto são passados ao `PropostaPadraoBuilder` pelo construtor.
- **Imutabilidade:** `PropostaComercial` é `final`, sem setters, com construtor package-private
  (só o Builder a cria) e `List.copyOf` nos itens.
- **Validação centralizada:** `PropostaPadraoBuilder.validar()` reúne todas as regras; `construir()`
  só entrega o produto se todas passarem.
- **Reinicialização:** após entregar uma proposta, o Builder zera todo o estado. Se a validação
  falhar, o estado é mantido para correção e nova tentativa; `iniciarProposta(...)` sempre descarta
  o estado anterior.
- **Dinheiro:** `BigDecimal`, com arredondamento `HALF_UP` a 2 casas no desconto.

## Perguntas

**1. Qual problema do exercício foi resolvido pelo Builder?**
Uma proposta tem poucos dados obrigatórios e vários opcionais (itens, desconto, observações,
moeda). Um construtor com todos esses parâmetros seria longo, propenso a erro de ordem e difícil de
ler, e construtores sobrecarregados explodiriam em número de combinações. O Builder monta a proposta
por etapas nomeadas e legíveis, concentra a validação num único ponto (`construir()`) e garante que
só exista um objeto completo e válido, que então se torna imutável.

**2. Por que `PropostaComercial` não deve ser Singleton?**
Cada proposta é uma entidade distinta, com cliente, itens e valores próprios; o sistema precisa de
muitas instâncias ao mesmo tempo. Um Singleton armazenaria estado global compartilhado: criar uma
proposta sobrescreveria a anterior e quebraria a imutabilidade e o isolamento entre propostas. A
unicidade só faz sentido para algo que é conceitualmente único, como a configuração comercial.

**3. Qual é o escopo real da unicidade de `ConfiguracaoComercial`?**
Uma instância por *classloader* dentro de uma única JVM em execução. Outra JVM (outro processo ou
servidor), ou outro classloader na mesma JVM (comum em servidores de aplicação), terá a sua própria
instância. Não há unicidade global, entre máquinas ou persistente entre execuções.

**4. Por que o Director é útil neste exercício, mas não é obrigatório para toda proposta?**
O Director reúne receitas que se repetem (proposta básica e completa), evitando duplicar a sequência
de chamadas ao Builder e padronizando o resultado. Mas ele só conhece as combinações previstas:
uma proposta que foge delas, como a personalizada do `main`, é montada usando o Builder diretamente,
sem perder validação nem legibilidade. O Director é uma conveniência, não uma exigência do padrão.

**5. Que dificuldade de teste surgiria se todas as classes chamassem `ConfiguracaoComercial.getInstancia()` internamente?**
Elas ficariam acopladas a um estado global escondido. Um teste do Builder não conseguiria trocar o
limite de desconto (por exemplo, testar com 5% ou 0%) nem a moeda, porque os valores estariam fixos
na instância única; e alterar a configuração num teste vazaria para os demais, criando dependência
de ordem de execução. Também não seria possível usar um objeto falso (*stub*) no lugar da
configuração. Passando os valores pelo construtor, cada teste cria o Builder com os parâmetros que
quiser.
