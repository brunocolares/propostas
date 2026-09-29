package propostas;

import propostas.configuracao.ConfiguracaoComercial;
import propostas.proposta.DiretorPropostas;
import propostas.proposta.ItemProposta;
import propostas.proposta.PropostaBuilder;
import propostas.proposta.PropostaComercial;
import propostas.proposta.PropostaInvalidaException;
import propostas.proposta.PropostaPadraoBuilder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Ponto de composição da aplicação.
 *
 * <p>É aqui, e somente aqui, que o Singleton é consultado. Os valores lidos são
 * entregues ao Builder pelo construtor; nenhuma outra classe conhece a
 * ConfiguracaoComercial. Assim os dois padrões ficam com responsabilidades separadas:
 * o Singleton controla A INSTÂNCIA da configuração, o Builder controla COMO cada
 * proposta é montada.
 */
public class Aplicacao {

    public static void main(String[] args) {
        // 1) Singleton: duas chamadas devem devolver a mesma referência.
        ConfiguracaoComercial config = ConfiguracaoComercial.getInstancia();
        ConfiguracaoComercial outra = ConfiguracaoComercial.getInstancia();
        System.out.println("Mesma configuração? " + (config == outra));
        System.out.println("Moeda padrão: " + config.getMoedaPadrao());
        System.out.println("Limite máximo de desconto: "
                + config.getLimiteMaximoDesconto().toPlainString() + "%");
        System.out.println();

        // 2) Composição: valores da configuração entram no Builder via construtor.
        PropostaBuilder builder = new PropostaPadraoBuilder(
                config.getMoedaPadrao(), config.getLimiteMaximoDesconto());
        DiretorPropostas diretor = new DiretorPropostas(builder);

        // 3) Proposta básica: receita pronta do Director.
        PropostaComercial basica = diretor.criarPropostaBasica(
                "Metalúrgica Vale do Aço", "Ana Souza", 15,
                new ItemProposta("Licença anual do sistema de laudos", 1, new BigDecimal("4800.00")));
        imprimir("Proposta básica", basica, false);

        // 4) Proposta personalizada: Builder direto, sem Director, porque foge das receitas.
        PropostaComercial personalizada = builder
                .iniciarProposta("Laboratório Aço Norte", "Carlos Lima", 30)
                .adicionarItem(new ItemProposta("Desenvolvimento do módulo de relatórios", 1, new BigDecimal("12500.00")))
                .adicionarItem(new ItemProposta("Horas de consultoria", 40, new BigDecimal("220.00")))
                .adicionarItem(new ItemProposta("Treinamento da equipe (turma)", 2, new BigDecimal("1900.00")))
                .aplicarDesconto(new BigDecimal("10"))
                .definirObservacoes("Pagamento em 3 parcelas iguais; 1ª na assinatura.")
                .definirMoeda(config.getMoedaPadrao())
                .construir();
        imprimir("Proposta personalizada", personalizada, true);

        // 5) A proposta básica não foi contaminada pela montagem seguinte (Builder reiniciado).
        System.out.println("Itens da proposta básica após nova montagem: "
                + basica.getItens().size());

        // 6) Proteção da coleção: a lista do produto é imutável.
        try {
            basica.getItens().add(new ItemProposta("Item intruso", 1, BigDecimal.ONE));
        } catch (UnsupportedOperationException e) {
            System.out.println("Lista de itens protegida: alteração externa rejeitada.");
        }
        System.out.println();

        // 7) Proposta inválida: sem itens. A mensagem vem da validação do Builder.
        try {
            builder.iniciarProposta("Cliente sem itens", "Bruno", 10).construir();
        } catch (PropostaInvalidaException e) {
            System.out.println("Validação: " + e.getMessage());
        }

        // 8) Outra invalidação: desconto de 20% acima do limite de 15% da configuração.
        try {
            builder.iniciarProposta("Cliente exigente", "Bruno", 10)
                    .adicionarItem(new ItemProposta("Suporte mensal", 1, new BigDecimal("1000.00")))
                    .aplicarDesconto(new BigDecimal("20"))
                    .construir();
        } catch (PropostaInvalidaException e) {
            System.out.println("Validação: " + e.getMessage());
        }
    }

    private static void imprimir(String titulo, PropostaComercial p, boolean comDesconto) {
        System.out.println(titulo + ":");
        System.out.println("  Cliente: " + p.getCliente() + " | Responsável: " + p.getResponsavel()
                + " | Validade: " + p.getValidadeEmDias() + " dias");
        for (ItemProposta item : p.getItens()) {
            System.out.println("  - " + item.descricao() + " (" + item.quantidade() + " x "
                    + dinheiro(p.getMoeda(), item.valorUnitario()) + ")");
        }
        if (!p.getObservacoes().isEmpty()) {
            System.out.println("  Observações: " + p.getObservacoes());
        }
        System.out.println("  Subtotal: " + dinheiro(p.getMoeda(), p.subtotal()));
        System.out.println("  Desconto (" + p.getDescontoPercentual().toPlainString() + "%): "
                + dinheiro(p.getMoeda(), p.valorDesconto()));
        System.out.println("  " + (comDesconto ? "Total com desconto: " : "Total: ")
                + dinheiro(p.getMoeda(), p.total()));
        System.out.println();
    }

    /** Formata no padrão brasileiro (1.234,56); "R$" para BRL, o código da moeda nos demais casos. */
    private static String dinheiro(String moeda, BigDecimal valor) {
        DecimalFormat formato = new DecimalFormat("#,##0.00",
                DecimalFormatSymbols.getInstance(new Locale("pt", "BR")));
        String simbolo = "BRL".equals(moeda) ? "R$" : moeda;
        return simbolo + " " + formato.format(valor);
    }
}
