package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.service.EmprestimoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class EmprestimoControllerTest {

    // Simula um objeto do tipo EmprestimoService para ser injetado no controller
    @Mock 
    private EmprestimoService emprestimoService;

    // Injeção do mock EmprestimoService no controller EmprestimoController
    @InjectMocks
    private EmprestimoController emprestimoController;

    // MockMvc serve para simular requisições HTTP ao controller
    private MockMvc mockMvc;

    // Configuração inicial antes de cada teste, criando uma instância do MockMvc com o controller a ser testado
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(emprestimoController).build();
    }


    // CT-08 — Sucesso no empréstimo de um livro
    @Test
    void deveRedirecionarComSucessoQuandoEmprestimoComUmLivroForRealizado() throws Exception {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataPrevistaDevolucao(LocalDate.of(2026, 7, 20));
        when(emprestimoService.emprestar("2024001", java.util.List.of(1L)))
                .thenReturn(emprestimo);

        mockMvc.perform(post("/emprestimos/novo")
                        .param("ra", "2024001")
                        .param("livroIds", "1"))
                .andExpect(status().is3xxRedirection()) // Espera ocorrer um redirecionamento
                .andExpect(redirectedUrl("/emprestimos/novo")) // Nesta URL em específico
                .andExpect(flash().attributeExists("sucesso")); // E espera ser retornado um atributo flash com sucesso
    }

    // CT-09 — Sucesso no empréstimo de múltiplos livros
    @Test
    void deveRedirecionarComSucessoQuandoEmprestimoComMultiplosLivrosForRealizado() throws Exception {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataPrevistaDevolucao(LocalDate.of(2026, 7, 20));
        // simula o comportamento do serviço de empréstimo (é um falso) para retornar um objeto Emprestimo quando chamado com os parâmetros fornecidos
        // ou seja, se o método emprestar for chamado com o RA "2024001" e uma lista de IDs de livros contendo 1L e 2L, ele retornará o objeto emprestimo criado acima
        when(emprestimoService.emprestar("2024001", java.util.List.of(1L, 2L)))
                .thenReturn(emprestimo); 

        mockMvc.perform(post("/emprestimos/novo")
                        .param("ra", "2024001")
                        .param("livroIds", "1", "2"))
                // espera um redirecionamento como resposta
                .andExpect(status().is3xxRedirection()) 

                // esse redirecionamento é para a URL "/emprestimos/novo"
                .andExpect(redirectedUrl("/emprestimos/novo"))

                // resposta contém um atributo de flash chamado "sucesso", que indica que o empréstimo foi realizado com sucesso
                .andExpect(flash().attributeExists("sucesso"));
    }

    // CT-10 — Erro ao emprestar para aluno inexistente
    @Test
    void deveRedirecionarComErroQuandoAlunoInexistente() throws Exception {
        when(emprestimoService.emprestar(eq("2024001"), anyList()))
                .thenThrow(new RuntimeException("Aluno inexistente"));

        mockMvc.perform(post("/emprestimos/novo")
                        .param("ra", "2024001")
                        .param("livroIds", "1"))
                .andExpect(status().is3xxRedirection()) 
                .andExpect(redirectedUrl("/emprestimos/novo"))
                .andExpect(flash().attributeExists("erro"));
    }

    // CT-11 — Erro ao emprestar para aluno com débito
    @Test
    void deveRedirecionarComErroQuandoAlunoPossuirDebito() throws Exception {
        when(emprestimoService.emprestar(eq("2024001"), anyList()))
                .thenThrow(new RuntimeException("Aluno possui débito pendente"));

        mockMvc.perform(post("/emprestimos/novo")
                        .param("ra", "2024001")
                        .param("livroIds", "1"))
                .andExpect(status().is3xxRedirection()) 
                .andExpect(redirectedUrl("/emprestimos/novo"))
                .andExpect(flash().attributeExists("erro"));
    }

    // CT-12 — Erro ao tentar emprestar sem selecionar livro
    @Test
    void deveRedirecionarComErroQuandoNenhumLivroForSelecionado() throws Exception {
        when(emprestimoService.emprestar("2024001", java.util.Collections.emptyList()))
                .thenThrow(new RuntimeException("Nenhum dos livros solicitados está disponível para empréstimo"));

        mockMvc.perform(post("/emprestimos/novo")
                        .param("ra", "2024001")
                        .param("livroIds", ""))
                .andExpect(status().is3xxRedirection()) 
                .andExpect(redirectedUrl("/emprestimos/novo"))
                .andExpect(flash().attributeExists("erro"));
    }
}