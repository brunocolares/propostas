package propostas.proposta;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Item de uma proposta: descrição, quantidade e valor unitário.
 *
 */
public record ItemProposta(String descricao, int quantidade, BigDecimal valorUnitario) {

    public ItemProposta {
        Objects.requireNonNull(valorUnitario, "o valor unitário do item é obrigatório");
        if (descricao == null || descricao.isBlank()) {
            throw new PropostaInvalidaException("a descrição do item não pode estar em branco.");
        }
        if (quantidade <= 0) {
            throw new PropostaInvalidaException("a quantidade do item deve ser maior que zero.");
        }
        if (valorUnitario.signum() < 0) {
            throw new PropostaInvalidaException("o valor unitário do item não pode ser negativo.");
        }
    }

    /** Valor do item sem desconto: quantidade x valor unitário. */
    public BigDecimal subtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
