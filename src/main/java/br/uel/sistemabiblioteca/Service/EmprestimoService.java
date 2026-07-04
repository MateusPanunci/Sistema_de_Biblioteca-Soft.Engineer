package br.uel.sistemabiblioteca.service;

import br.uel.sistemabiblioteca.model.Aluno;
import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.model.Livro;
import br.uel.sistemabiblioteca.repository.AlunoRepository;
import br.uel.sistemabiblioteca.repository.EmprestimoRepository;
import br.uel.sistemabiblioteca.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmprestimoService {

    private final AlunoRepository alunoRepository;
    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;

    public EmprestimoService(AlunoRepository alunoRepository,
                             LivroRepository livroRepository,
                             EmprestimoRepository emprestimoRepository) {
        this.alunoRepository = alunoRepository;
        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
    }


    @Transactional
    public Emprestimo emprestar(String ra, List<Long> livroIds) {

        // verifica se o aluno existe
        Aluno aluno = alunoRepository.findByRa(ra)
                .orElseThrow(() -> new RuntimeException("Aluno inexistente"));

        // verifica débito
        if (aluno.isPossuiDebito()) {
            throw new RuntimeException("Aluno possui débito pendente");
        }

        // pra cada livro, verifica se pode ser emprestado
        List<Livro> livrosDisponiveis = new ArrayList<>();
        for (Long id : livroIds) {
            Livro livro = livroRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Livro não encontrado: " + id));
            if (livro.podeSerEmprestado()) {
                livrosDisponiveis.add(livro);
            }
        }

        if (livrosDisponiveis.isEmpty()) {
            throw new RuntimeException("Nenhum dos livros solicitados está disponível para empréstimo");
        }

        // cria o empréstimo
        Emprestimo emprestimo = new Emprestimo(aluno);
        emprestimo.setLivros(livrosDisponiveis);
        emprestimo.setDataEmprestimo(LocalDate.now());

        // calcula a data prevista de devolução
        LocalDate dataDevolucao = calcularDataDevolucao(livrosDisponiveis);
        emprestimo.setDataPrevistaDevolucao(dataDevolucao);

        // marca cada livro como indisponível
        for (Livro livro : livrosDisponiveis) {
            livro.setDisponivel(false);
            livroRepository.save(livro);
        }

        // persiste o empréstimo
        return emprestimoRepository.save(emprestimo);
    }

    // calcula a data de devolução com base no prazo de cada livro.
    //pega a maior data de devolução entre os livros
    // se há mais de 2 livros, acrescenta 2 dias extras por livro adicional
 
    private LocalDate calcularDataDevolucao(List<Livro> livros) {
        LocalDate hoje = LocalDate.now();
        LocalDate maiorData = hoje;

        for (Livro livro : livros) {
            LocalDate dataLivro = hoje.plusDays(livro.getPrazoDevolucaoDias());
            if (dataLivro.isAfter(maiorData)) {
                maiorData = dataLivro;
            }
        }

        // mais de 2 livros = +2 dias por livro adicional
        if (livros.size() > 2) {
            int extras = livros.size() - 2;
            maiorData = maiorData.plusDays((long) extras * 2);
        }

        return maiorData;
    }
}
