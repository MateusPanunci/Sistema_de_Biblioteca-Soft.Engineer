package br.uel.sistemabiblioteca.service;

import br.uel.sistemabiblioteca.model.Aluno;
import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.model.Livro;
import br.uel.sistemabiblioteca.repository.AlunoRepository;
import br.uel.sistemabiblioteca.repository.EmprestimoRepository;
import br.uel.sistemabiblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks // Objeto ao qual vai injetar os mocks acima simulados/fictícios
    private EmprestimoService emprestimoService;

    // CT-10 — Erro ao emprestar para aluno inexistente
    @Test
    void deveLancarExcecaoQuandoAlunoNaoExistir() {
        // Configura o comportamento do mock alunoRepository para retornar um Optional vazio 
        // quando o método findByRa for chamado com o RA "2024001"
        when(alunoRepository.findByRa("2024001")).thenReturn(Optional.empty()); 

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emprestimoService.emprestar("2024001", List.of(1L)));

        // Se a mensagem de exceção for diferente de "Aluno inexistente", o teste falha
        assertEquals("Aluno inexistente", exception.getMessage());

        // Verifica se não houve interações com os mocks livroRepository e emprestimoRepository,
        // se houve, o teste falha
        verifyNoInteractions(livroRepository, emprestimoRepository);
    }

    // CT-11 — Erro ao emprestar para aluno com débito
    @Test
    void deveLancarExcecaoQuandoAlunoPossuirDebito() {
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        aluno.setPossuiDebito(true);
        when(alunoRepository.findByRa("2024001")).thenReturn(Optional.of(aluno));

        // Tem que retornar um erro  
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emprestimoService.emprestar("2024001", List.of(1L))); 
            

        // Com está mensagem de erro especificamente
        assertEquals("Aluno possui débito pendente", exception.getMessage());

        // E sem ter interagido com estas repositories
        verifyNoInteractions(livroRepository, emprestimoRepository);
    }

    // CT-12 — Erro ao tentar emprestar sem selecionar livro
    @Test
    void deveLancarExcecaoQuandoListaDeLivrosForVazia() {
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        when(alunoRepository.findByRa("2024001")).thenReturn(Optional.of(aluno));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emprestimoService.emprestar("2024001", List.of()));

        assertEquals("Nenhum dos livros solicitados está disponível para empréstimo", exception.getMessage());
        verifyNoInteractions(emprestimoRepository);
    }

    // CT-13 — Livro indisponível não aparece para empréstimo
    @Test
    void deveLancarExcecaoQuandoNenhumLivroDisponivel() {
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        when(alunoRepository.findByRa("2024001")).thenReturn(Optional.of(aluno));

        Livro livro = new Livro("Livro A", "Autor A", "ISBN-001", 5);
        livro.setDisponivel(false);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emprestimoService.emprestar("2024001", List.of(1L)));

        assertEquals("Nenhum dos livros solicitados está disponível para empréstimo", exception.getMessage());
        verifyNoInteractions(emprestimoRepository);
    }

    // CT-08 — Sucesso no empréstimo de um livro
    @Test
    void deveRealizarEmprestimoValidoComUmLivro() {
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        when(alunoRepository.findByRa("2024001")).thenReturn(Optional.of(aluno)); 

        Livro livro1 = new Livro("Livro A", "Autor A", "ISBN-001", 5);

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro1));
        
        // Configura o comportamento do mock emprestimoRepository para retornar o próprio objeto Emprestimo
        // invocation é o objeto que está sendo salvo, e getArgument(0) retorna o primeiro argumento passado para o método save
        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = emprestimoService.emprestar("2024001", List.of(1L));

        assertEquals(Emprestimo.StatusEmprestimo.ATIVO, emprestimo.getStatus());
        assertEquals(LocalDate.now().plusDays(5), emprestimo.getDataPrevistaDevolucao());
        assertFalse(livro1.isDisponivel()); // se o livro não estiver disponível, o teste passa, pois le tem que aparecer como emprestado
        verify(livroRepository).save(livro1);
        verify(emprestimoRepository).save(any(Emprestimo.class));
    }

    // CT-09 — Sucesso no empréstimo de múltiplos livros
    @Test
    void deveRealizarEmprestimoValidoComMultiplosLivros() {
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        when(alunoRepository.findByRa("2024001")).thenReturn(Optional.of(aluno));

        Livro livro1 = new Livro("Livro A", "Autor A", "ISBN-001", 5);
        Livro livro2 = new Livro("Livro B", "Autor B", "ISBN-002", 10);
        Livro livro3 = new Livro("Livro C", "Autor C", "ISBN-003", 7);

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro1));
        when(livroRepository.findById(2L)).thenReturn(Optional.of(livro2));
        when(livroRepository.findById(3L)).thenReturn(Optional.of(livro3));
        
        // Configura o comportamento do mock emprestimoRepository para retornar o próprio objeto Emprestimo
        // invocation é o objeto que está sendo salvo, e getArgument(0) retorna o primeiro argumento passado para o método save
        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = emprestimoService.emprestar("2024001", List.of(1L, 2L, 3L));

        assertEquals(Emprestimo.StatusEmprestimo.ATIVO, emprestimo.getStatus());
        assertEquals(LocalDate.now().plusDays(12), emprestimo.getDataPrevistaDevolucao());
        assertFalse(livro1.isDisponivel()); // se o livro não estiver disponível, o teste passa, pois le tem que aparecer como emprestado
        assertFalse(livro2.isDisponivel());
        assertFalse(livro3.isDisponivel());
        verify(livroRepository).save(livro1);
        verify(livroRepository).save(livro2);
        verify(livroRepository).save(livro3);
        verify(emprestimoRepository).save(any(Emprestimo.class));
    }
}