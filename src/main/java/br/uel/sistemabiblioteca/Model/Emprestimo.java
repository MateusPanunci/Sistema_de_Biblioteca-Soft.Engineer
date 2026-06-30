package br.uel.sistemabiblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emprestimo")
@Getter
@Setter
@NoArgsConstructor
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(nullable = false)
    private LocalDate dataEmprestimo = LocalDate.now();

    // Calcula a partir das datas dos livros
    private LocalDate dataPrevistaDevolucao;

    private LocalDate dataDevolucaoEfetiva;

    // Simplificação da classe livro, ja que só tem 1
    @ManyToMany
    @JoinTable(
        name = "emprestimo_livro",
        joinColumns = @JoinColumn(name = "emprestimo_id"),
        inverseJoinColumns = @JoinColumn(name = "livro_id")
    )
    private List<Livro> livros = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEmprestimo status = StatusEmprestimo.ATIVO;

    public Emprestimo(Aluno aluno) {
        this.aluno = aluno;
    }

    public enum StatusEmprestimo {
        ATIVO,
        DEVOLVIDO
    }
}
