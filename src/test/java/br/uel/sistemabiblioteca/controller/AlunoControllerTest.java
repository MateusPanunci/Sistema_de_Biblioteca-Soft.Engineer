package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Aluno;
import br.uel.sistemabiblioteca.controller.AlunoController;
import br.uel.sistemabiblioteca.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class AlunoControllerTest {

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AlunoController alunoController;

    private MockMvc mockMvc;


    // Antes de cada @test configura um mockMVC com apenas o alunoController
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alunoController).build();
    }

    //   Nos tests da controller, usamos o throws Exception na declaração dela
    // para não precisar colocar um try catch nos testes. 
    // já nos asserts e nos verifies, lançam erros específicos que o Junit consegue 
    // capturar sem problemas e encerrar o teste como falho

    // CT-07 — Erro ao cadastrar RA duplicado
    @Test
    void deveBloquearCadastroQuandoRaJaExistir() throws Exception {
        when(alunoRepository.findByRa("2024001"))
                .thenReturn(Optional.of(new Aluno("Ja Existe", "2024001")));

    
        mockMvc.perform(post("/alunos/novo")
                        .param("nome", "Maria Silva")
                        .param("ra", "2024001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alunos/novo"))
                .andExpect(flash().attributeExists("erro"));

        verify(alunoRepository, never()).save(org.mockito.ArgumentMatchers.any(Aluno.class));
    }

    // CT-05 — Sucesso ao cadastrar aluno
    @Test
    void deveCadastrarAlunoComSucesso() throws Exception {
        when(alunoRepository.findByRa("2024001")).thenReturn(Optional.empty());

        mockMvc.perform(post("/alunos/novo")
                        .param("nome", "Maria Silva")
                        .param("ra", "2024001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/alunos"))
                .andExpect(flash().attributeExists("sucesso"));

        // Captura o objeto Aluno que foi passado para o método save
        // já que não foi instanciado um objeto aluno antes da requisição HTTP
        ArgumentCaptor<Aluno> captor = ArgumentCaptor.forClass(Aluno.class);
        verify(alunoRepository).save(captor.capture());
        assertEquals("Maria Silva", captor.getValue().getNome());
        assertEquals("2024001", captor.getValue().getRa());
    }
}