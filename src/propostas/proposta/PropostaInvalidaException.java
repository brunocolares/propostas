package propostas.proposta;

/**
 * Sinaliza que os dados acumulados no Builder não formam uma proposta válida.
 * É uma exceção não verificada: erro de montagem é falha de uso do Builder,
 * não uma condição que o chamador possa "recuperar" com um try/catch rotineiro.
 */
public class PropostaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PropostaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
