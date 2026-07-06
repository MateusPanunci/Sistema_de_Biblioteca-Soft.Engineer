package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Aluno;
import br.uel.sistemabiblioteca.repository.AlunoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoRepository alunoRepository;

    public AlunoController(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    // lista todos os alunos
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("alunos", alunoRepository.findAll());
        return "aluno/lista";
    }

    //exibe o formulario de cadastro
    @GetMapping("/novo")
    public String formulario() {
        return "aluno/novo";
    }

    //processa o formulário e salva o aluno
    @PostMapping("/novo")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String ra,
                            RedirectAttributes redirectAttributes) {
        // verifica duplicado
        if (alunoRepository.findByRa(ra).isPresent()) {
            redirectAttributes.addFlashAttribute("erro", "RA já cadastrado no sistema.");
            return "redirect:/alunos/novo";
        }

        Aluno aluno = new Aluno(nome, ra);
        alunoRepository.save(aluno);

        redirectAttributes.addFlashAttribute("sucesso", "Aluno cadastrado com sucesso!");
        return "redirect:/alunos";
    }
}
