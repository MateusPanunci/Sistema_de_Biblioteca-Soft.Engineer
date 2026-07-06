package br.uel.sistemabiblioteca.controller;

import br.uel.sistemabiblioteca.model.Livro;
import br.uel.sistemabiblioteca.repository.LivroRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/livros")
public class LivroController {

    private final LivroRepository livroRepository;

    public LivroController(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    // lista todos os livros
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("livros", livroRepository.findAll());
        return "livro/lista";
    }

    // exibe o formulario de cadastro
    @GetMapping("/novo")
    public String formulario() {
        return "livro/novo";
    }

    //processa o formulário e salva o livro
    @PostMapping("/novo")
    public String cadastrar(@RequestParam String titulo,
                            @RequestParam String autor,
                            @RequestParam(required = false) String isbn,
                            @RequestParam Integer prazoDevolucaoDias,
                            @RequestParam(required = false) boolean reservado,
                            RedirectAttributes redirectAttributes) {
        if (titulo == null || titulo.isBlank() || autor == null || autor.isBlank()) {
            redirectAttributes.addFlashAttribute("erro", "Título e autor são obrigatórios.");
            return "redirect:/livros/novo";
        }

        // verifica duplicado (só se foi informado)
        if (isbn != null && !isbn.isBlank() && livroRepository.findByIsbn(isbn).isPresent()) {
            redirectAttributes.addFlashAttribute("erro", "ISBN já cadastrado no sistema.");
            return "redirect:/livros/novo";
        }

        Livro livro = new Livro(titulo, autor,
                (isbn != null && !isbn.isBlank()) ? isbn : null,
                prazoDevolucaoDias);
        livro.setReservado(reservado);
        livroRepository.save(livro);

        redirectAttributes.addFlashAttribute("sucesso", "Livro cadastrado com sucesso!");
        return "redirect:/livros";
    }
}
