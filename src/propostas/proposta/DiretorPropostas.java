package propostas.proposta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Director: encapsula receitas prontas de montagem.
 *
 * <p>Ele existe para as combinações que se repetem (proposta básica, proposta
 * completa). Não é obrigatório: quem precisa de uma proposta fora dessas receitas
 * usa o Builder diretamente, como a Aplicacao faz na proposta personalizada.
 * O Director depende da interface {@link PropostaBuilder}, não da classe concreta.
 */
public class DiretorPropostas {

    private final PropostaBuilder builder;

    public DiretorPropostas(PropostaBuilder builder) {
        this.builder = builder;
    }

    /** Receita mínima: apenas os dados obrigatórios e um único item. */
    public PropostaComercial criarPropostaBasica(String cliente, String responsavel,
                                                 int validadeEmDias, ItemProposta item) {
        return builder
                .iniciarProposta(cliente, responsavel, validadeEmDias)
                .adicionarItem(item)
                .construir();
    }

    /** Receita completa: vários itens, desconto e observações. */
    public PropostaComercial criarPropostaCompleta(String cliente, String responsavel,
                                                   int validadeEmDias, List<ItemProposta> itens,
                                                   BigDecimal descontoPercentual,
                                                   String observacoes) {
        builder.iniciarProposta(cliente, responsavel, validadeEmDias);
        for (ItemProposta item : itens) {
            builder.adicionarItem(item);
        }
        return builder
                .aplicarDesconto(descontoPercentual)
                .definirObservacoes(observacoes)
                .construir();
    }
}
