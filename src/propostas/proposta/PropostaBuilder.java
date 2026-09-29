package propostas.proposta;

import java.math.BigDecimal;

/**
 * Contrato das etapas de construção de uma proposta.
 * Os métodos de configuração retornam o próprio Builder para permitir
 * encadeamento
 */
public interface PropostaBuilder {

    /** Começa uma nova montagem, descartando qualquer estado anterior. */
    PropostaBuilder iniciarProposta(String cliente, String responsavel, int validadeEmDias);

    PropostaBuilder adicionarItem(ItemProposta item);

    PropostaBuilder aplicarDesconto(BigDecimal descontoPercentual);

    PropostaBuilder definirObservacoes(String observacoes);

    PropostaBuilder definirMoeda(String moeda);

    /** Valida tudo, entrega o produto e deixa o Builder pronto para uma nova montagem. */
    PropostaComercial construir();
}
