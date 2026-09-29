package propostas.configuracao;

import java.math.BigDecimal;

/**
 * Singleton com a configuração comercial da aplicação (moeda padrão e limite de desconto).
 *
 */
public final class ConfiguracaoComercial {

    private final String moedaPadrao = "BRL";
    private final BigDecimal limiteMaximoDesconto = new BigDecimal("15");

    // Construtor privado: ninguém fora desta classe consegue criar outra instância.
    private ConfiguracaoComercial() {
    }

    /** Guarda a instância. Só é inicializado quando getInstancia() é chamado pela 1ª vez. */
    private static final class Holder {
        private static final ConfiguracaoComercial INSTANCIA = new ConfiguracaoComercial();
    }

    public static ConfiguracaoComercial getInstancia() {
        return Holder.INSTANCIA;
    }

    public String getMoedaPadrao() {
        return moedaPadrao;
    }

    /** Percentual máximo de desconto (15 significa 15%). */
    public BigDecimal getLimiteMaximoDesconto() {
        return limiteMaximoDesconto;
    }
}
