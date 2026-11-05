package br.ufla.desafiocache.banco;

public record Evento(long id, String nome, String local) {

    public Evento comNome(String novoNome) {
        return new Evento(id, novoNome, local);
    }
}
