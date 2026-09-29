package propostas.proposta;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Item de uma proposta: descrição, quantidade e valor unitário.
 *
 * <p>Usamos {@code record} (Java 17) porque o item é um valor imutável sem
 * identidade própria. O construtor compacto garante que nenhum item inválido
 * chegue ao Builder, então a validação do item fica no próprio item.
 * BigDecimal evita os erros de arredondamento de double em valores monetários.
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
