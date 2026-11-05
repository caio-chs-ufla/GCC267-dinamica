package br.ufla.desafiocache;

import br.ufla.desafiocache.banco.Usuario;
import br.ufla.desafiocache.service.PerfilService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Bug 1 · Cada um vê o próprio perfil")
class Bug1PerfilTest extends TesteComRedis {

    @Autowired
    PerfilService perfis;

    @Test
    @DisplayName("Ana abre o perfil e, depois, o Bruno abre o dele")
    void cadaUsuarioVeOProprioPerfil() {
        Usuario ana = perfis.buscarPerfil(1);
        assertEquals("Ana", ana.nome(), "A Ana deveria ver o perfil da Ana.");

        Usuario bruno = perfis.buscarPerfil(2);
        assertEquals("Bruno", bruno.nome(),
                "O Bruno abriu o perfil e recebeu os dados de " + bruno.nome() + ". Vazamento de dados!");
    }

    @Test
    @DisplayName("A segunda leitura do mesmo perfil vem do cache")
    void segundaLeituraVemDoCache() {
        perfis.buscarPerfil(1);
        perfis.buscarPerfil(1);

        assertEquals(1, banco.totalDeConsultas(),
                "A segunda leitura do mesmo perfil não deveria ir ao banco.");
    }
}
