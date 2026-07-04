package br.uel.sistemabiblioteca.service;

import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.model.Livro;
import br.uel.sistemabiblioteca.repository.EmprestimoRepository;
import br.uel.sistemabiblioteca.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DevolucaoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;

    public DevolucaoService(EmprestimoRepository emprestimoRepository,
                            LivroRepository livroRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
    }

    @Transactional
    public Emprestimo devolver(Long emprestimoId) {

        // localiza o emprestimo
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));

        // verifica se ja foi devolvido
        if (emprestimo.getStatus() == Emprestimo.StatusEmprestimo.DEVOLVIDO) {
            throw new RuntimeException("Este empréstimo já foi devolvido");
        }

        // registra a data efetiva de devoluçao
        LocalDate hoje = LocalDate.now();
        emprestimo.setDataDevolucaoEfetiva(hoje);

        // verifica atraso e aplica débito ao aluno se necessário
        if (hoje.isAfter(emprestimo.getDataPrevistaDevolucao())) {
            emprestimo.getAluno().setPossuiDebito(true);
        }

        // marca todos os livros como disponíveis novamente
        for (Livro livro : emprestimo.getLivros()) {
            livro.setDisponivel(true);
            livroRepository.save(livro);
        }

        // atualiza status e persiste (camada DAO via Repository)
        emprestimo.setStatus(Emprestimo.StatusEmprestimo.DEVOLVIDO);
        return emprestimoRepository.save(emprestimo);
    }
}
