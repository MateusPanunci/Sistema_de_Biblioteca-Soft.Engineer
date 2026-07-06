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


    // faz um teste para verificar se o formulário de empréstimo é exibido corretamente
    // manda uma requisição GET para a URL "/emprestimos/novo" e espera que o status da resposta seja 200 (OK) e que a view retornada seja "emprestimo/novo"
    @Test
    void deveExibirFormularioDeEmprestimo() throws Exception {
        mockMvc.perform(get("/emprestimos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/novo"));
    }
     
    @Test
    void deveRedirecionarComSucessoQuandoEmprestimoForRealizado() throws Exception {
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

    @Test
    void deveRedirecionarComErroQuandoEmprestimoFalhar() throws Exception {
        when(emprestimoService.emprestar("2024001", anyList()))
                .thenThrow(new RuntimeException("Aluno inexistente"));

        mockMvc.perform(post("/emprestimos/novo")
                        .param("ra", "2024001")
                        .param("livroIds", "1"))
                .andExpect(status().is3xxRedirection()) 
                .andExpect(redirectedUrl("/emprestimos/novo"))
                .andExpect(flash().attributeExists("erro"));
    }
}