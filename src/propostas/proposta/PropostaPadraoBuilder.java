package propostas.proposta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder concreto: acumula os dados, valida e entrega uma {@link PropostaComercial}.
 *
 * <p>Esta classe NÃO chama ConfiguracaoComercial.getInstancia(). A moeda padrão e o
 * limite de desconto entram pelo construtor (injeção de dependência manual). O
 * Builder continua testável com qualquer limite e não fica acoplado ao Singleton.
 */
public class PropostaPadraoBuilder implements PropostaBuilder {

    private final String moedaPadrao;
    private final BigDecimal limiteDesconto;

    // Estado da montagem em andamento. Tudo isso é zerado em reiniciar().
    private String cliente;
    private String responsavel;
    private int validadeEmDias;
    private final List<ItemProposta> itens = new ArrayList<>();
    private BigDecimal descontoPercentual;
    private String observacoes;
    private String moeda;
    private boolean iniciada;

    public PropostaPadraoBuilder(String moedaPadrao, BigDecimal limiteDesconto) {
        if (moedaPadrao == null || moedaPadrao.isBlank()) {
            throw new IllegalArgumentException("a moeda padrão é obrigatória.");
        }
        if (limiteDesconto == null || limiteDesconto.signum() < 0) {
            throw new IllegalArgumentException("o limite de desconto deve ser zero ou positivo.");
        }
        this.moedaPadrao = moedaPadrao;
        this.limiteDesconto = limiteDesconto;
        reiniciar();
    }

    @Override
    public PropostaBuilder iniciarProposta(String cliente, String responsavel, int validadeEmDias) {
        reiniciar(); // iniciar sempre parte do zero, mesmo se havia uma montagem abandonada
        this.cliente = cliente;
        this.responsavel = responsavel;
        this.validadeEmDias = validadeEmDias;
        this.iniciada = true;
        return this;
    }

    @Override
    public PropostaBuilder adicionarItem(ItemProposta item) {
        exigirIniciada();
        if (item == null) {
            throw new PropostaInvalidaException("o item da proposta não pode ser nulo.");
        }
        itens.add(item);
        return this;
    }

    @Override
    public PropostaBuilder aplicarDesconto(BigDecimal descontoPercentual) {
        exigirIniciada();
        // O limite é conferido em construir(), quando a proposta é avaliada por inteiro.
        this.descontoPercentual = descontoPercentual;
        return this;
    }

    @Override
    public PropostaBuilder definirObservacoes(String observacoes) {
        exigirIniciada();
        this.observacoes = observacoes;
        return this;
    }

    @Override
    public PropostaBuilder definirMoeda(String moeda) {
        exigirIniciada();
        this.moeda = moeda;
        return this;
    }

    @Override
    public PropostaComercial construir() {
        exigirIniciada();
        validar();

        PropostaComercial proposta = new PropostaComercial(
                cliente.strip(),
                responsavel.strip(),
                validadeEmDias,
                itens, // o produto faz a cópia defensiva
                descontoPercentual == null ? BigDecimal.ZERO : descontoPercentual,
                observacoes == null ? "" : observacoes.strip(),
                moeda);

        // Reinicialização segura: só depois de entregar com sucesso. Se a validação
        // falhar, o estado é mantido para que o chamador possa corrigir e tentar de novo.
        reiniciar();
        return proposta;
    }

    /** Concentra todas as regras num só lugar: ler esta lista é ler o contrato do produto. */
    private void validar() {
        if (cliente == null || cliente.isBlank()) {
            throw new PropostaInvalidaException("o cliente é obrigatório.");
        }
        if (responsavel == null || responsavel.isBlank()) {
            throw new PropostaInvalidaException("o responsável é obrigatório.");
        }
        if (validadeEmDias <= 0) {
            throw new PropostaInvalidaException("a validade deve ser maior que zero dias.");
        }
        if (itens.isEmpty()) {
            throw new PropostaInvalidaException("a proposta deve possuir pelo menos um item.");
        }
        if (moeda == null || moeda.isBlank()) {
            throw new PropostaInvalidaException("a moeda é obrigatória.");
        }
        if (descontoPercentual != null) {
            if (descontoPercentual.signum() < 0) {
                throw new PropostaInvalidaException("o desconto não pode ser negativo.");
            }
            if (descontoPercentual.compareTo(limiteDesconto) > 0) {
                throw new PropostaInvalidaException(
                        "o desconto de " + descontoPercentual.stripTrailingZeros().toPlainString()
                        + "% excede o limite permitido de "
                        + limiteDesconto.stripTrailingZeros().toPlainString() + "%.");
            }
        }
    }

    private void exigirIniciada() {
        if (!iniciada) {
            throw new IllegalStateException("chame iniciarProposta(...) antes das demais etapas.");
        }
    }

    /**
     * Zera todo o estado. A lista é limpa (e não recriada) porque o produto já
     * recebeu uma cópia; nada do que foi entregue é afetado.
     */
    private void reiniciar() {
        cliente = null;
        responsavel = null;
        validadeEmDias = 0;
        itens.clear();
        descontoPercentual = null;
        observacoes = null;
        moeda = moedaPadrao;
        iniciada = false;
    }
}
