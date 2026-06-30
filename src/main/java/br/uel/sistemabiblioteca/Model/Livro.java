package br.uel.sistemabiblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "livro")
@Getter
@Setter
@NoArgsConstructor
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(unique = true)
    private String isbn;

    @Column(nullable = false)
    private Integer prazoDevolucaoDias;

    // true = é exemplar reservado/não pode ser emprestado
    @Column(nullable = false)
    private boolean reservado = false;

    @Column(nullable = false)
    private boolean disponivel = true;

    public Livro(String titulo, String autor, String isbn, Integer prazoDevolucaoDias) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.prazoDevolucaoDias = prazoDevolucaoDias;
    }

    // Retorna true se o livro PODE ser emprestado (não está reservado nem indisponível).
    public boolean podeSerEmprestado() {
        return !this.reservado && this.disponivel;
    }
}
