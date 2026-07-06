package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.model.Livro;
import br.uel.sistemabiblioteca.repository.AlunoRepository;
import br.uel.sistemabiblioteca.repository.EmprestimoRepository;
import br.uel.sistemabiblioteca.repository.LivroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final LivroRepository livroRepository;
    private final AlunoRepository alunoRepository;
    private final EmprestimoRepository emprestimoRepository;

    public HomeController(LivroRepository livroRepository,
                          AlunoRepository alunoRepository,
                          EmprestimoRepository emprestimoRepository) {
        this.livroRepository = livroRepository;
        this.alunoRepository = alunoRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    // tela inicial com o resumo geral do sistema
    @GetMapping("/")
    public String visaoGeral(Model model) {
        List<Livro> livros = livroRepository.findAll();

        model.addAttribute("totalLivros", livros.size());
        model.addAttribute("livrosDisponiveis",
                livros.stream().filter(Livro::podeSerEmprestado).count());
        model.addAttribute("emprestimosAtivos",
                emprestimoRepository.findByStatus(Emprestimo.StatusEmprestimo.ATIVO).size());
        model.addAttribute("totalAlunos", alunoRepository.count());

        // ultimos 5 emprestimos registrados
        model.addAttribute("emprestimosRecentes",
                emprestimoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                        .stream().limit(5).toList());

        return "index";
    }
}