package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Emprestimo;
import br.uel.sistemabiblioteca.repository.EmprestimoRepository;
import br.uel.sistemabiblioteca.service.DevolucaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/devolucoes")
public class DevolucaoController {

    private final DevolucaoService devolucaoService;
    private final EmprestimoRepository emprestimoRepository;

    public DevolucaoController(DevolucaoService devolucaoService,
                               EmprestimoRepository emprestimoRepository) {
        this.devolucaoService = devolucaoService;
        this.emprestimoRepository = emprestimoRepository;
    }

    // exibe o formulario de devolução com a lista de emprestimos ativos
    @GetMapping("/nova")
    public String formularioDevolucao(Model model) {
        List<Emprestimo> emprestimosAtivos = emprestimoRepository
                .findByStatus(Emprestimo.StatusEmprestimo.ATIVO);
        model.addAttribute("emprestimosAtivos", emprestimosAtivos);
        return "devolucao/nova"; // src/main/resources/templates/devolucao/nova.html
    }

    // processa a devolução
    @PostMapping("/nova")
    public String realizarDevolucao(@RequestParam Long emprestimoId,
                                    RedirectAttributes redirectAttributes) {
        try {
            devolucaoService.devolver(emprestimoId);
            redirectAttributes.addFlashAttribute("sucesso", "Devolução registrada com sucesso!");
            return "redirect:/devolucoes/nova";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/devolucoes/nova";
        }
    }
}
