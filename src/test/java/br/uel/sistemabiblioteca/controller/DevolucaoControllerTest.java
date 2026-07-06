package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Aluno;
import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.repository.EmprestimoRepository;
import br.uel.sistemabiblioteca.service.DevolucaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class DevolucaoControllerTest {

    @Mock
    private DevolucaoService devolucaoService;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private DevolucaoController devolucaoController;

    private MockMvc mockMvc;

    // Antes de executar cada @teste, o Junit executa este método de configuração,
    // que cria uma instância do MockMvc com o controller a ser testado
    @BeforeEach 
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(devolucaoController).build();
    }

    @Test
    void deveExibirFormularioDeDevolucaoComEmprestimosAtivos() throws Exception {
        Emprestimo emprestimo = new Emprestimo(new Aluno("Maria Silva", "2024001"));
        emprestimo.setStatus(Emprestimo.StatusEmprestimo.ATIVO);
        emprestimo.setDataPrevistaDevolucao(LocalDate.of(2026, 7, 20));

        // Configura o comportamento da função abaixo com o enum "ATIVO", para usar no teste HTTP do mockMvc
        when(emprestimoRepository.findByStatus(Emprestimo.StatusEmprestimo.ATIVO))
                .thenReturn(List.of(emprestimo));
         
        // Faz o teste HTTP simulando uma requisição GET para a URL "/devolucoes/nova"
        // espera que o status da resposta seja 200 (OK), que a view retornada seja "devolucao/nova"
        // e que o modelo da view contenha o atributo "emprestimosAtivos"
        mockMvc.perform(get("/devolucoes/nova"))
                .andExpect(status().isOk())
                .andExpect(view().name("devolucao/nova"))
                .andExpect(model().attributeExists("emprestimosAtivos"));
    }

    @Test
    void deveRedirecionarComSucessoQuandoDevolucaoForRegistrada() throws Exception {
        mockMvc.perform(post("/devolucoes/nova")
                        .param("emprestimoId", "1"))
                .andExpect(status().is3xxRedirection()) // espera que o status da resposta seja um redirecionamento
                .andExpect(redirectedUrl("/devolucoes/nova"))
                .andExpect(flash().attributeExists("sucesso"));
    }

    @Test
    void deveRedirecionarComErroQuandoDevolucaoFalhar() throws Exception {
        when(devolucaoService.devolver(anyLong()))
                .thenThrow(new RuntimeException("Empréstimo não encontrado"));

        mockMvc.perform(post("/devolucoes/nova")
                        .param("emprestimoId", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devolucoes/nova"))
                .andExpect(flash().attributeExists("erro"));
    }
}