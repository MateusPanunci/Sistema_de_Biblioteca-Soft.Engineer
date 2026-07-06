package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Livro;
import br.uel.sistemabiblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class LivroControllerTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroController livroController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(livroController).build();
    }

    // CT-01 — Sucesso ao cadastrar livro
    // e 
    // CT-04 — Livro cadastrado como reservado
    @Test
    void deveCadastrarLivroComSucesso() throws Exception {
        when(livroRepository.findByIsbn("ISBN-001")).thenReturn(Optional.empty());

        mockMvc.perform(post("/livros/novo")
                        .param("titulo", "Livro A") // passa todos estes parâmetros para o post 
                        .param("autor", "Autor A")
                        .param("isbn", "ISBN-001")
                        .param("prazoDevolucaoDias", "7")
                        .param("reservado", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"))
                .andExpect(flash().attributeExists("sucesso"));

        // Captura o objeto Livro que foi passado para o método save
        // já que não foi instanciado um objeto livro antes da requisição HTTP
        ArgumentCaptor<Livro> captor = ArgumentCaptor.forClass(Livro.class);
        verify(livroRepository).save(captor.capture());

        // verificações no objeto capturado 
        assertEquals("Livro A", captor.getValue().getTitulo());
        assertEquals("Autor A", captor.getValue().getAutor());
        assertEquals("ISBN-001", captor.getValue().getIsbn());
        assertEquals(7, captor.getValue().getPrazoDevolucaoDias());
        assertTrue(captor.getValue().isReservado()); // CT-4
    }

    // CT-02 — Erro ao salvar com campos obrigatórios vazios
    @Test
    void deveBloquearCadastroQuandoCamposObrigatoriosForemVazios() throws Exception {
        mockMvc.perform(post("/livros/novo")
                        .param("titulo", "")
                        .param("autor", "")
                        .param("isbn", "ISBN-001")
                        .param("prazoDevolucaoDias", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros/novo"))
                .andExpect(flash().attributeExists("erro"));

        // Verifica se o método save não foi chamado, impedindo o cadastro do livro
        verify(livroRepository, never()).save(org.mockito.ArgumentMatchers.any(Livro.class));
    }

    // CT-03 — Erro ao cadastrar ISBN duplicado
    @Test 
    void deveBloquearCadastroQuandoIsbnJaExistir() throws Exception {
        when(livroRepository.findByIsbn("ISBN-001"))
                .thenReturn(Optional.of(new Livro("Livro A", "Autor A", "ISBN-001", 7)));

        mockMvc.perform(post("/livros/novo")
                        .param("titulo", "Livro A")
                        .param("autor", "Autor A")
                        .param("isbn", "ISBN-001")
                        .param("prazoDevolucaoDias", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros/novo"))
                .andExpect(flash().attributeExists("erro"));

        // garante que o método save não foi chamado, impedindo o cadastro do livro
        verify(livroRepository, never()).save(org.mockito.ArgumentMatchers.any(Livro.class));
    }
}