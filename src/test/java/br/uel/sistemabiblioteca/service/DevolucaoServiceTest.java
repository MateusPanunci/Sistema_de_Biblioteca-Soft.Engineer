package br.uel.sistemabiblioteca.service;

import br.uel.sistemabiblioteca.model.Aluno;
import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.model.Livro;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevolucaoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private DevolucaoService devolucaoService;

    // Os testes possuem basicamente a mesma lógica dos testes do EmprestimoServiceTest, 
    // mas com foco na devolução de livros e no comportamento do sistema quando um empréstimo é devolvido, seja dentro do prazo ou atrasado.
    
    // CT-16 — Erro ao devolver empréstimo já devolvido
    @Test
    void deveLancarExcecaoQuandoEmprestimoJaDevolvido() {
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        Livro livro = new Livro("Livro A", "Autor A", "ISBN-001", 5);
        livro.setDisponivel(false);

        Emprestimo emprestimo = new Emprestimo(aluno);
        emprestimo.setLivros(List.of(livro));
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(1));
        emprestimo.setStatus(Emprestimo.StatusEmprestimo.DEVOLVIDO);
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> devolucaoService.devolver(1L));

        assertEquals("Este empréstimo já foi devolvido", exception.getMessage());
        verifyNoInteractions(livroRepository);
    }

    // CT-14 — Sucesso na devolução dentro do prazo
    @Test
    void deveRegistrarDevolucaoDentroDoPrazoSemDebito() {
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        Livro livro = new Livro("Livro A", "Autor A", "ISBN-001", 5);
        livro.setDisponivel(false);

        Emprestimo emprestimo = new Emprestimo(aluno);
        emprestimo.setLivros(List.of(livro));
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(2));
        emprestimo.setStatus(Emprestimo.StatusEmprestimo.ATIVO);

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo devolvido = devolucaoService.devolver(1L);

        assertEquals(Emprestimo.StatusEmprestimo.DEVOLVIDO, devolvido.getStatus());
        assertEquals(LocalDate.now(), devolvido.getDataDevolucaoEfetiva());
        assertFalse(devolvido.getAluno().isPossuiDebito());
        assertTrue(devolvido.getLivros().get(0).isDisponivel());
        verify(livroRepository).save(emprestimo.getLivros().get(0));
        verify(emprestimoRepository).save(any(Emprestimo.class));
    }

    // CT-15 — Devolução em atraso gera débito para o aluno
    @Test
    void deveMarcarDebitoQuandoDevolucaoAtrasar() {
    
        Aluno aluno = new Aluno("Maria Silva", "2024001");
        Livro livro = new Livro("Livro A", "Autor A", "ISBN-001", 5);
        livro.setDisponivel(false);

        Emprestimo emprestimo = new Emprestimo(aluno);
        emprestimo.setLivros(List.of(livro));
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(1));
        emprestimo.setStatus(Emprestimo.StatusEmprestimo.ATIVO);

        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo devolvido = devolucaoService.devolver(1L);

        assertEquals(Emprestimo.StatusEmprestimo.DEVOLVIDO, devolvido.getStatus());
        assertTrue(devolvido.getAluno().isPossuiDebito());
    }
}