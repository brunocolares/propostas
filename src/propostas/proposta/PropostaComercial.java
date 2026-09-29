package propostas.proposta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Produto final do Builder: uma proposta comercial imutável.
 *
 */
public final class PropostaComercial {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final String cliente;
    private final String responsavel;
    private final int validadeEmDias;
    private final List<ItemProposta> itens;
    private final BigDecimal descontoPercentual;
    private final String observacoes;
    private final String moeda;

    // Sem validação aqui de propósito: o Builder é o guardião das regras de
    // negócio (inclusive o limite de desconto, que vem da configuração).
    PropostaComercial(String cliente, String responsavel, int validadeEmDias,
                      List<ItemProposta> itens, BigDecimal descontoPercentual,
                      String observacoes, String moeda) {
        this.cliente = cliente;
        this.responsavel = responsavel;
        this.validadeEmDias = validadeEmDias;
        this.itens = List.copyOf(itens); // cópia defensiva + imutável
        this.descontoPercentual = descontoPercentual;
        this.observacoes = observacoes;
        this.moeda = moeda;
    }

    public String getCliente() { return cliente; }

    public String getResponsavel() { return responsavel; }

    public int getValidadeEmDias() { return validadeEmDias; }

    /** Lista imutável: tentar alterá-la lança UnsupportedOperationException. */
    public List<ItemProposta> getItens() { return itens; }

    public BigDecimal getDescontoPercentual() { return descontoPercentual; }

    /** Observações são opcionais; string vazia significa "sem observações". */
    public String getObservacoes() { return observacoes; }

    public String getMoeda() { return moeda; }

    /** Soma dos subtotais dos itens, antes do desconto. */
    public BigDecimal subtotal() {
        return itens.stream()
                .map(ItemProposta::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Valor monetário do desconto, arredondado a 2 casas (HALF_UP). */
    public BigDecimal valorDesconto() {
        return subtotal()
                .multiply(descontoPercentual)
                .divide(CEM, 2, RoundingMode.HALF_UP);
    }

    /** Subtotal menos o desconto. */
    public BigDecimal total() {
        return subtotal().subtract(valorDesconto()).setScale(2, RoundingMode.HALF_UP);
    }
}
