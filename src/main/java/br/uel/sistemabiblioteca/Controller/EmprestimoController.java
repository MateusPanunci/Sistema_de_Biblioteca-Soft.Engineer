package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.service.EmprestimoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    // exibe o formulario de empréstimo
    @GetMapping("/novo")
    public String formularioEmprestimo(Model model) {
        model.addAttribute("livrosDisponiveis",
                livroRepository.findAll().stream()
                        .filter(Livro::podeSerEmprestado)
                        .toList());
        return "emprestimo/novo";
    }

    // processa o formulário e realiza o empréstimo
    @PostMapping("/novo")
    public String realizarEmprestimo(@RequestParam String ra,
                                     @RequestParam List<Long> livroIds,
                                     RedirectAttributes redirectAttributes) {
        try {
            Emprestimo emprestimo = emprestimoService.emprestar(ra, livroIds);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Empréstimo realizado com sucesso! Devolução prevista para: "
                    + emprestimo.getDataPrevistaDevolucao());
            return "redirect:/emprestimos/novo";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/emprestimos/novo";
        }
    }
}
